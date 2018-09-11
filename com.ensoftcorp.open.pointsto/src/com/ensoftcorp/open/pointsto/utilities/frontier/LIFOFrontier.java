package com.ensoftcorp.open.pointsto.utilities.frontier;

import java.util.LinkedHashSet;

/**
 * A Last In First Out based worklist
 * 
 * @author Ben Holland
 *
 * @param <E>
 */
public class LIFOFrontier<E> implements Frontier<E> {

	// reference: https://stackoverflow.com/a/14024061/475329
	class CachedLinkedHashSet<F> extends LinkedHashSet<F> {
		private static final long serialVersionUID = 1L;
		private F last = null;
	    
		public CachedLinkedHashSet(int initialCapacity, float f) {
			super(initialCapacity, f);
		}
	    
		public CachedLinkedHashSet() {
			super();
		}
		
		public boolean add(F f) {
	        last = f;
	        return super.add(f);
	    }
		
	    public F getLast() {
	        return last;
	    }

	    public F removeLast() {
	    	remove(last);
	        return last;
	    }
	}
	
	private CachedLinkedHashSet<E> frontier;

	/**
	 * Creates a new frontier
	 */
	public LIFOFrontier() {
		frontier = new CachedLinkedHashSet<E>();
	}

	/**
	 * Creates a new frontier with the given initial capacity
	 * 
	 * @param initialCapacity
	 */
	public LIFOFrontier(int initialCapacity) {
		frontier = new CachedLinkedHashSet<E>(initialCapacity, 0.75f);
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
	 * Returns (and removes) the next element from the frontier in Last in First out (LIFO) order
	 * 
	 * @return
	 */
	public E next() {
		return frontier.removeLast();
	}

	/**
	 * Adds the element to the end of frontier, if the element already exists
	 * the frontier is left unchanged. Returns true if the element was added,
	 * false if the element already existed.
	 * 
	 * @param e
	 * @return
	 */
	public boolean add(E e) {
		return frontier.add(e);
	}

	/**
	 * Returns the number of elements in this frontier set (its cardinality).
	 * 
	 * @return
	 */
	public long size() {
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
