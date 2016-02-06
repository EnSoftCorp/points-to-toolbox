package com.ensoftcorp.open.pointsto.analysis;

import com.ensoftcorp.atlas.core.log.Log;

/**
 * An abstract class defining the features that a points-to analyzer should
 * implement.
 * 
 * @author Ben Holland
 */
public abstract class PointsTo {

	private boolean hasRun = false;
	
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
			return start - System.currentTimeMillis();
		}
	}
	
	/**
	 * Runs the fixed-point points-to analysis algorithm
	 */
	protected abstract void runAnalysis();
	
}
