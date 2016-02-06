package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.utilities.AddressFactory;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.utilities.frontier.FIFOFrontier;
import com.ensoftcorp.open.pointsto.utilities.frontier.Frontier;

import net.ontopia.utils.CompactHashMap;

public class JimplePointsTo extends PointsTo {

	/**
	 * A factory class for producing new unique addresses
	 */
	private AddressFactory addressFactory = new AddressFactory();
	
	/**
	 * Defines the address of the null type
	 */
	private final Long NULL_TYPE_ADDRESS = addressFactory.getNewAddress();
	
	/**
	 * A mapping of addresses to types
	 */
	public CompactHashMap<Long,GraphElement> addressToType = new CompactHashMap<Long,GraphElement>();
	
	/**
	 * A model for the contents of a particular array dimension. The model is
	 * stored as a map of array references to a list of the contents of each
	 * array dimension.
	 * 
	 * Since multi-dimensional arrays are actually arrays of arrays, two new
	 * addresses should be created for "Foo[][] arr = new Foo[x][y]" (one for
	 * each dimension of the array), such that address1->{address2},
	 * address2->{nullType}, and address1 would have propagated from the
	 * "new Foo[x][y]" array instantiation.
	 */
	public CompactHashMap<Long,HashSet<Long>> arrayMemoryModel = new CompactHashMap<Long,HashSet<Long>>();
	
	// a worklist of nodes to propagate information from
	private Frontier<GraphElement> frontier = new FIFOFrontier<GraphElement>();
	
	// a set of assignment relationships
	private Q dataFlowEdges = Common.universe().edgesTaggedWithAny(XCSG.LocalDataFlow, XCSG.InterproceduralDataFlow);

	@Override
	protected void runAnalysis() {
		Q instantiations = Common.universe().nodesTaggedWithAny(XCSG.Instantiation); // new allocations
		AtlasSet<GraphElement> instantiationNodes = instantiations.eval().nodes();

		// create a unique id for each allocation site and add
		// the allocation site to the worklist to propagate 
		// information forward from
		for(GraphElement instantiation : instantiationNodes){
			HashSet<Long> pointsToIds = PointsToAnalysis.getPointsToSet(instantiation);
			pointsToIds.add(addressFactory.getNewAddress());
			frontier.add(instantiation);
		}
		
		// keep propagating allocation ids forward along assignments
		// until there is nothing more to propagate
		while(frontier.hasNext()){
			GraphElement from = frontier.next();
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
			HashSet<Long> fromPointsToSet = PointsToAnalysis.getPointsToSet(from);
			HashSet<Long> toPointsToSet = PointsToAnalysis.getPointsToSet(to);
			// if the to set learned something from the from set,
			// then add the to node to the worklist because it may
			// have something to teach its children
			if(toPointsToSet.addAll(fromPointsToSet)){
				frontier.add(to);
			}
		}
	}
	
}

