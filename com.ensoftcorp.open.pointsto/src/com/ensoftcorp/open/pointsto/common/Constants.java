package com.ensoftcorp.open.pointsto.common;

/**
 * Some defined attributes and tags used by the points-to analyses
 * 
 * @author Ben Holland
 */
public class Constants {

	/**
	 * Attribute key name for node points-to sets
	 */
	public static final String POINTS_TO_SET = "points-to";
	
	/**
	 * Applied to edges to indicate that the edge's runtime possibility was
	 * verified by the points-to analysis
	 */
	public static final String INFERRED = "inferred";

	/**
	 * A convenience attribute that is applied to array components to indicate
	 * the array address This tag is applied during graph enhancements. An array
	 * component will contain a single address corresponding to a unique array
	 * instantiation. This means it is safe to select array components using
	 * selectNode(POINTS_TO_ARRAY_ADDRESS, address).
	 */
	public static final String POINTS_TO_ARRAY_ADDRESS = "points-to-array-address";
	
}
