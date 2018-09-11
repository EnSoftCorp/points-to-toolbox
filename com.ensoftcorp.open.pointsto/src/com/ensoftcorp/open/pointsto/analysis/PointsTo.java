package com.ensoftcorp.open.pointsto.analysis;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.UncheckedGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.algorithms.StronglyConnectedComponents;
import com.ensoftcorp.open.pointsto.common.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.log.Log;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

import net.ontopia.utils.CompactHashMap;

/**
 * An abstract class defining the features that a points-to analyzer should
 * implement.
 * 
 * @author Ben Holland
 */
public abstract class PointsTo {
	
	private boolean hasRun = false;
	protected boolean isDisposed = false;
	protected long lastUpdateTime = 0;
	
	public static final long UPDATE_INTERVAL = 5000; // 5 seconds
	
	/**
	 * Returns true if the points-to analysis has completed
	 * @return
	 */
	public boolean hasRun(){
		return hasRun;
	}
	
	/**
	 * Runs the points to analysis (if it hasn't been run already)
	 * and returns the time in milliseconds to complete the analysis
	 * @return
	 */
	public double run(){
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		if(hasRun){
			return 0;
		} else {
			Log.info("Starting " + getClass().getSimpleName() + " points-to analysis");
			long start = System.nanoTime();
			runAnalysis();
			long stop = System.nanoTime();
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			double time = (stop - start)/1000.0/1000.0; // ms
			if(time < 100) {
				Log.info("Finished " + getClass().getSimpleName() + " points-to analysis in " + decimalFormat.format(time) + "ms");
			} else {
				time = (stop - start)/1000.0/1000.0/1000.0; // s
				if(time < 60) {
					Log.info("Finished " + getClass().getSimpleName() + " points-to analysis in " + decimalFormat.format(time) + "s");
				} else {
					time = (stop - start)/1000.0/1000.0/1000.0/60.0; // m
					if(time < 60) {
						Log.info("Finished " + getClass().getSimpleName() + " points-to analysis in " + decimalFormat.format(time) + "m");
					} else {
						time = (stop - start)/1000.0/1000.0/1000.0/60.0/60.0; // h
						Log.info("Finished " + getClass().getSimpleName() + " points-to analysis in " + decimalFormat.format(time) + "h");
					}
				}
			}
			
			hasRun = true;
			return time;
		}
	}
	
	/**
	 * Runs the fixed-point points-to analysis algorithm
	 */
	protected abstract void runAnalysis();
	
	/**
	 * Returns the mapping of arrays to the array's components
	 * @return
	 */
	public abstract HashSet<Integer> getArrayMemoryModelAliases(Integer address);
	
	/**
	 * Returns the array memory model addresses
	 * @return
	 */
	public abstract HashSet<Integer> getArrayMemoryModels();
	
	/**
	 * Returns a set of alias addresses
	 * @param node
	 * @return
	 */
	public abstract HashSet<Integer> getAliasAddresses();
	
	/**
	 * Returns a set of alias addresses for the given node
	 * @param node
	 * @return
	 */
	public abstract HashSet<Integer> getAliasAddresses(Node node);
	
	/**
	 * Removes an alias address from the alias set
	 * @param node
	 */
	public abstract void addAliasAddress(Node node, Integer address);
	
	/**
	 * Adds an alias address to the alias set
	 * @param node
	 */
	public abstract void removeAliasAddress(Node node, Integer address);
	
	/**
	 * Returns the mapping of an address to its corresponding instantiation
	 * @return
	 */
	public abstract Node getInstantiation(Integer address);

	/**
	 * Returns the mapping of an address to its corresponding static type
	 * @return
	 */
	public abstract Node getType(Integer address);
	
	/**
	 * Returns a set of all addressed nodes
	 * @return
	 */
	public abstract AtlasSet<Node> getAddressedNodes();
	
	/**
	 * Returns the inferred data flow graph as the results of the fixed point analysis
	 * @return
	 */
	public abstract Graph getInferredDataFlowGraph();
	
	public boolean isDisposed(){
		return isDisposed;
	}
	
	/**
	 * Signals that the points to analysis results no longer need to be maintained by the analysis
	 */
	public abstract void dispose();
	
	/**
	 * Helper method to consistently convert integer based addresses to alias tags
	 * @param addressedObject
	 * @param address
	 */
	protected void serializeAlias(Node addressedObject, Integer address) {
		if(PointsToPreferences.isTagAliasesEnabled()){
			if(address == 0){
				addressedObject.tag(PointsToAnalysis.NULL_ALIAS);
			} else {
				addressedObject.tag(PointsToAnalysis.ALIAS_PREFIX + address);
			}
		}
	}
	
	private static final String MERGE_SCC_NODE = "MERGE_SCC_NODE";
	private static final String MERGE_SCC_EDGE = "MERGE_SCC_EDGE";
	private CompactHashMap<Node,AtlasSet<Node>> collapsedNodes = new CompactHashMap<Node,AtlasSet<Node>>();
	private AtlasSet<Edge> collapsedEdges = new AtlasHashSet<Edge>();
	
	protected Graph collapseSCCs(Graph dfGraph) {
		if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info("Computing SCCs...");
		StronglyConnectedComponents sccs = new StronglyConnectedComponents(dfGraph);
		int sccGroupID = 1;
		long collapsedNodeCounter = 0;
		long collapsedEdgeCounter = 0;
		double progress = 0;
		List<AtlasHashSet<Node>> sccList = sccs.findSCCs(false);
		double total = sccList.size();
		
		AtlasSet<Node> dfNodes = new AtlasHashSet<Node>(dfGraph.nodes());
		AtlasSet<Edge> dfEdges = new AtlasHashSet<Edge>(dfGraph.edges());
		
		for(AtlasHashSet<Node> scc : sccList){
			if(PointsToPreferences.isGeneralLoggingEnabled() && System.currentTimeMillis()-lastUpdateTime > PointsTo.UPDATE_INTERVAL) {
				Log.info("Collapsing SCCs: (" + new DecimalFormat("#.##").format((progress / total) * 100.0) + " %) " 
						+ String.format("Collapsed %s SCCs (elliding %s nodes and %s edges)", (sccGroupID-1), collapsedNodeCounter, collapsedEdgeCounter));
				lastUpdateTime = System.currentTimeMillis();
			}
			
			if(scc.size() > 1) {
				// create a new merge node
				Node mergeNode = Graph.U.createNode();
				mergeNode.putAttr(XCSG.name, "SCC_" + (sccGroupID++));
				mergeNode.tag(MERGE_SCC_NODE);
				collapsedNodes.put(mergeNode, scc);
				collapsedNodeCounter += scc.size();
				
				// get the edges in and out of the scc
				Q dfg = Common.toQ(dfGraph);
				Q component = Common.toQ(scc).induce(dfg);
				AtlasSet<Edge> incomingEdges = new AtlasHashSet<Edge>(dfg.reverseStep(component).differenceEdges(component).eval().edges());
				collapsedEdgeCounter += incomingEdges.size();
				
				AtlasSet<Edge> outgoingEdges = new AtlasHashSet<Edge>(dfg.forwardStep(component).differenceEdges(component).eval().edges());
				collapsedEdgeCounter += outgoingEdges.size();
				
				// create new merged edges going into the merge node
				// just need to know what edges will be removed and where to add merge edges
				AtlasSet<Node> fromNodes = new AtlasHashSet<Node>();
				for(Edge incomingEdge : incomingEdges) {
					fromNodes.add(incomingEdge.from());
					collapsedEdges.add(incomingEdge);
				}
				AtlasSet<Edge> mergedIncomingEdges = new AtlasHashSet<Edge>();
				for(Node from : fromNodes) {
					Edge mergedIncomingEdge = Graph.U.createEdge(from, mergeNode);
					mergedIncomingEdge.tag(MERGE_SCC_EDGE);
					mergedIncomingEdges.add(mergedIncomingEdge);
				}
				
				// create new merged edges going into the merge node
				// just need to know what edges will be removed and where to add merge edges
				AtlasSet<Node> toNodes = new AtlasHashSet<Node>();
				for(Edge outgoingEdge : outgoingEdges) {
					toNodes.add(outgoingEdge.to());
					collapsedEdges.add(outgoingEdge);
				}
				AtlasSet<Edge> mergedOutgoingEdges = new AtlasHashSet<Edge>();
				for(Node to : toNodes) {
					Edge mergedOutgoingEdge = Graph.U.createEdge(to, mergeNode);
					mergedOutgoingEdge.tag(MERGE_SCC_EDGE);
					mergedOutgoingEdges.add(mergedOutgoingEdge);
				}
				
				// remove the scc node
				for(Node sccNode : scc) {
					dfNodes.remove(sccNode);
				}
				
				// remove the scc node edges
				for(Edge sccEdge : component.eval().edges()) {
					dfEdges.remove(sccEdge);
				}
				
				// add the merge node
				dfNodes.add(mergeNode);
				
				// add the merge node edges
				dfEdges.addAll(mergedIncomingEdges);
				dfEdges.addAll(mergedOutgoingEdges);
			}
			
			// increment progress
			progress++;
		}
		
		// update the data flow graph
		dfGraph = new UncheckedGraph(dfNodes, dfEdges);
		
		if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info(String.format("Collapsed %s SCCs (elliding %s nodes and %s edges)", (sccGroupID-1), collapsedNodeCounter, collapsedEdgeCounter));
		
		return dfGraph;
	}
	
	/**
	 * 
	 * @param dfGraph
	 * @return
	 */
	protected Graph expandSCCs(Graph dfGraph) {
		AtlasSet<Node> dfNodes = new AtlasHashSet<Node>(dfGraph.nodes());
		AtlasSet<Edge> dfEdges = new AtlasHashSet<Edge>(dfGraph.edges());

		// add the scc nodes back to the dfg and add alias tags
		Set<Entry<Node, AtlasSet<Node>>> entrySet = collapsedNodes.entrySet();
		int numSCCs = entrySet.size();
		double progress = 0;
		for(Entry<Node,AtlasSet<Node>> entry : entrySet) {
			if(PointsToPreferences.isGeneralLoggingEnabled() && System.currentTimeMillis()-lastUpdateTime > PointsTo.UPDATE_INTERVAL) {
				Log.info("Expanding SCCs: (" + new DecimalFormat("#.##").format((progress / (double) numSCCs) * 100.0) + " %) of " + numSCCs + " SCCs");
				lastUpdateTime = System.currentTimeMillis();
			}
			
			Node mergeNode = entry.getKey();
			AtlasSet<Node> scc = entry.getValue();
			for(Node sccNode : scc) {
				for(Integer address : this.getAliasAddresses(mergeNode)) {
					serializeAlias(sccNode, address);
					dfNodes.add(sccNode);
				}
			}
			progress++;
		}
		collapsedNodes.clear();

		// add the collapsed edges back to the dfg
		dfEdges.addAll(collapsedEdges);
		collapsedEdges.clear();
		
		if(PointsToPreferences.isGeneralLoggingEnabled()) Log.info(String.format("Expanded " + numSCCs + " SCCs"));
		
		// update the resulting graph
		dfGraph = new UncheckedGraph(dfNodes, dfEdges);
		return dfGraph;
	}
}
