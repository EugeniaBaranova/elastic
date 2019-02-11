package com.example.elasticsearch.elastic.changesDetecrot;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class CheckListCustomDetector implements ChangesDetector {
    @Override
    public Set<HistoryChanges> getHistoryChanges(List<CheckListItem> left, List<CheckListItem> right) {

        Set<HistoryChanges> changes = new HashSet<>();
        for(CheckListItem item : left){

            Optional<CheckListItem> first = right.stream().filter(new Predicate<CheckListItem>() {
                @Override
                public boolean test(CheckListItem checkListItem) {
                    return checkListItem.getId().equals(item.getId());
                }
            }).findFirst();

            if(first.isPresent()){
                HistoryChanges changed = getChange(item, first.get(), "CHANGED");
                changes.add(changed);
            }

        }
        return changes;
    }


    private HistoryChanges getChange(CheckListItem base, CheckListItem modified, String type){
        return HistoryChanges.builder().base(base).modified(modified).type(type).build();
    }
}
