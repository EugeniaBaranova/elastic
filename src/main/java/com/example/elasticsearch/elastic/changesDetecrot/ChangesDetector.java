package com.example.elasticsearch.elastic.changesDetecrot;

import java.util.List;
import java.util.Set;

public interface ChangesDetector {

    Set<HistoryChanges> getHistoryChanges(List<CheckListItem> left, List<CheckListItem> right);

}
