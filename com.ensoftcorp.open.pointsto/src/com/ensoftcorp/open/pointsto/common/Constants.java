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
	
}
