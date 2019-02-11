package com.example.elasticsearch.elastic.changesDetecrot;

import lombok.Getter;
import lombok.Setter;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.CollectionChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.ElementValueChange;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.metamodel.object.InstanceId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class CheckListChangesDetector implements ChangesDetector {

    private Javers javers;

    @Autowired
    public CheckListChangesDetector(Javers javers) {
        this.javers = javers;
    }


    @Getter
    @Setter
    private class BaseModifiedPair {
        private CheckListItem base;
        private CheckListItem modified;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BaseModifiedPair pair = (BaseModifiedPair) o;

            if (base != null ? !base.equals(pair.base) : pair.base != null) return false;
            return modified != null ? modified.equals(pair.modified) : pair.modified == null;
        }

        @Override
        public int hashCode() {
            int result = base != null ? base.hashCode() : 0;
            result = 31 * result + (modified != null ? modified.hashCode() : 0);
            return result;
        }
    }

    @Override
    public Set<HistoryChanges> getHistoryChanges(List<CheckListItem> left, List<CheckListItem> right) {

        List<HistoryChanges> historyChanges = new ArrayList<>();

        Map<CheckListItem, String> changesMap = new HashMap<>();

        Diff diff = javers.compareCollections(left, right, CheckListItem.class);

        List<Change> changes = diff.getChanges();

        if (!CollectionUtils.isEmpty(changes)) {

            for (Change change : changes) {

                if (change instanceof ObjectRemoved) {
                    BaseModifiedPair removed = getRemoved((ObjectRemoved) change);
                    HistoryChanges removedChange = buildChange("REMOVED", removed.getBase(), removed.getModified());
                    historyChanges.add(removedChange);
                    continue;
                }

                if (change instanceof NewObject) {
                    BaseModifiedPair added = getAdded((NewObject) change);
                    HistoryChanges addedChange = buildChange("ADDED", added.getBase(), added.getModified());
                    historyChanges.add(addedChange);
                    continue;
                }


                if (change instanceof ValueChange) {
                    BaseModifiedPair singleModified = getSingleModified(left, (ValueChange) change);
                    CheckListItem modified = singleModified.getModified();
                    String type = changesMap.get(modified);

                    if (type == null || !"CHANGED".equals(type)) {
                        HistoryChanges modifiedChange = buildChange("CHANGED", singleModified.getBase(), singleModified.getModified());
                        historyChanges.add(modifiedChange);
                        changesMap.put(modified, "CHANGED");
                    }

                } else {

                    List<BaseModifiedPair> modified = getModified(left, right, (CollectionChange) change);
                    for (BaseModifiedPair modifiedPair : modified) {
                        HistoryChanges modifiedChange = buildChange("CHANGED", modifiedPair.getBase(), modifiedPair.getModified());
                        historyChanges.add(modifiedChange);
                    }
                }

            }
        }


        return new HashSet<>(historyChanges);
    }


    private BaseModifiedPair getRemoved(ObjectRemoved removed) {
        BaseModifiedPair pair = new BaseModifiedPair();

        Optional<Object> affectedObject = removed.getAffectedObject();
        if (affectedObject.isPresent()) {
            Object removedObject = affectedObject.get();
            if (removedObject instanceof CheckListItem) {
                pair.setModified((CheckListItem) removedObject);
                pair.setBase((CheckListItem) removedObject);
            }
        }
        return pair;

    }


    private BaseModifiedPair getAdded(NewObject newObject) {
        BaseModifiedPair pair = new BaseModifiedPair();
        Optional<Object> affectedObject = newObject.getAffectedObject();
        if (affectedObject.isPresent()) {
            Object added = affectedObject.get();
            if (added instanceof CheckListItem) {
                pair.setBase(null);
                pair.setModified((CheckListItem) added);
            }
        }
        return pair;
    }


    private BaseModifiedPair getSingleModified(List<CheckListItem> left, ValueChange change) {
        Optional<Object> affectedObject = change.getAffectedObject();
        BaseModifiedPair pair = new BaseModifiedPair();
        if (affectedObject.isPresent()) {
            CheckListItem modified = (CheckListItem) affectedObject.get();
            pair.setModified(modified);
            Optional<CheckListItem> base = left.stream().filter(checkListItem -> checkListItem.getId().equals(modified.getId())).findFirst();
            base.ifPresent(pair::setBase);
        }
        return pair;
    }

    private List<BaseModifiedPair> getModified(List<CheckListItem> left, List<CheckListItem> right, CollectionChange changes) {

        List<BaseModifiedPair> baseModifiedPairs = new ArrayList<>();
        Optional<Object> affectedObject = changes.getAffectedObject();
        if (affectedObject.isPresent()) {
            List<ContainerElementChange> containerChanges = changes.getChanges();
            if (containerChanges != null) {
                for (ContainerElementChange change : containerChanges) {
                    if (change instanceof ValueAdded) {
                        processChangesForAddedItem(right, baseModifiedPairs, (ValueAdded) change);
                        continue;
                    }

                    if (change instanceof ElementValueChange) {
                        ElementValueChange elementValueChange = (ElementValueChange) change;
                        Object base = elementValueChange.getLeftValue();
                        Object modified = elementValueChange.getRightValue();
                        if (base instanceof InstanceId
                                && modified instanceof InstanceId) {
                            Object baseId = ((InstanceId) base).getCdoId();
                            Object modifiedId = ((InstanceId) modified).getCdoId();
                            Optional<CheckListItem> second = right.stream().filter(checkListItem -> checkListItem.getId().equals(baseId)).findFirst();
                            Optional<CheckListItem> first = left.stream().filter(checkListItem -> checkListItem.getId().equals(baseId)).findFirst();

                            if (baseId.equals(modifiedId)
                                    && first.isPresent()
                                    && second.isPresent()) {
                                CheckListItem baseValue = first.get();
                                CheckListItem modifiedValue = second.get();
                                BaseModifiedPair pair = createPair(baseValue, modifiedValue);
                                baseModifiedPairs.add(pair);
                            } else {
                                Optional<CheckListItem> modifiedValue = right.stream().filter(checkListItem -> checkListItem.getId().equals(modifiedId)).findFirst();
                                if (modifiedValue.isPresent()) {
                                    BaseModifiedPair pair = createPair(null, modifiedValue.get());
                                    baseModifiedPairs.add(pair);
                                }
                            }
                        } else {
                            BaseModifiedPair pair = createPair((CheckListItem) base, (CheckListItem) modified);
                            baseModifiedPairs.add(pair);

                        }
                    }
                }

            }
        }

        return baseModifiedPairs;
    }

    private void processChangesForAddedItem(List<CheckListItem> right, List<BaseModifiedPair> baseModifiedPairs, ValueAdded change) {
        Object value = change.getValue();
        if (value instanceof InstanceId) {
            Object addedId = ((InstanceId) value).getCdoId();
            Optional<CheckListItem> added = right.stream().filter(checkListItem -> checkListItem.getId().equals(addedId)).findFirst();
            if (added.isPresent()) {
                BaseModifiedPair pair = createPair(null, added.get());
                baseModifiedPairs.add(pair);
            }
        }
    }


    private BaseModifiedPair createPair(CheckListItem base, CheckListItem modified) {
        BaseModifiedPair pair = new BaseModifiedPair();
        pair.setBase(base);
        pair.setModified(modified);
        return pair;

    }


    private HistoryChanges buildChange(String type, CheckListItem base, CheckListItem modified) {
        return HistoryChanges
                .builder()
                .type(type)
                .base(base)
                .modified(modified)
                .build();
    }
}
