package com.ensoftcorp.open.pointsto.utilities;

import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.open.pointsto.common.Constants;

/**
 * Utilities for assisting in the computation of points-to sets
 * 
 * @author Ben Holland
 */
public class PointsToAnalysis {

	/**
	 * Gets or creates the points to set for a graph element.
	 * Returns a reference to the points to set so that updates to the 
	 * set will also update the set on the graph element.
	 * @param ge
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static HashSet<Long> getPointsToSet(GraphElement ge){
		if(ge.hasAttr(Constants.POINTS_TO_SET)){
			return (HashSet<Long>) ge.getAttr(Constants.POINTS_TO_SET);
		} else {
			HashSet<Long> pointsToIds = new HashSet<Long>();
			ge.putAttr(Constants.POINTS_TO_SET, pointsToIds);
			return pointsToIds;
		}
	}
	
}
