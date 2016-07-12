package com.ensoftcorp.open.pointsto.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.pointsto.Activator;
import com.ensoftcorp.open.pointsto.log.Log;

public class PointsToPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable Jimple points-to analysis
	 */
	public static final String JIMPLE_POINTS_TO_ANALYSIS = "JIMPLE_POINTS_TO_ANALYSIS";
	public static final Boolean JIMPLE_POINTS_TO_ANALYSIS_DEFAULT = false;
	private static boolean jimplePointsToAnalysisValue = JIMPLE_POINTS_TO_ANALYSIS_DEFAULT;
	
	public static boolean isJimplePointsToAnalysisEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return jimplePointsToAnalysisValue;
	}
	
	/**
	 * Enable/disable Java source points-to analysis
	 */
	public static final String JAVA_POINTS_TO_ANALYSIS = "JAVA_POINTS_TO_ANALYSIS";
	public static final Boolean JAVA_POINTS_TO_ANALYSIS_DEFAULT = false;
	private static boolean javaPointsToAnalysisValue = JAVA_POINTS_TO_ANALYSIS_DEFAULT;
	
	public static boolean isJavaPointsToAnalysisEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return javaPointsToAnalysisValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(JIMPLE_POINTS_TO_ANALYSIS, JIMPLE_POINTS_TO_ANALYSIS_DEFAULT);
		preferences.setDefault(JAVA_POINTS_TO_ANALYSIS, JAVA_POINTS_TO_ANALYSIS_DEFAULT);
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			jimplePointsToAnalysisValue = preferences.getBoolean(JIMPLE_POINTS_TO_ANALYSIS);
			javaPointsToAnalysisValue = preferences.getBoolean(JAVA_POINTS_TO_ANALYSIS);
		} catch (Exception e){
			Log.warning("Error accessing points-to analysis preferences, using defaults...", e);
		}
		initialized = true;
	}

}
