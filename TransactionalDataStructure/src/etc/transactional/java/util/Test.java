package etc.transactional.java.util;

import java.util.*;

import etc.transactional.*;

public class Test {
	public static void main(String[] args) {
		var tx = TX.start();
		
		var list = new TArrayList<>();
		
		list.add("one");
		list.add("two");
		log("list", list.size(), list);
		
		tx.commit();
		log("list", list.size(), list);
		
		tx = TX.start();
		
		list.add("one");
		list.add("two");
		log("list", list.size(), list);
		
		tx.rollback();
		log("list", list.size(), list);
		
		tx = TX.start();
		
		list = new TArrayList<>();
		
		list.add("First");
		log("list", list.size(), list);
		
		log("tx2");
		var tx2 = TX.start();
		
		list.add("Second");
		log("list", list.size(), list);
		
		tx2.rollback();
		log("list", list.size(), list);
		
		tx.rollback();
		log("list", list.size(), list);
		
		tx = TX.start();
		
		list.add("me");
		list.add("you");
		log("list", list.size(), list);
		
		tx2 = TX.start();
		list.add("Mine");
		list.add("Yours");
		log("list", list.size(), list);
		
		tx.rollback();
		log("list", list.size(), list);
		try {
			tx2.commit();
			throw new Error();
		}
		catch (IllegalStateException e) {
			// good
		}
		
		tx = TX.start();
		
		list.add("me");
		list.add("you");
		log("list", list.size(), list);
		
		tx2 = TX.start();
		list.add("Mine");
		list.add("Yours");
		log("list", list.size(), list);
		
		tx2.rollback();
		tx2 = TX.start();
		list.add("His");
		list.add("Hers");
		log("list", list.size(), list);
		
		tx.rollback();
		log("list", list.size(), list);
		
		try {
			tx2.commit();
			throw new Error();
		}
		catch (IllegalStateException e) {
			// good
		}
	}
	
	public static void log(Object...objects) {
		System.out.println(Arrays.deepToString(objects));
	}
}
