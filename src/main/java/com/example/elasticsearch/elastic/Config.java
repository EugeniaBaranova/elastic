package com.example.elasticsearch.elastic;

import com.example.elasticsearch.elastic.changesDetecrot.ChangesDetector;
import com.example.elasticsearch.elastic.changesDetecrot.CheckListChangesDetector;
import com.example.elasticsearch.elastic.changesDetecrot.CheckListCustomDetector;
import com.example.elasticsearch.elastic.changesDetecrot.CheckListItem;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.ListCompareAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {


    @Bean
    public Javers javers() {
        return JaversBuilder.javers()
/*
                .registerValue(CheckListItem.class, (a, b) -> a.getId().equals(b.getId()) && (a.isChecked() == b.isChecked() || a.getText().equals(b.getText())))
*/
                .withListCompareAlgorithm(ListCompareAlgorithm.SIMPLE)
                .build();
    }

    @Bean
    public ChangesDetector checkListChangeDetector() {
        return new CheckListChangesDetector(javers());
    }


    @Bean
    public ChangesDetector checkListCustomDetector(){
        return new CheckListCustomDetector();
    }
}
