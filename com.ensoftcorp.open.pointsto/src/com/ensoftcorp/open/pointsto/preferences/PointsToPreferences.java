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
	 * Enable/disable tagging aliases
	 */
	public static final String TAG_ALIASES = "TAG_ALIASES";
	public static final Boolean TAG_ALIASES_DEFAULT = true;
	private static boolean tagAliasesValue = TAG_ALIASES_DEFAULT;
	
	public static boolean isTagAliasesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return tagAliasesValue;
	}
	
	/**
	 * Enable/disable tagging inferred dataflows
	 */
	public static final String TAG_INFERRED_DATAFLOWS = "TAG_INFERRED_DATAFLOWS";
	public static final Boolean TAG_INFERRED_DATAFLOWS_DEFAULT = true;
	private static boolean tagInferredDataflowsValue = TAG_INFERRED_DATAFLOWS_DEFAULT;
	
	public static boolean isTagInferredDataflowsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return tagInferredDataflowsValue;
	}
	
	/**
	 * Enable/disable rewriting array components
	 */
	public static final String REWRITE_ARRAY_COMPONENTS = "REWRITE_ARRAY_COMPONENTS";
	public static final Boolean REWRITE_ARRAY_COMPONENTS_DEFAULT = true;
	private static boolean rewriteArrayComponentsValue = REWRITE_ARRAY_COMPONENTS_DEFAULT;
	
	public static boolean isRewriteArrayComponentsEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return rewriteArrayComponentsValue;
	}
	
	/**
	 * Enable/disable tracking of primitives (very expensive)
	 * Also enables modeling JDK data flows of primitive instantiations for bytecode
	 */
	public static final String TRACK_PRIMITIVES = "TRACK_PRIMITIVES";
	public static final Boolean TRACK_PRIMITIVES_DEFAULT = false;
	private static boolean trackPrimitivesValue = TRACK_PRIMITIVES_DEFAULT;
	
	public static boolean isTrackPrimitivesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return trackPrimitivesValue;
	}
	
	/**
	 * Enable/disable tagging of runtime types
	 */
	public static final String TAG_RUNTIME_TYPES = "TAG_RUNTIME_TYPES";
	public static final Boolean TAG_RUNTIME_TYPES_DEFAULT = true;
	private static boolean tagRuntimeTypesValue = TAG_RUNTIME_TYPES_DEFAULT;
	
	public static boolean isTagRuntimeTypesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return tagRuntimeTypesValue;
	}
	
	/**
	 * Enable/disable disposal of points-to resources
	 */
	public static final String DISPOSE_RESOURCES = "DISPOSE_RESOURCES";
	public static final Boolean DISPOSE_RESOURCES_DEFAULT = true;
	private static boolean disposeResourcesValue = DISPOSE_RESOURCES_DEFAULT;
	
	public static boolean isDisposeResourcesEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return disposeResourcesValue;
	}
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(RUN_POINTS_TO_ANALYSIS, RUN_POINTS_TO_ANALYSIS_DEFAULT);
		preferences.setDefault(POINTS_TO_ANALYSIS_MODE, POINTS_TO_ANALYSIS_MODE_DEFAULT);
		preferences.setDefault(GENERAL_LOGGING, GENERAL_LOGGING_DEFAULT);
		preferences.setDefault(TAG_ALIASES, TAG_ALIASES_DEFAULT);
		preferences.setDefault(TAG_INFERRED_DATAFLOWS, TAG_INFERRED_DATAFLOWS_DEFAULT);
		preferences.setDefault(TAG_RUNTIME_TYPES, TAG_RUNTIME_TYPES_DEFAULT);
		preferences.setDefault(REWRITE_ARRAY_COMPONENTS, REWRITE_ARRAY_COMPONENTS_DEFAULT);
		preferences.setDefault(TRACK_PRIMITIVES, TRACK_PRIMITIVES_DEFAULT);
		preferences.setDefault(DISPOSE_RESOURCES, DISPOSE_RESOURCES_DEFAULT);
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
			tagAliasesValue = preferences.getBoolean(TAG_ALIASES);
			tagInferredDataflowsValue = preferences.getBoolean(TAG_INFERRED_DATAFLOWS);
			rewriteArrayComponentsValue = preferences.getBoolean(REWRITE_ARRAY_COMPONENTS);
			disposeResourcesValue = preferences.getBoolean(DISPOSE_RESOURCES);
			trackPrimitivesValue = preferences.getBoolean(TRACK_PRIMITIVES);
		} catch (Exception e){
			Log.warning("Error accessing points-to analysis preferences, using defaults...", e);
		}
		initialized = true;
	}

}
