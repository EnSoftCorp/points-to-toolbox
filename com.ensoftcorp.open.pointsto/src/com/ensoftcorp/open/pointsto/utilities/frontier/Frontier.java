package com.ensoftcorp.open.pointsto.utilities.frontier;

/**
 * Data structure for maintaining the worklist set of elements to process in a
 * prescribed order.
 * 
 * @author Ben Holland
 * @param <E>
 */
public interface Frontier<E> {

	/**
	 * Returns true if the frontier is not empty, false otherwise
	 * 
	 * @return
	 */
	public boolean hasNext();

	/**
	 * Returns (and removes) the next element from the frontier
	 * 
	 * @return
	 */
	public E next();

	/**
	 * Adds the element to the frontier Returns true if the element was added,
	 * false if the element already existed.
	 * 
	 * @param e
	 * @return
	 */
	public boolean add(E e);

	/**
	 * Returns a string representation of this frontier collection.
	 * 
	 * @return
	 */
	@Override
	public String toString();
}
