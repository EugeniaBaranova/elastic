package com.example.elasticsearch.elastic.changesDetecrot;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class HistoryChanges {

    private Object base;

    private Object modified;

    private String type;

}
