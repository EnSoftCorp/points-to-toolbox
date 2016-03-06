package com.ensoftcorp.open.pointsto.analysis;

import java.util.HashSet;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
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
	 * Returns a convenience mapping of arrays to the array's components
	 * @return
	 */
	public abstract Map<Long, HashSet<Long>> getArrayMemoryModel();
	
	/**
	 * Returns a convenience mapping of an address to its corresponding instantiation
	 * @return
	 */
	public abstract Map<Long, GraphElement> getAddressToInstantiation();

	/**
	 * Returns a convenience mapping of an address to its corresponding static type
	 * @return
	 */
	public abstract Map<Long, GraphElement> getAddressToType();
	
	/**
	 * Returns the inferred data flow graph as the results of the fixed point analysis
	 * @return
	 */
	public abstract Q getInferredDataFlowGraph();
	
}
