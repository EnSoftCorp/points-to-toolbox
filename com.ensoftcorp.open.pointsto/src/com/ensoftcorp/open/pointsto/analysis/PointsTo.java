package com.ensoftcorp.open.pointsto.analysis;

import java.text.DecimalFormat;
import java.util.HashSet;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.open.pointsto.common.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.log.Log;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

/**
 * An abstract class defining the features that a points-to analyzer should
 * implement.
 * 
 * @author Ben Holland
 */
public abstract class PointsTo {
	
	private boolean hasRun = false;
	protected boolean isDisposed = false;
	
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
	
}
