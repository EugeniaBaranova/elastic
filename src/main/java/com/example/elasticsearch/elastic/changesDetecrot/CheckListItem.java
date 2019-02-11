package com.example.elasticsearch.elastic.changesDetecrot;

import lombok.*;
import org.javers.core.metamodel.annotation.Id;

@Getter
@Setter
@ToString
@Builder
public class CheckListItem {
    @Id
    private String id;

    private String text;

    private boolean checked;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckListItem item = (CheckListItem) o;
        if (id != null ? !id.equals(item.id) : item.id != null) return false;

        if(checked != item.checked
                || text == null
                || item.text == null
                || !text.equals(item.getText())) return false;

        return true;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (checked ? 1 : 0);
        return result;
    }
}
