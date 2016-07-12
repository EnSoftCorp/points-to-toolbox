package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;

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
			Log.info("Starting " + getClass().getSimpleName() + " Points-to Analysis");
			runAnalysis();
			Log.info("Finished " + getClass().getSimpleName() + " Points-to Analysis");
			hasRun = true;
			return System.currentTimeMillis() - start;
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
	public abstract HashSet<Long> getArrayMemoryModel(Long address);
	
	/**
	 * Returns a set of alias addresses
	 * @param node
	 * @return
	 */
	public abstract HashSet<Long> getAliasAddresses(Node node);
	
	/**
	 * Returns the mapping of an address to its corresponding instantiation
	 * @return
	 */
	public abstract Node getInstantiation(Long address);

	/**
	 * Returns the mapping of an address to its corresponding static type
	 * @return
	 */
	public abstract Node getType(Long address);
	
	/**
	 * Returns a set of all addressed nodes
	 * @return
	 */
	public abstract AtlasSet<Node> getAddressedNodes();
	
	/**
	 * Returns the inferred data flow graph as the results of the fixed point analysis
	 * @return
	 */
	public abstract Q getInferredDataFlowGraph();
	
	public boolean isDisposed(){
		return isDisposed;
	}
	
	/**
	 * Signals that the points to analysis results no longer need to be maintained by the analysis
	 */
	public abstract void dispose();
	
}
