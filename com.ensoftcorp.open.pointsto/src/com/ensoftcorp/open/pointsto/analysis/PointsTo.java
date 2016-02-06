package com.ensoftcorp.open.pointsto.analysis;

import com.ensoftcorp.atlas.core.log.Log;

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
	 * Runs the points to analysis
	 */
	protected abstract void runAnalysis();
	
}
