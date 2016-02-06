package com.ensoftcorp.open.pointsto.utilities.frontier;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A Least Recently Used based worklist
 * 
 * @author Ben Holland
 *
 * @param <E>
 */
public class LRUFrontier<E> implements Frontier<E> {

	private LinkedHashSet<E> frontier;

	/**
	 * Creates a new frontier
	 */
	public LRUFrontier() {
		frontier = new LinkedHashSet<E>();
	}

	/**
	 * Creates a new frontier with the given initial capacity
	 * 
	 * @param initialCapacity
	 */
	public LRUFrontier(int initialCapacity) {
		frontier = new LinkedHashSet<E>(initialCapacity, 0.75f);
	}

	/**
	 * Returns true if the frontier is not empty, false otherwise
	 * 
	 * @return
	 */
	public boolean hasNext() {
		return !frontier.isEmpty();
	}

	/**
	 * Returns (and removes) the next element from the frontier in Least
	 * Recently Used (LRU) order
	 * 
	 * @return
	 */
	public E next() {
		Iterator<E> iterator = frontier.iterator();
		E next = iterator.next();
		iterator.remove();
		return next;
	}

	/**
	 * Adds the element to the frontier, moving the element to the end of the
	 * frontier if it already is contained in the frontier. Returns true if the
	 * element was added, false if the element already existed.
	 * 
	 * @param e
	 * @return
	 */
	public boolean add(E e) {
		boolean present = frontier.remove(e);
		frontier.add(e);
		return present;
	}

	/**
	 * Returns the number of elements in this frontier set (its cardinality).
	 * 
	 * @return
	 */
	public int size() {
		return frontier.size();
	}

	/**
	 * Returns a string representation of this frontier collection. The string
	 * representation consists of a list of the collection's elements in the
	 * order they are returned by its iterator, enclosed in square brackets
	 * ("[]"). Adjacent elements are separated by the characters ", " (comma and
	 * space). Elements are converted to strings as by String.valueOf(Object).
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		return frontier.toString();
	}

}
