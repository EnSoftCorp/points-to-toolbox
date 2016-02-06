package com.ensoftcorp.open.pointsto.utilities;

import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
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
	
	/**
	 * Given an object reference, return the stated type of that reference.
	 * 
	 * @param ge
	 * @return
	 */
	public static GraphElement statedType(GraphElement ge){
		return Common.universe().edgesTaggedWithAny(XCSG.TypeOf).successors(Common.toQ(ge)).eval().nodes().getFirst();
	}
	
	/**
	 * Given an array element type, return the array type for the given dimension
	 * @param arrayElementType
	 * @param dimension
	 * @return
	 */
	public static GraphElement getArrayTypeForDimension(GraphElement arrayElementType, int dimension){
		Q arrayTypes = Common.universe().edgesTaggedWithAny(XCSG.ArrayElementType).predecessors(Common.toQ(arrayElementType));
		return arrayTypes.selectNode(Node.DIMENSION, dimension).eval().nodes().getFirst();
	}
	
}
