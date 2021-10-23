package etc.transactional;

import java.util.*;
import java.util.function.*;

public class TX implements AutoCloseable {
	private static LinkedList<TX> stack = new LinkedList<>();
	
	public static TX getOngoing() {
		return stack.peek();
	}
	
	public static TX getClosestWith(Object obj) {
		for (TX tx : stack) {
			if (tx.lockedObjects.containsKey(obj)) {
				return tx;
			}
		}
		return null;
	}
	
	public static TX start() {
		var tx = new TX();
		stack.push(tx);
		return tx;
	}
	
	public IdentityHashMap<Object, Object> lockedObjects = new IdentityHashMap<>();
	private boolean isDone = false;
	
	private List<Runnable> onCommits = new ArrayList<>();
	public void addOnCommit(Runnable run) {
		checkDone();
		if (run != null) {
			onCommits.add(run);
		}
	}
	public void commit() {
		commit(false);
	}
	public void commit(boolean fromParent) {
		checkDone();
		if (!fromParent) {
			preDone(tx -> tx.commit(true));
		}
		for (var run : onCommits) {
			run.run();
		}
		onDone();
	}

	private List<Runnable> onRollbacks = new ArrayList<>();
	public void addOnRollback(Runnable run) {
		checkDone();
		if (run != null) {
			onRollbacks.add(run);
		}
	}
	public void rollback() {
		rollback(false);
	}
	public void rollback(boolean fromParent) {
		checkDone();
		if (!fromParent) {
			preDone(tx -> tx.rollback(true));
		}
		for (var run : onRollbacks) {
			run.run();
		}
		onDone();
	}
	
	private void checkDone() {
		if (isDone) {
			throw new IllegalStateException("This tx is done. Please let this get gc'ed and start a new one");
		}
	}
	
	private void preDone(Consumer<TX> cons) {
		TX tx = getOngoing();
		int count = stack.size(); // sanity check to prevent infinite loop
		while (tx != this && count > 0) { // parent transaction is about to be ended. end the child transactions in the same way.
			cons.accept(tx);
			tx = getOngoing();
			count--;
		}
	}
	
	/**
	 * release
	 */
	private void onDone() {
		stack.pop();
		lockedObjects = null;
		onCommits = null;
		onRollbacks = null;
		isDone = true;
	}

	@Override
	public void close() throws Exception {
		rollback();
	}
}
