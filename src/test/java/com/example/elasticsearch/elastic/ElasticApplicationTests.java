package com.example.elasticsearch.elastic;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.map.EntryAdded;
import org.javers.core.metamodel.annotation.Id;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ElasticApplicationTests {

	@Getter
	@Setter
	@EqualsAndHashCode
	@Builder
	static class Item {
		@Id
		private String id;
		private String value;
		private boolean check;
	}

	public static void detectChanges(List<Item> left, List<Item> right){

		for(Item item : left){
			Optional<Item> changedItem = right.stream().filter(item1 -> item1.getId().equals(item)).findFirst();
			if(changedItem.isPresent()){
				registerChanges(item, changedItem.get());
			}else {



			}



		}



	}

	private static void registerChanges(Item before, Item after) {

	}


	public static void main(String[] args) {


		Javers javers = JaversBuilder.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.SIMPLE)
				.build();


		Item item1 = Item.builder().id("itemid1").check(false).value("value_1").build();

		Item item2 = Item.builder().id("itemid2").check(false).value("value_1").build();

		Item item3 = Item.builder().id("itemid3").check(false).value("value_1").build();

		Item item4 = Item.builder().id("itemid4").check(true).value("value_1").build();

		Item item5 = Item.builder().id("itemid1").check(true).value("value_1").build();

		Item item6 = Item.builder().id("itemid6").check(true).value("value_1").build();

		Item item7 = Item.builder().id("itemid7").check(true).value("value_3").build();

		Item item8 = Item.builder().id("itemid8").check(true).value("value_1").build();

		Item item9 = Item.builder().id("itemid9").check(true).value("value_1").build();



		List<Item> left = Arrays.asList(item1, item2, item3, item4);

		List<Item> right = Arrays.asList(item5,item6,item7,item8);

		Diff diff = javers.compareCollections(left, right, Item.class);


		List<Change> changes = diff.getChanges();

		for(Change change : changes){

			if(change instanceof ObjectRemoved){

				System.out.println("Removed. "+change);
			}else if(change instanceof NewObject){

				System.out.println("Added new Object "+change);
			}else {
				System.out.println("Обьект был изменен");
			}

		}


	}

}

