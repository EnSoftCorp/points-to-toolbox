package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Address;
import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.java.commons.analysis.CommonQueries;
import com.ensoftcorp.open.pointsto.log.Log;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;
import com.ensoftcorp.open.pointsto.utilities.AddressFactory;
import com.ensoftcorp.open.pointsto.utilities.AnalysisUtilities;
import com.ensoftcorp.open.pointsto.utilities.SubtypeCache;
import com.ensoftcorp.open.pointsto.utilities.frontier.FIFOFrontier;
import com.ensoftcorp.open.pointsto.utilities.frontier.Frontier;
import com.ensoftcorp.open.pointsto.utilities.frontier.LIFOFrontier;
import com.ensoftcorp.open.pointsto.utilities.frontier.LRUFrontier;

import net.ontopia.utils.CompactHashMap;

/**
 * A fixed point points-to analysis for Jimple
 * 
 * @author Ben Holland
 */
public class JimplePointsTo extends PointsTo {

	/**
	 * Attribute key name for node points-to sets
	 */
	private static final String POINTS_TO_SET = "jimple-points-to-set";

	/**
	 * Gets or creates the points to set for a graph element.
	 * Returns a reference to the points to set so that updates to the 
	 * set will also update the set on the graph element.
	 * @param ge
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	private static HashSet<Integer> getPointsToSet(Node node){
		if(node.hasAttr(POINTS_TO_SET)){
			return (HashSet<Integer>) node.getAttr(POINTS_TO_SET);
		} else {
			HashSet<Integer> pointsToIds = new HashSet<Integer>();
			node.putAttr(POINTS_TO_SET, pointsToIds);
			return pointsToIds;
		}
	}
	
	@Override
	public void dispose(){
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		isDisposed = true;
		// remove the temporary attributes
		AtlasSet<Node> addressedObjects = new AtlasHashSet<Node>(Query.universe().selectNode(POINTS_TO_SET).eval().nodes());
		while(!addressedObjects.isEmpty()){
			Node addressedObject = addressedObjects.one();
			addressedObject.removeAttr(POINTS_TO_SET);
			addressedObjects.remove(addressedObject);
		}
	}
	
	@Override
	public void addAliasAddress(Node node, Integer address) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		getPointsToSet(node).add(address);
	}

	@Override
	public void removeAliasAddress(Node node, Integer address) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		getPointsToSet(node).remove(address);
	}
	
	@Override
	public HashSet<Integer> getAliasAddresses() {
		HashSet<Integer> result = new HashSet<Integer>();
		for(int i=NULL_TYPE_ADDRESS; i<=addressFactory.peekCurrentAddress(); i++) {
			result.add(i);
		}
		return result;
	}
	
	@Override
	public HashSet<Integer> getAliasAddresses(Node node) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		HashSet<Integer> result = new HashSet<Integer>();
		result.addAll(getPointsToSet(node));
		return result;
	}
	
	/**
	 * A factory class for producing new unique addresses
	 */
	private final AddressFactory addressFactory = new AddressFactory();
	
	/**
	 * Defines the address of the null type
	 */
	private final Integer NULL_TYPE_ADDRESS = addressFactory.getNewAddress();
	
	/**
	 * A convenience mapping of the addresses to instantiations
	 */
	private final CompactHashMap<Integer,Node> addressToInstantiation = new CompactHashMap<Integer,Node>();

	@Override
	public AtlasSet<Node> getAddressedNodes() {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		return new AtlasHashSet<Node>(Query.universe().selectNode(POINTS_TO_SET).eval().nodes());
	}
	
	@Override
	public HashSet<Integer> getArrayMemoryModelAliases(Integer address) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		HashSet<Integer> result = new HashSet<Integer>();
		result.addAll(arrayMemoryModel.get(address));
		return result;
	}

	@Override
	public HashSet<Integer> getArrayMemoryModels() {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		HashSet<Integer> result = new HashSet<Integer>();
		result.addAll(arrayMemoryModel.keySet());
		return result;
	}
	
	@Override
	public Node getInstantiation(Integer address) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		return addressToInstantiation.get(address);
	}

	@Override
	public Node getType(Integer address) {
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		return addressToType.get(address);
	}
	
	/**
	 * A convenience mapping of addresses to types
	 */
	private final CompactHashMap<Integer,Node> addressToType = new CompactHashMap<Integer,Node>();
	
	/**
	 * A worklist of nodes containing points-to information to propagate
	 */
	private final Frontier<Node> frontier;
	
	/**
	 * A progress monitor for use with resolving sets
	 */
	private final IProgressMonitor monitor = new org.eclipse.core.runtime.NullProgressMonitor();
	
	/**
	 * Cached supertype relations for concrete types
	 */
	private final SubtypeCache subtypes = new SubtypeCache(monitor);
	
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
	private final CompactHashMap<Integer,HashSet<Integer>> arrayMemoryModel = new CompactHashMap<Integer,HashSet<Integer>>();

	/*
	 * The underlying data flow graph used to propagate points-to information.
	 * 
	 * Initially this graph consists of intraprocedural and static dispatches
	 * data flow edges, but interprocedural data flow edges will be added to
	 * this graph as dynamic dispatches are resolved forming a dynamic
	 * transitive closure.
	 */
	private Graph dfGraph;
	private AtlasSet<Node> dfNodes = new AtlasHashSet<Node>();
	private AtlasSet<Edge> dfEdges = new AtlasHashSet<Edge>();
	
	public JimplePointsTo() {
		if(PointsToPreferences.isPointsToAnalysisFIFOFrontierMode()) {
			frontier = new FIFOFrontier<Node>();
			if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Points-to Analysis Frontier Mode: FIFO");
		} else if(PointsToPreferences.isPointsToAnalysisLIFOFrontierMode()) {
			frontier = new LIFOFrontier<Node>();
			if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Points-to Analysis Frontier Mode: LIFO");
		} else if(PointsToPreferences.isPointsToAnalysisLRUFrontierMode()) {
			frontier = new LRUFrontier<Node>();
			if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Points-to Analysis Frontier Mode: LRU");
		} else { 
			// default
			frontier = new FIFOFrontier<Node>();
			if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Points-to Analysis Frontier Mode: FIFO");
		}
	}
	
	@Override
	public Graph getInferredDataFlowGraph() {
		return dfGraph;
	}
	
	/**
	 * This method seeds the frontier with null, object, array, primitive and
	 * external root set instantiations assigning a unique address to each and
	 * storing a mapping of the address to the Object type.
	 * 
	 * Note: Root set instantiations are the set of instantiations made outside
	 * the observable world such as the arguments array passed to a main method
	 * or any library methods that we are not analyzing.
	 */
	private void seedFrontier() {
		// set the first address (address 0) to be the null type
		// technically there is no instantiation of null, but it can be useful to treat is as one
		Node nullType = Query.universe().nodes(XCSG.Java.NullType).eval().nodes().one();
		addressToInstantiation.put(NULL_TYPE_ADDRESS, nullType);
		addressToType.put(NULL_TYPE_ADDRESS, nullType);
		
		// TODO: consider external root set objects
		
		// note: enum constants are instantiated in jimple in the <clinit> method, but in source it is implied
		// create unique addresses for types of new statements and array instantiations
		Q newRefs = Query.universe().nodes(XCSG.Instantiation, XCSG.ArrayInstantiation);
		if(PointsToPreferences.isTrackPrimitivesEnabled()){
			Q literalInstantiations = Query.universe().nodes(XCSG.Literal);
			newRefs = newRefs.union(literalInstantiations);
		}
		
		AtlasSet<Node> newRefNodes = newRefs.eval().nodes();
		long newRefNodesSize = newRefNodes.size();
		long newRefNodesSeeded = 0;
		
		for(Node newRef : newRefNodes){
			if(PointsToPreferences.isGeneralLoggingEnabled() && System.currentTimeMillis()-lastUpdateTime > PointsTo.UPDATE_INTERVAL) {
				Log.info("Seeding Frontier: " + newRefNodesSeeded + "/" + newRefNodesSize);
				lastUpdateTime = System.currentTimeMillis();
			}
			
			Node statedType = AnalysisUtilities.statedType(newRef);
			if(statedType != null){
				// create a new address for the reference and add a  
				// mapping from the address to the state type
				Integer address = NULL_TYPE_ADDRESS;
				if(!statedType.equals(nullType)){
					address = addressFactory.getNewAddress();
				}
				getPointsToSet(newRef).add(address);
				serializeAlias(newRef, address);
				addressToInstantiation.put(address, newRef);
				addressToType.put(address, statedType);
				
				// if this is an array instantiation then we should create an
				// array memory model and addresses for array memory references
				// of each array dimension of the array
				if(PointsToPreferences.isArrayComponentTrackingEnabled() && statedType.hasAttr(Attr.Node.DIMENSION)){
					Node arrayType = statedType;
					int arrayDimension = (int) arrayType.getAttr(Attr.Node.DIMENSION);
					Node arrayElementType = Query.universe().edges(XCSG.ArrayElementType).successors(Common.toQ(arrayType)).eval().nodes().one();
					// the top dimension has already been addressed, so start at dimension minus 1
					for(int i=arrayDimension-1; i>0; i--){
						// map array dimension address to array dimension type
						Integer arrayDimensionAddress = addressFactory.getNewAddress();
						addressToInstantiation.put(arrayDimensionAddress, newRef);
						arrayType = AnalysisUtilities.getArrayTypeForDimension(arrayElementType, i);
						addressToType.put(arrayDimensionAddress, arrayType);
						// map address containing dimension to array dimension, address1 -> set: { address2 }
						HashSet<Integer> arrayValueAddresses = new HashSet<Integer>();
						arrayValueAddresses.add(arrayDimensionAddress);
						arrayMemoryModel.put(address, arrayValueAddresses);
						// update the current address to the next array level
						address = arrayDimensionAddress;
					}
					// map lowest level of arrayAddress -> set: { NULL_TYPE_ADDRESS }
					// since array contents are initialized to null by default
					HashSet<Integer> arrayValueAddresses = new HashSet<Integer>();
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
		
		if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Tracking " + newRefNodesSize + " object aliases.");
	}

	/**
	 * This methods runs the fixed point points-to analysis algorithm for
	 * Jimple.
	 * 
	 * First the frontier worklist is seeded with unique addresses of root
	 * instantiations then that information is iteratively propagate until a
	 * fixed point (no new points-to information can be transferred) is reached.
	 * 
	 * During the iterative propagation the following actions may occur.
	 * 
	 * 1) When new type-compatible points-to information has been transfered the
	 * frontier next is updated with nodes to propagate information from in the
	 * iteration
	 * 
	 * 2) If a dynamic dispatch becomes resolvable, add the resolved data flow
	 * edge to the underlying data flow graph and use the updated data flow
	 * graph in the next iteration
	 * 
	 * 3) When points-to information is propagated to an array write, propagate
	 * the type-compatible points-to information to the corresponding array
	 * reads (based on the array aliases)
	 * 
	 * 4) When points-to information is propagated to an array reference
	 * (perhaps revealing a new array alias), re-propagate information from the
	 * corresponding array write to array reads based on the updated array
	 * aliasing.
	 */
	@Override
	protected void runAnalysis() {
		lastUpdateTime = System.currentTimeMillis();
		
		// seed the frontier with the set of instantiations
		seedFrontier();
		
		// create the initial underlying data flow graph that will get dynamically
		// updated as new dynamic dispatches are resolved
		Q conservativeDF = AnalysisUtilities.getConservativeDataFlow(monitor);
		dfEdges = new AtlasHashSet<Edge>();
		dfEdges.addAll(conservativeDF.eval().edges());
		dfNodes = new AtlasHashSet<Node>();
		dfNodes.addAll(conservativeDF.eval().nodes());
		dfGraph = new UncheckedGraph(dfNodes, dfEdges);
		
		// model primitive valueOf dataflow method summaries
		if(PointsToPreferences.isTrackPrimitivesEnabled()){
			dfEdges.addAll(modelPrimitiveInstantiationDataFlows());
			dfGraph = new UncheckedGraph(dfNodes, dfEdges);
		}
		
		if(PointsToPreferences.isCollapsingSCCsEnabled()) dfGraph = collapseSCCs(dfGraph);
		
		// create graphs and sets for resolving dynamic dispatches
		AtlasHashSet<Node> dynamicCallsiteThisSet = AnalysisUtilities.getDynamicCallsiteThisSet(monitor);
		Graph dfInvokeThisGraph = Common.resolve(monitor, Query.universe().edges(XCSG.IdentityPassedTo)).eval();
		Graph methodSignatureGraph = Common.resolve(monitor, Query.universe().edges(XCSG.InvokedFunction, XCSG.InvokedSignature)).eval();

		// create graphs for performing array analysis
		Q arrayAccess = Query.universe().nodes(XCSG.ArrayAccess);
		Q arrayIdentityFor = Query.universe().edges(XCSG.ArrayIdentityFor);
		AtlasSet<Node> arrayReferences = arrayIdentityFor.predecessors(arrayAccess).eval().nodes();
		
		// iteratively propagate points-to information until a fixed point is reached
		long iteration = 0;
		while(frontier.hasNext()){
			if(PointsToPreferences.isGeneralLoggingEnabled() && System.currentTimeMillis()-lastUpdateTime > PointsTo.UPDATE_INTERVAL) {
				Log.info("Propagating Points-to Sets: Iteration: " + iteration + ", Frontier Size: " + frontier.size());
				lastUpdateTime = System.currentTimeMillis();
			}
			
			// remove the next node from the frontier to start propagating type information from
			Node from = frontier.next();
			AtlasSet<Edge> outEdges = dfGraph.edges(from, NodeDirection.OUT);

			// propagate points-to information aInteger each outgoing data flow edge
			for(Edge edge : outEdges){
				Node to = edge.to();

				// transfer type-compatible points-to information from the "from" node to the "to" node
				if(transferTypeCompatibleAddresses(from, to)){
					// if we transfered a new address from the "from" node to
					// the "to" node, then add the "to" node to the frontier for
					// the next iteration since it may have new points-to
					// information to communicate to others
					frontier.add(to);
					
					// check if we need to update the data flow graph for the next iteration
					if(dynamicCallsiteThisSet.contains(to)){
						// if we've just added new runtime types to a callsite
						// "this", then add new data flow edge possibilities for
						// any newly resolved dynamic dispatches
						
						// ASSERT: "this." corresponds to exactly one callsite
						assert(dfInvokeThisGraph.edges(to, NodeDirection.OUT).size() == 1);
						
						// get the callsite, method signature, and runtime types
						Node callsite = dfInvokeThisGraph.edges(to, NodeDirection.OUT).one().to();
						Node methodSignature = methodSignatureGraph.edges(callsite, NodeDirection.OUT).one().to();
						
						AtlasSet<Node> runtimeTypes = new AtlasHashSet<Node>();
						for(Integer address : getPointsToSet(to)){
							runtimeTypes.add(addressToType.get(address));
						}
						
						// resolve any potential dynamic dispatches
						AtlasSet<Node> resolvedDispatches = CommonQueries.dynamicDispatch(Common.toQ(runtimeTypes), Common.toQ(methodSignature)).eval().nodes();
						AtlasSet<Node> signatureSet = AnalysisUtilities.getSignatureSet(resolvedDispatches);
						
						// ASSERT: only DF_INTERPROCEDURAL edges have a CALL_SITE_ID
						// Legacy Comment: Tom says blame Jeremias if this isn't true :P
						Address csid = (Address) callsite.getAttr(Attr.Node.CALL_SITE_ID);
	
						// for each resolved dispatch we need to update the data flow graph for the next iteration
						for(Edge resolvedEdge : Graph.U.edges().filter(Attr.Edge.CALL_SITE_ID, csid)){
							if(signatureSet.contains(resolvedEdge.to()) || signatureSet.contains(resolvedEdge.from())){
								// add the edge and update the node sets if the edge doesn't already exist in the graph
								if(dfEdges.add(resolvedEdge)){
									// add node endpoints for the edge to graph to keep graph well-formed
									Node dest = resolvedEdge.to();
									Node origin = resolvedEdge.from();
									dfNodes.add(dest);
									dfNodes.add(origin);
									
									// technically we don't have to update the dfGraph reference because updating the dfNodes and dfEdges
									// sets has already changed the dfGraph because UncheckedGraph does not make copies.  This was done
									// for readability and as a guard in case the Atlas implementation changes in the future
									dfGraph = new UncheckedGraph(dfNodes, dfEdges);
									
									// if edge is between two object references and the origin has type
									// information that the dest does not have, add origin to the frontier
									HashSet<Integer> originAddresses = getPointsToSet(origin);
									HashSet<Integer> destAddresses = getPointsToSet(dest);
									if(!destAddresses.containsAll(originAddresses)){
										frontier.add(origin);
									}
								}
							}
						}
					}
					
					// if we hit an array write, new values need to be added to the array memory model
					if(PointsToPreferences.isArrayComponentTrackingEnabled() && to.tags().contains(XCSG.ArrayWrite)){
						// "to" node is an array write so propagate addresses
						// from array write to the corresponding array reads
						updateArrayMemoryModels(to);
					} 
					
					if(PointsToPreferences.isArrayComponentTrackingEnabled() && arrayReferences.contains(to)){
						// "to" node is an array write reference, so it may be possible to transfer 
						// new addresses of array values from new array writes to corresponding array 
						// reads or array writes to new array reads, reads will be added to the frontier
						// if new information is available to propagate
						for(Node arrayWrite : AnalysisUtilities.getArrayWriteAccessesForArrayReference(to)){
							updateArrayMemoryModels(arrayWrite);
						}
						
						// "to" node is an array read reference, so we may need to read out values of
						// the array memory model and propagate them onward by adding the array reads
						// to the frontier
						for(Node arrayRead : AnalysisUtilities.getArrayReadAccessesForArrayReference(to)){
							for(Integer arrayReferenceAddress : getPointsToSet(to)){
								try {
									HashSet<Integer> arrayMemoryModelAddresses = arrayMemoryModel.get(arrayReferenceAddress);
									if(getPointsToSet(arrayRead).addAll(arrayMemoryModelAddresses)){
										for(Integer arrayMemorymodelAddress : arrayMemoryModelAddresses) {
											serializeAlias(arrayRead, arrayMemorymodelAddress);
										}
										frontier.add(arrayRead);
									}
								} catch (Exception e){
									// TODO: this case is known to occur when a reference is casted to an array type
									// there should be a better way to deal with this in place of skipping it and moving on
									// also skipping this case will likely cause a null alias warning later
									Log.warning("Points-to set does not exist for array read: " + arrayRead.address().toAddressString());
								}
							}
						}
					}
				}
			}
			iteration++;
		}
		
		if(PointsToPreferences.isCollapsingSCCsEnabled()) dfGraph = expandSCCs(dfGraph);
	}
	
	/**
	 * Creates local data flow edges from valueOf arguments to the master return for primitive types
	 * @return
	 */
	private AtlasHashSet<Edge> modelPrimitiveInstantiationDataFlows() {
		Log.info("Adding model summaries for primitive type instantiations...");
		AtlasHashSet<Edge> summaryEdges = new AtlasHashSet<Edge>();
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Byte", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Character", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Short", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Integer", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Long", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Float", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Double", "valueOf")));
		summaryEdges.addAll(createDataFlowSummaryEdges(Common.methodSelect("java.lang", "Boolean", "valueOf")));
		return summaryEdges;
	}

	/**
	 * Creates a local data flow edge between each parameter and the master return of the given methods
	 * @param method
	 * @return
	 */
	private AtlasHashSet<Edge> createDataFlowSummaryEdges(Q methods) {
		AtlasHashSet<Edge> summaryEdges = new AtlasHashSet<Edge>();
		for(Node method : methods.eval().nodes()){
			Q parameters = Common.toQ(method).contained().nodes(XCSG.Parameter);
			Q returnValues = Common.toQ(method).contained().nodes(XCSG.ReturnValue);
			for(Node parameter : parameters.eval().nodes()){
				for(Node returnValue : returnValues.eval().nodes()){
					Edge summaryEdge = Graph.U.createEdge(parameter, returnValue);
					summaryEdge.tag(XCSG.LocalDataFlow);
					summaryEdge.tag("PRIMITIVE_DATA_FLOW_SUMMARY");
					summaryEdges.add(summaryEdge);
				}
			}
		}
		return summaryEdges;
	}
	
	/**
	 * Transfers type-compatible addresses from a data flow node to a data flow
	 * node. An address is compatible if the address's stated type exists in the
	 * subtype hierarchy of the data flow node that points-to information is
	 * being transfered to.
	 * 
	 * @param from Data flow node to transfer addresses from
	 * @param to Data flow node to transfer addresses to
	 * @return Returns true iff new addresses were transfered, false otherwise
	 */
	private boolean transferTypeCompatibleAddresses(Node from, Node to){
		boolean toReceivedNewAddresses = false;
		HashSet<Integer> fromAddresses = getPointsToSet(from);
		HashSet<Integer> toAddresses = getPointsToSet(to);		
		// need to check type compatibility
		Node toStatedType = AnalysisUtilities.statedType(to);
		if(toStatedType != null){
			// if the from type is compatible with the compatible to type set, add it
			for(Integer fromAddress : fromAddresses){
				Node addressType = addressToType.get(fromAddress);
				if (subtypes.isSubtypeOf(addressType, toStatedType)) {
					toReceivedNewAddresses |= toAddresses.add(fromAddress);
					serializeAlias(to, fromAddress);
				}
			}
		} else {
			// DEBUG: show(Common.toQ(com.ensoftcorp.atlas.core.db.graph.Graph.U.nodes().getAt(java.lang.Integer.valueOf(<address>, 16).IntegerValue())))
			Log.warning("No stated type during transfer for ref: " + to.address().toAddressString());
		}
		
		return toReceivedNewAddresses;
	}
	
	/**
	 * Let AW be an array write, AR be an array read. Let REFR be an array
	 * reference corresponding to an AR, and REFW be an array reference
	 * corresponding to an AW. For the given AW, propagate addresses from AW to
	 * AR for each REFR that has an address that matches an address in REFW's
	 * addresses.
	 * 
	 * @param arrayWrite
	 */
	private void updateArrayMemoryModels(Node arrayWrite) {
		try {
			// for each REFW corresponding to the AW
			for(Node arrayWriteReference : AnalysisUtilities.getArrayReferencesForArrayAccess(arrayWrite)){
				// for each REFW address
				for(Integer arrayWriteReferenceAddress : getPointsToSet(arrayWriteReference)){
					// add the AW addresses to the the array memory values set for the array REFW address
					if(arrayMemoryModel.get(arrayWriteReferenceAddress).addAll(getPointsToSet(arrayWrite))){
						// if new addresses were added to the array, propagate them to the corresponding reads
						Q allArrayReads = Query.universe().nodes(XCSG.ArrayRead);
						Q allArrayReadReferences = Query.universe().edges(XCSG.ArrayIdentityFor).predecessors(allArrayReads);
						for(Node arrayReadReference : allArrayReadReferences.eval().nodes()){
							HashSet<Integer> arrayReadReferenceAddresses = getPointsToSet(arrayReadReference);
							if(arrayReadReferenceAddresses.contains(arrayWriteReferenceAddress)){
								// transfer addresses from AW to each AR corresponding to the REFR with a matching address
								AtlasSet<Node> arrayReads = AnalysisUtilities.getArrayReadAccessesForArrayReference(arrayReadReference);
								for(Node arrayRead : arrayReads){
									if(transferTypeCompatibleAddressesFromArrayMemoryModel(arrayWriteReferenceAddress, arrayRead)){
										// if we transfered new addresses add the AR to the frontier
										frontier.add(arrayRead);
									}
								}
							}
						}
					}
				}
			}
		} catch (NullPointerException e){
			// TODO: aliases which propagated through casts are throwing off the array memory model logic
		}
	}
	
	/**
	 * For a given array instantiation (a single address on an array reference),
	 * this method transfers the addresses in the array memory model to each
	 * array read for aliases of the given array instantiation
	 * 
	 * @param arrayReferenceAddress
	 * @param arrayRead
	 * @return
	 */
	private boolean transferTypeCompatibleAddressesFromArrayMemoryModel(Integer arrayReferenceAddress, Node arrayRead) {
		boolean readReceivedNewAddresses = false;
		HashSet<Integer> fromAddresses = arrayMemoryModel.get(arrayReferenceAddress);
		HashSet<Integer> toAddresses = getPointsToSet(arrayRead);		
		// need to check type compatibility
		Node toStatedType = AnalysisUtilities.statedType(arrayRead);
		if(toStatedType != null){
			// if the from type is compatible with the compatible to type set, add it
			for(Integer fromAddress : fromAddresses){
				Node addressType = addressToType.get(fromAddress);
				if (subtypes.isSubtypeOf(addressType, toStatedType)) {
					readReceivedNewAddresses |= toAddresses.add(fromAddress);
					serializeAlias(arrayRead, fromAddress);
				}
			}
		} else {
			Log.warning("No stated type during transfer for array read: " + arrayRead.address().toAddressString());
		}
		
		return readReceivedNewAddresses;
	}
	
}
