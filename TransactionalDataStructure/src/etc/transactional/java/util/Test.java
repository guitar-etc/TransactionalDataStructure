package etc.transactional.java.util;

import java.util.*;

import etc.transactional.*;

import static java.util.Arrays.*;

public class Test {
	public static void main(String[] args) throws InterruptedException {
//		test_single_thread();
		test_multi_thread();
	}
	
	static void hold(long mili) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Tests that later transaction waits for prior transaction to end before starting.
	static void test_multi_thread() throws InterruptedException {
		var list = new TArrayList<>();
		int run1WaitTime = 10;
		long[] start = {System.currentTimeMillis()};
		long[] run2EndTime = {-1};
		boolean[] run2DoCommit = {false};
		
		Runnable run1 = () -> {
			TX.start(tx -> {
				hold(run1WaitTime);
				list.add("run1");
//				log(list);
			});
		};
		
		Runnable run2 = () -> {
			TX.start(tx -> {
				list.add("run2");
//				log(list);
//				log(System.currentTimeMillis() - start[0] > run1WaitTime);
				run2EndTime[0] = System.currentTimeMillis();
				if (run2DoCommit[0]) {
					tx.commit();
				}
			});
		};
		
		Thread th1 = new Thread(run1);
		Thread th2 = new Thread(run2);
		
		th1.start();
		hold(run1WaitTime / 10);
		th2.start();
		
		th1.join();
		th2.join();
		log(run2EndTime[0] - start[0] > run1WaitTime);
		log(list.isEmpty());
		
		start[0] = System.currentTimeMillis();
		th1 = new Thread(run1);
		th2 = new Thread(run2);
		run2DoCommit[0] = true;
		
		th2.start();
		hold(run1WaitTime / 10);
		th1.start();
		
		th2.join();
		log(run2EndTime[0] - start[0] < run1WaitTime);
		log(list.equals(asList("run2")));
		
		th1.join();
		log(list.equals(asList("run2")));
		
		list.clear();
		
		
		TX.start(tx -> {
			list.add("First");
			Runnable run3 = () -> {
				TX.start(
						tx, tx2 -> {
					hold(run1WaitTime);
					list.add("run3");
					log(list);
				});
			};
			Thread th3 = new Thread(run3);
			th3.start();
			list.add("Second");
			log(list);
		});
		log(list.isEmpty(), list);
	}
	
	public static void test_single_thread() {
		final var list = new TArrayList<>();
		TX.start(tx -> {
			list.add("one");
			list.add("two");
			log(asList("one", "two").equals(list));
			
			tx.commit();
			log(asList("one", "two").equals(list));
		});
		
		
		TX.start(tx -> {
			list.add("one");
			list.add("two");
			log(asList("one", "two", "one", "two").equals(list));
			
			tx.rollback();
			log(asList("one", "two").equals(list));
		});
		
		var list2 = new TArrayList<>();
		TX.start(tx -> {
			list2.add("First");
			log(asList("First").equals(list2));
			
			TX.start(tx2 -> {
				list2.add("Second");
				log(asList("First", "Second").equals(list2));
				
				tx2.rollback();
				log(asList("First").equals(list2));
				
				tx.rollback();
				log(asList().equals(list2));
			});
		});
		
		TX.start(tx -> {
			list2.add("me");
			list2.add("you");
			log(asList("me", "you").equals(list2));
			TX.start(tx2 -> {
				list2.add("Mine");
				list2.add("Yours");
				log(asList("me", "you", "Mine", "Yours").equals(list2));
				
				tx.rollback();
				log(asList().equals(list2));
				
				try {
					tx2.commit();
					throw new Error();
				}
				catch (IllegalStateException e) {
					// good
				}
			});
		});
		
		TX.start(tx -> {
			list2.add("me");
			list2.add("you");
			log(asList("me", "you").equals(list2));
			
			TX.start(tx2 -> {
				list2.add("Mine");
				list2.add("Yours");
				log(asList("me", "you", "Mine", "Yours").equals(list2));
				
				tx2.rollback();
			});
			
			TX.start(tx2 -> {
				list2.add("His");
				list2.add("Hers");
				log(asList("me", "you", "His", "Hers").equals(list2));
				
				tx.rollback();
				log(asList().equals(list2));
				
				try {
					tx2.commit();
					throw new Error();
				}
				catch (IllegalStateException e) {
					// good
				}
			});
		});
		
		
		var temp = new TArrayList<>();
		TX.start(tx -> {
			temp.add("Not empty");
		});
		log(temp.isEmpty());
	}
	
	public static void log(Object...objects) {
		System.out.println(Arrays.deepToString(objects));
	}
}
