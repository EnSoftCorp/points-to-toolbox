package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.utilities.AddressFactory;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.utilities.SubtypeCache;
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
	private CompactHashMap<Long,GraphElement> addressToType = new CompactHashMap<Long,GraphElement>();
	
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
	private CompactHashMap<Long,HashSet<Long>> arrayMemoryModel = new CompactHashMap<Long,HashSet<Long>>();
	
	/*
	 * The underlying data flow graph used to propagate points-to information.
	 * 
	 * Initially this graph consists of intraprocedural and static dispatches
	 * data flow edges, but interprocedural data flow edges will be added to
	 * this graph as dynamic dispatches are resolved forming a dynamic
	 * transitive closure.
	 */
	private Graph dfGraph;
	private AtlasSet<GraphElement> dfNodes = new AtlasHashSet<GraphElement>();
	private AtlasSet<GraphElement> dfEdges = new AtlasHashSet<GraphElement>();
	
	/**
	 * A worklist of nodes containing points-to information to propagate
	 */
	private Frontier<GraphElement> frontier = new FIFOFrontier<GraphElement>();
	
	/**
	 * Cached supertype relations for concrete types
	 */
	private SubtypeCache subtypes;
	

	@Override
	protected void runAnalysis() {
		IProgressMonitor monitor = new org.eclipse.core.runtime.NullProgressMonitor();
		subtypes = new SubtypeCache(monitor);
		seedFrontier();
	}
	
	/**
	 * This method seeds the frontier with null, object, array, primitive and
	 * root set instantiations assigning a unique address to each and storing a
	 * mapping of the address to the Object type.
	 * 
	 * Note: Root set instantiations are the set of instantiations made outside
	 * the observable world such as the arguments array passed to a main method
	 * or any library methods that we are not analyzing.
	 */
	private void seedFrontier() {
		// set the first address (address 0) to be the null type
		GraphElement nullType = Common.universe().nodesTaggedWithAny(XCSG.Java.NullType).eval().nodes().getFirst();
		addressToType.put(NULL_TYPE_ADDRESS, nullType);
		
		// TODO: consider primitives and String literals
		
		// TODO: consider root set objects
		
		// create unique addresses for types of new statements and array instantiations
		Q newRefs = Common.universe().nodesTaggedWithAny(XCSG.Instantiation, XCSG.ArrayInstantiation);
		for(GraphElement newRef : newRefs.eval().nodes()){
			GraphElement statedType = PointsToAnalysis.statedType(newRef);
			if(statedType != null){
				// create a new address for the reference and add a  
				// mapping from the address to the state type
				Long address = addressFactory.getNewAddress();
				PointsToAnalysis.getPointsToSet(newRef).add(address);
				addressToType.put(address, statedType);
				// if this is an array instantiation then we should create an
				// array memory model and addresses for array memory references
				// of each array dimension of the array
				if(statedType.hasAttr(Node.DIMENSION)){
					GraphElement arrayType = statedType;
					int arrayDimension = (int) arrayType.getAttr(Node.DIMENSION);
					GraphElement arrayElementType = Common.universe().edgesTaggedWithAny(XCSG.ArrayElementType).successors(Common.toQ(arrayType)).eval().nodes().getFirst();
					// the top dimension has already been addressed, so start at dimension minus 1
					for(int i=arrayDimension-1; i>0; i--){
						// map array dimension address to array dimension type
						Long arrayDimensionAddress = addressFactory.getNewAddress();
						arrayType = PointsToAnalysis.getArrayTypeForDimension(arrayElementType, i);
						addressToType.put(arrayDimensionAddress, arrayType);
						// map address containing dimension to array dimension, address1 -> set: { address2 }
						HashSet<Long> arrayValueAddresses = new HashSet<Long>();
						arrayValueAddresses.add(arrayDimensionAddress);
						arrayMemoryModel.put(address, arrayValueAddresses);
						// update the current address to the next array level
						address = arrayDimensionAddress;
					}
					// map lowest level of arrayAddress -> set: { NULL_TYPE_ADDRESS }
					// since array contents are initialized to null by default
					HashSet<Long> arrayValueAddresses = new HashSet<Long>();
					arrayValueAddresses.add(NULL_TYPE_ADDRESS);
					arrayMemoryModel.put(address, arrayValueAddresses);
				}
				frontier.add(newRef);
			} else {
				if(newRef.tags().contains(XCSG.ArrayInstantiation)){
					Log.warning("No stated type during initialization for Array: " + newRef.address().toAddressString());
				} else {
					Log.warning("No stated type during initialization for Object: " + newRef.address().toAddressString());
				}
			}
		}
	}
	
}

