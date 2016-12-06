package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.open.pointsto.log.Log;

/**
 * An abstract class defining the features that a points-to analyzer should
 * implement.
 * 
 * @author Ben Holland
 */
public abstract class PointsTo {
	
	private boolean hasRun = false;
	protected boolean isDisposed = false;
	
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
	public long run(){
		if(isDisposed){
			throw new RuntimeException("Points-to analysis was disposed.");
		}
		if(hasRun){
			return 0;
		} else {
			long start = System.currentTimeMillis();
			Log.info("Starting " + getClass().getSimpleName() + " points-to analysis");
			runAnalysis();
			long time = System.currentTimeMillis() - start;
			Log.info("Finished " + getClass().getSimpleName() + " points-to analysis in " + time + "ms");
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
	
}
