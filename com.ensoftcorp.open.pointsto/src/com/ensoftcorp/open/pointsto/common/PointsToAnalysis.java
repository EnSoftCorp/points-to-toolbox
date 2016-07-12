package com.ensoftcorp.open.pointsto.common;

import java.util.ArrayList;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.Common;

/**
 * Some defined attributes and tags exposed by the analysis
 * 
 * @author Ben Holland
 */
public class PointsToAnalysis {

	/**
	 * The tag prefix of points-to addresses An "address" is an abstract concept
	 * the corresponds to a unique allocation. References with the same
	 * points-to address may be aliases of each other.
	 * 
	 * For convenience this tag is also applied to array components to indicate
	 * the array address. An array component will contain a single address
	 * corresponding to a unique array instantiation.
	 */
	public static final String POINTS_TO_PREFIX = "POINTS_TO_";
	
	/**
	 * This attribute is placed on array instantiations to hold a serialized
	 * form of the addresses used to model an array's memory. An array is
	 * assigned an address for each of its dimensions.
	 */
	public static final String ARRAY_MEMORY_MODEL = "ARRAY_MEMORY_MODEL";

	/**
	 * Applied to edges to indicate that the edge's runtime possibility was
	 * verified by the points-to analysis
	 */
	public static final String INFERRED = "INFERRED";

	/**
	 * Returns nodes with the same points-to address tags as the given node
	 * @param node
	 * @return
	 */
	public static AtlasSet<Node> getAliases(Node node){
		Q aliases = Common.universe().nodesTaggedWithAny(getPointsToTags(node));
		return new AtlasHashSet<Node>(aliases.eval().nodes());
	}
	
	public static AtlasSet<Node> getArrayMemoryAliases(Node arrayInstantiation){
		Q aliases = Common.universe().nodesTaggedWithAny(getArrayMemoryPointsToTags(arrayInstantiation));
		return new AtlasHashSet<Node>(aliases.eval().nodes());
	}
	
	public static String[] getArrayMemoryPointsToTags(Node arrayInstantiation){
		ArrayList<String> tags = new ArrayList<String>();
		String s = arrayInstantiation.getAttr(ARRAY_MEMORY_MODEL).toString();
		s = s.substring(1, s.length()-2);
		String[] addresses = s.split(",");
		for(String address : addresses){
			tags.add(POINTS_TO_PREFIX + address);
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}
	
	/**
	 * Returns an array of points-to tags applied to the given node
	 * @param node
	 * @return
	 */
	public static String[] getPointsToTags(Node node){
		ArrayList<String> tags = new ArrayList<String>();
		for(String tag : node.tags()){
			if(tag.startsWith(POINTS_TO_PREFIX)){
				tags.add(tag);
			}
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}

}
