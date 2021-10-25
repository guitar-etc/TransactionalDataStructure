package etc.transactional;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

public class TX implements AutoCloseable {
	private static LinkedList<TX> stack = new LinkedList<>();
	
	/**
	 * At first, I planned to lock each object whenever they are used.
	 * However, when I started planning for multi-thread support, I realized that 2 threads can easily go into a dead-lock situation.
	 * 
	 * Also, if a transaction, A, is going to read certain object and a transaction that started after A, B, wants to write to the object before A reaches to the "Read", 
	 * it's hard to decide if B has to wait for A. This is because the locks aren't imposed upfront but whenever an object is accessed.
	 * 
	 * For now, lock the entire Heap with a write lock regardless of read or write.
	 */
	private static ReadWriteLock lock = new ReentrantReadWriteLock();
	
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
	
	private TX() {
//		threadId = Thread.currentThread().getId();
	}
	
	public static void start(Consumer<TX> cons) {
		start(null, cons);
	}
	
	/**
	 * If the parent tx was rolled back, we want to roll back the creation of child transaction, thus we want to roll back the child transaction.
	 * If the parent tx was committed, it's up to the child transaction whether it will be committed or rolled back.
	 *
	 * 
	 * @param parentTx
	 * @param cons
	 */
	public static void start(TX parent, Consumer<TX> cons) {
		TX _tx = null;
		try (TX tx = new TX()) {
			_tx = tx;
			lock.writeLock().lock();
			stack.push(tx);
			
			if (parent != null) {
				parent.addOnRollback(() -> {
					tx.cancel();
				});
			}
			if (!tx.isCancelled) {
				cons.accept(tx);
			}
		}
		// currently no 2 transactions can run simultaneously because I'm locking the entire Heap.
		// In order to lock less, tx need to know all the locks it needs to acquire and acquire them upon start.
		// Otherwise, 2 tx's can easily go into a deadlock or the data can be corrupted.
		// So, TXCancelledException should technically never be thrown.
		catch (TXCancelledException e) {
			if (e.getTx() != _tx) {
				throw new Error("_tx: " + _tx, e);
			}
		}
		finally {
			lock.writeLock().unlock();
		}
	}
	
	private void setToDone() {
		isDone = true;
	}
	
	/**
	 * release
	 */
	private void onDone() {
		stack.pop();
		lockedObjects = null;
		onCommits = null;
		onRollbacks = null;
		setToDone();
	}

	@Override
	public void close() {
		checkCancelled();
		if (!isDone) {
			rollback();
		}
	}
	
	public IdentityHashMap<Object, Object> lockedObjects = new IdentityHashMap<>();
	private boolean isDone = false;
	
	private List<Runnable> onCommits = new ArrayList<>();
	private boolean isCommitted = false;
	public void addOnCommit(Runnable run) {
//		checkDone();
		checkCancelled();
		if (run != null) {
			if (onCommits != null) {
				onCommits.add(run);
			}
			else if (isCommitted) {
				run.run();
			}
		}
	}
	public void commit() {
		commit(false);
	}
	public void commit(boolean fromParent) {
//		checkDone();
		checkCancelled();
		if (!fromParent) {
			preDone(tx -> tx.commit(true));
		}
		for (var run : onCommits) {
			run.run();
		}
		isCommitted = true;
		onDone();
	}

	private List<Runnable> onRollbacks = new ArrayList<>();
	private boolean isRolledBack = false;
	public void addOnRollback(Runnable run) {
//		checkDone();
		checkCancelled();
		if (run != null) {
			if (onRollbacks != null) {
				onRollbacks.add(run);
			}
			else if (isRolledBack) {
				run.run();
			}
		}
	}
	public void rollback() {
		rollback(false);
	}
	public void rollback(boolean fromParent) {
//		checkDone();
		checkCancelled();
		if (!fromParent) {
			preDone(tx -> tx.rollback(true));
		}
		for (var run : onRollbacks) {
			run.run();
		}
		isRolledBack = true;
		onDone();
	}
	
//	private void checkDone() {
//		if (isDone) {
//			throw new IllegalStateException("This tx is done. Please let this get gc'ed and start a new one");
//		}
//	}
	
	private void checkCancelled() {
		if (isCancelled) {
			throw new TXCancelledException(this);
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
	
	private boolean isCancelled = false;
	public void cancel() {
		checkCancelled();
		isCancelled = true;
	}

	@Override
	public String toString() {
		return "TX [lockedObjects=" + lockedObjects + ", isDone=" + isDone + ", onCommits=" + onCommits
				+ ", isCommitted=" + isCommitted + ", onRollbacks=" + onRollbacks + ", isRolledBack=" + isRolledBack
				+ ", isCancelled=" + isCancelled + "]";
	}

//	private long threadId;
//	public boolean isForCurrentThread() {
//		checkCancelled();
//		return Thread.currentThread().getId() == threadId;
//	}
}
