package com.ensoftcorp.open.pointsto.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ensoftcorp.open.pointsto.Activator;
import com.ensoftcorp.open.pointsto.log.Log;

public class PointsToPreferences extends AbstractPreferenceInitializer {

	private static boolean initialized = false;
	
	/**
	 * Enable/disable points-to analysis
	 */
	public static final String RUN_POINTS_TO_ANALYSIS = "RUN_POINTS_TO_ANALYSIS";
	public static final Boolean RUN_POINTS_TO_ANALYSIS_DEFAULT = false;
	private static boolean runPointsToAnalysisValue = RUN_POINTS_TO_ANALYSIS_DEFAULT;
	
	public static boolean isPointsToAnalysisEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return runPointsToAnalysisValue;
	}
	
	public static final String POINTS_TO_ANALYSIS_MODE = "POINTS_TO_ANALYSIS_MODE";
	public static final String JIMPLE_POINTS_TO_ANALYSIS_MODE = "JIMPLE_POINTS_TO_ANALYSIS_MODE";
	public static final String JAVA_POINTS_TO_ANALYSIS_MODE = "JAVA_POINTS_TO_ANALYSIS_MODE";
	public static final String POINTS_TO_ANALYSIS_MODE_DEFAULT = JAVA_POINTS_TO_ANALYSIS_MODE;
	private static String analysisModeValue = POINTS_TO_ANALYSIS_MODE_DEFAULT;
	
	/**
	 * Returns true if java points-to analysis is enabled
	 * @return
	 */
	public static boolean isJavaPointsToAnalysisModeEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return analysisModeValue.equals(JAVA_POINTS_TO_ANALYSIS_MODE);
	}
	
	/**
	 * Returns true if jimple points-to analysis is enabled
	 * @return
	 */
	public static boolean isJimplePointsToAnalysisModeEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return analysisModeValue.equals(JIMPLE_POINTS_TO_ANALYSIS_MODE);
	}
	
	/**
	 * Enable/disable general logging
	 */
	public static final String GENERAL_LOGGING = "GENERAL_LOGGING";
	public static final Boolean GENERAL_LOGGING_DEFAULT = true;
	private static boolean generalLoggingValue = GENERAL_LOGGING_DEFAULT;
	
	public static boolean isGeneralLoggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return generalLoggingValue;
	}
	
	/**
	 * Enable/disable modeling JDK data flows of primitive instantiations
	 */
	public static final String MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS = "MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS";
	public static final Boolean MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS_DEFAULT = true;
	private static boolean modelPrimitiveInstantiationDataFlowsValue = MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS_DEFAULT;
	
	public static boolean isModelPrimitiveInstantiationDataFlowsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return modelPrimitiveInstantiationDataFlowsValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(RUN_POINTS_TO_ANALYSIS, RUN_POINTS_TO_ANALYSIS_DEFAULT);
		preferences.setDefault(POINTS_TO_ANALYSIS_MODE, POINTS_TO_ANALYSIS_MODE_DEFAULT);
		preferences.setDefault(GENERAL_LOGGING, GENERAL_LOGGING_DEFAULT);
		preferences.setDefault(MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS, MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS_DEFAULT);
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			runPointsToAnalysisValue = preferences.getBoolean(RUN_POINTS_TO_ANALYSIS);
			analysisModeValue = preferences.getString(POINTS_TO_ANALYSIS_MODE);		
			generalLoggingValue = preferences.getBoolean(GENERAL_LOGGING);
			modelPrimitiveInstantiationDataFlowsValue = preferences.getBoolean(MODEL_PRIMITIVE_INSTANTIATION_DATAFLOWS);
		} catch (Exception e){
			Log.warning("Error accessing points-to analysis preferences, using defaults...", e);
		}
		initialized = true;
	}

}
