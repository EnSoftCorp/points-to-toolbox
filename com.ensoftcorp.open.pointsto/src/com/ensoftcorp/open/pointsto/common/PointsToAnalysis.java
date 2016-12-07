package com.ensoftcorp.open.pointsto.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.java.core.script.Common;

/**
 * Some defined attributes and tags exposed by the analysis
 * 
 * @author Ben Holland
 */
public class PointsToAnalysis {

	/**
	 * The tag prefix of points-to addresses. An "address" is an abstract concept
	 * the corresponds to a unique allocation. References with overlapping
	 * points-to addresses may be aliases of each other. References with only
	 * the same alias must be aliases of each other. References without any
	 * commons points-to address must not be aliases of each other.
	 * 
	 * For convenience this tag is also applied to array components to indicate
	 * the array address. An array component will contain a single address
	 * corresponding to a unique array instantiation.
	 */
	public static final String ALIAS_PREFIX = "ALIAS_";
	
	/**
	 * An alias to null
	 * This is reserved to replace "address" <ALIAS_PREFIX + 0>
	 */
	public static final String NULL_ALIAS = "NULL_ALIAS";
	
	/**
	 * This tag is placed on array instantiations to hold a serialized
	 * form of the addresses used to model an array's memory. An array is
	 * assigned an address for each of its dimensions.
	 */
	public static final String ARRAY_MEMORY_MODEL_PREFIX = "ARRAY_MEMORY_MODEL_";

	/**
	 * An uninitialized array model
	 * This is reserved to replace "address" <ARRAY_MEMORY_MODEL_PREFIX + 0>
	 */
	public static final String NULL_ARRAY_MEMORY_MODEL = "NULL_ARRAY_MEMORY_MODEL";
	
	/**
	 * Returns true if the alias is an array memory model
	 * Does not consider null aliases
	 * @param address
	 * @return
	 */
	public static boolean isArrayMemoryModelAlias(String aliasTag){
		if(aliasTag.equals(NULL_ARRAY_MEMORY_MODEL) || aliasTag.equals(NULL_ALIAS)){
			return false;
		} else if(!aliasTag.startsWith(ALIAS_PREFIX)){
			throw new IllegalArgumentException(aliasTag + " is not a valid alias tag.");
		} else {
			try {
				Integer address = Integer.parseInt(aliasTag.replace(ALIAS_PREFIX, ""));
				return isArrayMemoryModelAddress(address);
			} catch (Exception e){
				throw new IllegalArgumentException(aliasTag + " is not a valid alias tag.");
			}
		}
	}
	
	/**
	 * Returns true if the address is an array memory model
	 * @param address
	 * @return
	 */
	private static boolean isArrayMemoryModelAddress(Integer address) {
		if(address == 0){
			return true;
		} else {
			return !Common.universe().nodesTaggedWithAny((ARRAY_MEMORY_MODEL_PREFIX + address), NULL_ARRAY_MEMORY_MODEL)
				.eval().nodes().isEmpty();
		}
	}
	
	public static boolean notAliases(Node ref1, Node ref2){
		return !mayAlias(ref1, ref2);
	}
	
	/**
	 * Checks if the two given references may be aliases of each other
	 * @param ref1
	 * @param ref2
	 * @return
	 */
	public static boolean mayAlias(Node ref1, Node ref2){
		HashSet<String> ref1AliasTags = new HashSet<String>();
		for(String aliasTag : getAliasTags(ref1)){
			ref1AliasTags.add(aliasTag);
		}
		for(String aliasTag : getAliasTags(ref2)){
			if(ref1AliasTags.contains(aliasTag)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if the two given references must be aliases of each other
	 * @param ref1
	 * @param ref2
	 * @return
	 */
	public static boolean mustAlias(Node ref1, Node ref2){
		String[] ref1AliasTags = getAliasTags(ref1);
		Arrays.sort(ref1AliasTags);
		String[] ref2AliasTags = getAliasTags(ref2);
		Arrays.sort(ref2AliasTags);
		if(ref1AliasTags.length == ref2AliasTags.length){
			for(int i=0; i<ref1AliasTags.length; i++){
				if(!ref1AliasTags[i].equals(ref2AliasTags[i])){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Applied to edges to indicate that the edge's runtime data flow possibility was
	 * verified by the points-to analysis
	 */
	public static final String INFERRED_DATA_FLOW = "INFERRED_DATA_FLOW";
	
	/**
	 * Applied to edges to indicate that the edge's runtime type of possibility was
	 * verified by the points-to analysis
	 */
	public static final String INFERRED_TYPE_OF = "INFERRED_TYPE_OF";
	
	/**
	 * Returns aliases to the array memory model
	 * @param arrayInstantiation
	 * @return
	 */
	public static AtlasSet<Node> getArrayMemoryModelAliases(Node arrayInstantiation){
		String[] tags = getArrayMemoryModelAliasTags(arrayInstantiation);
		if(tags.length == 0){
			return new AtlasHashSet<Node>();
		} else {
			Q aliases = Common.universe().nodesTaggedWithAny(tags);
			return new AtlasHashSet<Node>(aliases.eval().nodes());
		}
	}
	
	/**
	 * Returns tags of aliases to the array memory model
	 * 
	 * @param arrayInstantiation
	 * @return
	 */
	public static String[] getArrayMemoryModelAliasTags(Node arrayInstantiation){
		ArrayList<String> tags = new ArrayList<String>();
		if(arrayInstantiation.taggedWith(XCSG.ArrayInstantiation)){
			for(String tag : arrayInstantiation.tags()){
				if(tag.equals(NULL_ARRAY_MEMORY_MODEL)){
					tags.add(NULL_ALIAS);
				} else if(tag.startsWith(ARRAY_MEMORY_MODEL_PREFIX)){
					tags.add(tag.replace(ARRAY_MEMORY_MODEL_PREFIX, ALIAS_PREFIX));
				}
			}
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}
	
	/**
	 * Returns tags denoting the array memory models represented by the given
	 * array instantiation
	 */
	public static String[] getArrayMemoryModelTags(Node arrayInstantiation){
		ArrayList<String> tags = new ArrayList<String>();
		if(arrayInstantiation.taggedWith(XCSG.ArrayInstantiation)){
			for(String tag : arrayInstantiation.tags()){
				if(tag.startsWith(ARRAY_MEMORY_MODEL_PREFIX) || tag.equals(NULL_ARRAY_MEMORY_MODEL)){
					tags.add(tag);
				}
			}
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}
	
	/**
	 * Returns nodes with the same points-to address tags as the given node
	 * @param node
	 * @return
	 */
	public static Q getAliases(Node node){
		String[] tags = getAliasTags(node);
		if(tags.length == 0){
			return Common.empty();
		} else {
			Q aliases = Common.universe().nodesTaggedWithAny(tags);
			return aliases;
		}
	}
	
	/**
	 * Returns an array of points-to tags applied to the given node
	 * @param node
	 * @return
	 */
	public static String[] getAliasTags(Node node){
		ArrayList<String> tags = new ArrayList<String>();
		for(String tag : node.tags()){
			if(tag.startsWith(ALIAS_PREFIX) || tag.equals(NULL_ALIAS)){
				tags.add(tag);
			}
		}
		String[] result = new String[tags.size()];
		result = tags.toArray(result);
		return result;
	}

}
