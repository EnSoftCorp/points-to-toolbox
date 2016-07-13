package com.ensoftcorp.open.pointsto.utilities;

import java.util.HashSet;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.index.Index;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.analysis.PointsTo;
import com.ensoftcorp.open.pointsto.common.PointsToAnalysis;

/**
 * Utilities for making enhancements to the Atlas graph based on points-to results
 * 
 * @author Ben Holland
 */
public class GraphEnhancements {
	
	public static long tagInferredEdges(PointsTo pointsTo){
		long numInferredEdges = 0;
		// blessing interprocedural invocation data flow edges with inferred tag
		for(Edge dfEdge : new AtlasHashSet<Edge>(pointsTo.getInferredDataFlowGraph().edges())){
			dfEdge.tag(PointsToAnalysis.INFERRED);
			numInferredEdges++;
		}
		return numInferredEdges;
	}
	
	public static long serializeArrayMemoryModels(PointsTo pointsTo){
		long numMemoryModels = 0;
		Q arrayInstantiations = Common.universe().nodesTaggedWithAny(XCSG.ArrayInstantiation);
		for(Node arrayInstantiation : arrayInstantiations.eval().nodes()){
			// should only have one alias address on the instantiation
			for(Integer address : pointsTo.getAliasAddresses(arrayInstantiation)){
				HashSet<Integer> arrayMemoryModel = pointsTo.getArrayMemoryModel(address);
				for(Integer arrayMemoryModelAddress : arrayMemoryModel){
					arrayInstantiation.tag(PointsToAnalysis.ARRAY_MEMORY_MODEL_PREFIX + arrayMemoryModelAddress);
					numMemoryModels++;
				}
			}
		}
		return numMemoryModels;
	}
	
	public static long rewriteArrayComponents(PointsTo pointsTo){
		arrayNumber = 1;
		// first delete all array components
		AtlasSet<Node> arrayComponents = Common.resolve(new NullProgressMonitor(), Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents)).eval().nodes();
		for(GraphElement arrayComponent : arrayComponents){
			Graph.U.delete(arrayComponent);
		}
		
		// get all the array reads and writes that could potentially be matched
		Q addressedObjects = Common.toQ(pointsTo.getAddressedNodes());
		AtlasSet<Node> arrayReads = Common.resolve(new NullProgressMonitor(), addressedObjects.nodesTaggedWithAny(XCSG.ArrayRead)).eval().nodes();
		AtlasSet<Node> arrayWrites = Common.resolve(new NullProgressMonitor(), addressedObjects.nodesTaggedWithAny(XCSG.ArrayWrite)).eval().nodes();
		Q arrayIdentityForEdges = Common.universe().edgesTaggedWithAny(XCSG.ArrayIdentityFor);
		
		// create a new array component for each array instantiation
		Q arrayInstantiations = Common.universe().nodesTaggedWithAny(XCSG.ArrayInstantiation);
		for(Node arrayInstantiation : arrayInstantiations.eval().nodes()){
			for(Integer address : pointsTo.getAliasAddresses(arrayInstantiation)){
				findOrCreateArrayComponent(pointsTo, address);
			}
		}

		// connect each array write to an array component
		for(Node arrayWrite : arrayWrites){
			Node array = arrayIdentityForEdges.predecessors(Common.toQ(arrayWrite)).eval().nodes().getFirst();
			for(Integer address : pointsTo.getAliasAddresses(array)){
				GraphElement arrayComponent = findOrCreateArrayComponent(pointsTo, address);
				// create interprocedural data flow edge from array write node to array component node
				GraphElement arrayWriteEdge = Graph.U.createEdge(arrayWrite, arrayComponent);
				arrayWriteEdge.tag(XCSG.InterproceduralDataFlow);
				arrayWriteEdge.tag(Index.INDEX_VIEW_TAG);
				arrayWriteEdge.tag(PointsToAnalysis.INFERRED);
				Graph.U.addEdge(arrayWriteEdge);
			}
		}
		
		// connect each array component to an array read
		for(Node arrayRead : arrayReads){
			Node array = arrayIdentityForEdges.predecessors(Common.toQ(arrayRead)).eval().nodes().getFirst();
			for(Integer address : pointsTo.getAliasAddresses(array)){
				Node arrayComponent = findOrCreateArrayComponent(pointsTo, address);
				// create interprocedural data flow edge from array component node to array read node
				GraphElement arrayReadEdge = Graph.U.createEdge(arrayComponent, arrayRead);
				arrayReadEdge.tag(XCSG.InterproceduralDataFlow);
				arrayReadEdge.tag(Index.INDEX_VIEW_TAG);
				arrayReadEdge.tag(PointsToAnalysis.INFERRED);
				Graph.U.addEdge(arrayReadEdge);
			}
		}
		return arrayNumber-1;
	}
	
	private static int arrayNumber = 1;
	
	private static Node findOrCreateArrayComponent(PointsTo pointsTo, Integer address){
		Q addressedObjects = Common.toQ(pointsTo.getAddressedNodes());
		AtlasSet<Node> arrayComponents = addressedObjects.nodesTaggedWithAny(XCSG.ArrayComponents).eval().nodes();
		for(Node arrayComponent : arrayComponents){
			if(pointsTo.getAliasAddresses(arrayComponent).contains(address)){
				return arrayComponent;
			}
		}
		// no array component exists for address, so create array component node
		Node arrayComponent = Graph.U.createNode();
		arrayComponent.tag(XCSG.ArrayComponents);
		arrayComponent.tag(Index.INDEX_VIEW_TAG);
		arrayComponent.putAttr(XCSG.name, "@[" + (arrayNumber++) + "]");
		arrayComponent.tag(PointsToAnalysis.POINTS_TO_PREFIX + address);
		return arrayComponent;
	}

	/**
	 * Converts temporary sets to tags and attributes
	 * @return 
	 */
	public static long serializeAliases(PointsTo pointsTo) {
		long numAliasesTags = 0;
		AtlasSet<Node> addressedObjects = pointsTo.getAddressedNodes();
		for(Node addressedObject : addressedObjects){
			HashSet<Integer> pointsToSet = pointsTo.getAliasAddresses(addressedObject);
			for(Integer address : pointsToSet){
				addressedObject.tag(PointsToAnalysis.POINTS_TO_PREFIX + address);
				numAliasesTags++;
			}
		}
		return numAliasesTags;
	}
	
}
