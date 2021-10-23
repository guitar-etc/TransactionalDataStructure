package etc.transactional.java.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import etc.transactional.*;

/**
 * Naive implementation that copies the original state
 * @author pycs9
 *
 * @param <E>
 */

public class TArrayList<E> extends ArrayList<E> {
	private static final long serialVersionUID = 1L;
	private ArrayList<E> impl = new ArrayList<>();
	
	@SuppressWarnings("unchecked")
	private ArrayList<E> impl() {
		var tx = TX.getOngoing();
		if (tx != null) {
			var txLocal = (ArrayList<E>)tx.lockedObjects.get(this);
			
			if (txLocal != null) {
				return txLocal;
			}
			else {
				TX closestTx = TX.getClosestWith(this);
				if (closestTx == null) {
					txLocal = (ArrayList<E>)impl.clone();
				}
				else {
					txLocal = (ArrayList<E>) ((ArrayList<E>)closestTx.lockedObjects.get(this)).clone();
				}
				tx.lockedObjects.put(this, txLocal);
				
				var f_txLocal = txLocal;
				tx.addOnCommit(() -> {
					impl = f_txLocal;
				});
				return txLocal;
			}
		}
		else {
			return impl;
		}
	}
	
	public void trimToSize() {
		impl().trimToSize();
	}
	
	public void ensureCapacity(int minCapacity) {
		impl().ensureCapacity(minCapacity);
	}

	public int size() {
		return impl().size();
	}

	public boolean isEmpty() {
		return impl().isEmpty();
	}

	public boolean containsAll(Collection<?> c) {
		return impl().containsAll(c);
	}

	public boolean contains(Object o) {
		return impl().contains(o);
	}

	public int indexOf(Object o) {
		return impl().indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return impl().lastIndexOf(o);
	}

	public Object clone() {
		return impl().clone();
	}

	public Object[] toArray() {
		return impl().toArray();
	}

	public <T> T[] toArray(T[] a) {
		return impl().toArray(a);
	}

	public String toString() {
		return impl().toString();
	}

	public E get(int index) {
		return impl().get(index);
	}

	public E set(int index, E element) {
		return impl().set(index, element);
	}

	public boolean add(E e) {
		return impl().add(e);
	}

	public void add(int index, E element) {
		impl().add(index, element);
	}

	public E remove(int index) {
		return impl().remove(index);
	}

	public boolean equals(Object o) {
		return impl().equals(o);
	}

	public <T> T[] toArray(IntFunction<T[]> generator) {
		return impl().toArray(generator);
	}

	public int hashCode() {
		return impl().hashCode();
	}

	public boolean remove(Object o) {
		return impl().remove(o);
	}

	public void clear() {
		impl().clear();
	}

	public boolean addAll(Collection<? extends E> c) {
		return impl().addAll(c);
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		return impl().addAll(index, c);
	}

	public boolean removeAll(Collection<?> c) {
		return impl().removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return impl().retainAll(c);
	}

	public ListIterator<E> listIterator(int index) {
		return impl().listIterator(index);
	}

	public ListIterator<E> listIterator() {
		return impl().listIterator();
	}

	public Iterator<E> iterator() {
		return impl().iterator();
	}

	public Stream<E> stream() {
		return impl().stream();
	}

	public Stream<E> parallelStream() {
		return impl().parallelStream();
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return impl().subList(fromIndex, toIndex);
	}

	public void forEach(Consumer<? super E> action) {
		impl().forEach(action);
	}

	public Spliterator<E> spliterator() {
		return impl().spliterator();
	}

	public boolean removeIf(Predicate<? super E> filter) {
		return impl().removeIf(filter);
	}

	public void replaceAll(UnaryOperator<E> operator) {
		impl().replaceAll(operator);
	}

	public void sort(Comparator<? super E> c) {
		impl().sort(c);
	}
}