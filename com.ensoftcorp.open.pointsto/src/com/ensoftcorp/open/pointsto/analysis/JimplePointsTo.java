package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;
import java.util.LinkedList;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;

public class JimplePointsTo extends PointsTo {

	private int id = 0;
	
	// a set of assignment relationships
	private Q dataFlowEdges = Common.universe().edgesTaggedWithAny(XCSG.LocalDataFlow, XCSG.InterproceduralDataFlow);
	
	// a worklist of nodes to propagate information from
	private LinkedList<GraphElement> worklist = new LinkedList<GraphElement>();

	@Override
	protected void runAnalysis() {
		Q instantiations = Common.universe().nodesTaggedWithAny(XCSG.Instantiation); // new allocations
		AtlasSet<GraphElement> instantiationNodes = instantiations.eval().nodes();

		// create a unique id for each allocation site and add
		// the allocation site to the worklist to propagate 
		// information forward from
		for(GraphElement instantiation : instantiationNodes){
			HashSet<Integer> pointsToIds = PointsToAnalysis.getPointsToSet(instantiation);
			pointsToIds.add(id++);
			worklist.add(instantiation);
		}
		
		// keep propagating allocation ids forward along assignments
		// until there is nothing more to propagate
		while(!worklist.isEmpty()){
			GraphElement from = worklist.removeFirst();
			propagatePointsTo(from);
		}
	}
	
	/**
	 * Propagates points-to information forward from the given node
	 * to all nodes that this node is directly assigned to. Each node
	 * that receives new information is added back to the worklist
	 * @param from
	 */
	private void propagatePointsTo(GraphElement from){
		AtlasSet<GraphElement> toNodes = dataFlowEdges.successors(Common.toQ(from)).eval().nodes();
		for(GraphElement to : toNodes){
			HashSet<Integer> fromPointsToSet = PointsToAnalysis.getPointsToSet(from);
			HashSet<Integer> toPointsToSet = PointsToAnalysis.getPointsToSet(to);
			// if the to set learned something from the from set,
			// then add the to node to the worklist because it may
			// have something to teach its children
			if(toPointsToSet.addAll(fromPointsToSet)){
				worklist.add(to);
			}
		}
	}
	
}

