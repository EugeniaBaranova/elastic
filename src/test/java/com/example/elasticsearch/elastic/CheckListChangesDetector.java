package com.example.elasticsearch.elastic;

import com.example.elasticsearch.elastic.changesDetecrot.ChangesDetector;
import com.example.elasticsearch.elastic.changesDetecrot.CheckListItem;
import com.example.elasticsearch.elastic.changesDetecrot.HistoryChanges;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;

@ContextConfiguration(classes = {Config.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class CheckListChangesDetector {

    @Autowired
    @Qualifier("checkListChangeDetector")
    private ChangesDetector detector;


    @Test
    public void first() {
        Assert.assertNotNull(detector);
    }


    @Test
    public void checkOnOneElementTest() {

        CheckListItem itemBefore = CheckListItem.builder().id("1").text("value1").checked(false).build();

        CheckListItem itemAfter = CheckListItem.builder().id("1").text("value1").checked(true).build();

        long before = System.currentTimeMillis();
        List<HistoryChanges> historyChanges = new ArrayList<>(detector.getHistoryChanges(Arrays.asList(itemBefore), Arrays.asList(itemAfter)));
        long after = System.currentTimeMillis();
        System.out.println(after - before);

        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(1));
        Assert.assertThat(historyChanges.get(0).getType(), is("CHANGED"));
        Assert.assertNotNull(historyChanges.get(0).getBase());
        Assert.assertNotNull(historyChanges.get(0).getModified());
    }


    @Test
    public void checkMoreThanOneElementTest() {

        CheckListItem itemBefore1 = CheckListItem.builder().id("1").text("value1").checked(false).build();
        CheckListItem itemBefore2 = CheckListItem.builder().id("2").text("value1").checked(false).build();

        CheckListItem itemAfter1 = CheckListItem.builder().id("1").text("value1").checked(true).build();
        CheckListItem itemAfter2 = CheckListItem.builder().id("2").text("value3").checked(true).build();

        List<HistoryChanges> historyChanges = new ArrayList<>(detector.getHistoryChanges(Arrays.asList(itemBefore1, itemBefore2),
                Arrays.asList(itemAfter1, itemAfter2)));

        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(2));
        Assert.assertThat(historyChanges.get(0).getType(), is("CHANGED"));
        Assert.assertNotNull(historyChanges.get(0).getBase());
        Assert.assertNotNull(historyChanges.get(0).getModified());

        Assert.assertThat(historyChanges.get(1).getType(), is("CHANGED"));
        Assert.assertNotNull(historyChanges.get(1).getBase());
        Assert.assertNotNull(historyChanges.get(1).getModified());
    }


    @Test
    public void removeOneElementTest() {
        CheckListItem itemBefore = CheckListItem.builder().id("1").text("value1").checked(false).build();

        List<HistoryChanges> historyChanges = new ArrayList<>(detector.getHistoryChanges(Arrays.asList(itemBefore), new ArrayList<>()));
        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(1));
        Assert.assertThat(historyChanges.get(0).getType(), is("REMOVED"));
        Assert.assertNotNull(historyChanges.get(0).getBase());
        Assert.assertNotNull(historyChanges.get(0).getModified());
    }


    @Test
    public void renamedOneElementTest() {
        CheckListItem itemBefore = CheckListItem.builder().id("1").text("value1").checked(false).build();

        CheckListItem itemAfter = CheckListItem.builder().id("1").text("value_23").checked(false).build();

        List<HistoryChanges> historyChanges = new ArrayList<>(detector.getHistoryChanges(Arrays.asList(itemBefore), Arrays.asList(itemAfter)));

        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(1));
        Assert.assertThat(historyChanges.get(0).getType(), is("CHANGED"));
        Assert.assertNotNull(historyChanges.get(0).getBase());
        Assert.assertNotNull(historyChanges.get(0).getModified());
        Assert.assertThat(historyChanges.get(0).getBase(), is(itemBefore));
        Assert.assertThat(historyChanges.get(0).getModified(), is(itemAfter));

    }


    @Test
    public void removedAllAddedNewTest() {
        CheckListItem itemBefore1 = CheckListItem.builder().id("1").text("value1").checked(false).build();
        CheckListItem itemBefore2 = CheckListItem.builder().id("2").text("value12").checked(false).build();
        CheckListItem itemBefore3 = CheckListItem.builder().id("3").text("value12").checked(false).build();

        CheckListItem itemAfter1 = CheckListItem.builder().id("4").text("value1").checked(true).build();
        CheckListItem itemAfter2 = CheckListItem.builder().id("5").text("value12").checked(true).build();
        CheckListItem itemAfter3 = CheckListItem.builder().id("6").text("value12").checked(true).build();

        List<CheckListItem> left = Arrays.asList(itemBefore1, itemBefore2, itemBefore3);

        List<CheckListItem> right = Arrays.asList(itemAfter1, itemAfter2, itemAfter3);

        long before = System.currentTimeMillis();
        Set<HistoryChanges> historyChanges = detector.getHistoryChanges(left, right);
        long after = System.currentTimeMillis();
        System.out.println(after - before);
        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(9));

    }


    @Test
    public void removeAddChangeTest() {
        CheckListItem itemBefore1 = CheckListItem.builder().id("1").text("value1").checked(false).build();
        CheckListItem itemBefore2 = CheckListItem.builder().id("2").text("value12").checked(false).build();
        CheckListItem itemBefore3 = CheckListItem.builder().id("3").text("value12").checked(false).build();


        CheckListItem itemAfter1 = CheckListItem.builder().id("1").text("value1").checked(true).build();
        CheckListItem itemAfter2 = CheckListItem.builder().id("4").text("value1_2").checked(true).build();
        CheckListItem itemAfter3 = CheckListItem.builder().id("3").text("value12").checked(true).build();
        CheckListItem itemAfter4 = CheckListItem.builder().id("5").text("value1_2").checked(true).build();


        List<CheckListItem> left = Arrays.asList(itemBefore1, itemBefore2, itemBefore3);

        List<CheckListItem> right = Arrays.asList(itemAfter1, itemAfter2, itemAfter3, itemAfter4);

        Set<HistoryChanges> historyChanges = detector.getHistoryChanges(left, right);
        Assert.assertNotNull(historyChanges);
        Assert.assertThat(historyChanges.size(), is(7));
    }


}
