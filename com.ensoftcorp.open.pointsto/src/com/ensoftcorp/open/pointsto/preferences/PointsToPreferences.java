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
	
	/**
	 * Configures whether or not immutability analysis should be run
	 */
	public static void enabledPointsToAnalysis(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setDefault(RUN_POINTS_TO_ANALYSIS, enabled);
		loadPreferences();
	}
	
	/**
	 * Returns true if points-to analysis is enabled 
	 * @return
	 */
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
	 * Configures points-to analysis mode to target Java source
	 */
	public static void setJavaPointsToAnalysisMode(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(POINTS_TO_ANALYSIS_MODE, JAVA_POINTS_TO_ANALYSIS_MODE);
		loadPreferences();
	}
	
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
	 * Configures points-to analysis mode to target Jimple
	 */
	public static void setJimplePointsToAnalysisMode(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(POINTS_TO_ANALYSIS_MODE, JIMPLE_POINTS_TO_ANALYSIS_MODE);
		loadPreferences();
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
	
	public static final String POINTS_TO_ANALYSIS_FRONTIER_MODE = "POINTS_TO_ANALYSIS_FRONTIER_MODE";
	public static final String POINTS_TO_ANALYSIS_FIFO_FRONTIER_MODE = "POINTS_TO_ANALYSIS_FIFO_FRONTIER_MODE";
	public static final String POINTS_TO_ANALYSIS_LIFO_FRONTIER_MODE = "POINTS_TO_ANALYSIS_LIFO_FRONTIER_MODE";
	public static final String POINTS_TO_ANALYSIS_LRU_FRONTIER_MODE = "POINTS_TO_ANALYSIS_LRU_FRONTIER_MODE";
	public static final String POINTS_TO_ANALYSIS_FRONTIER_MODE_DEFAULT = POINTS_TO_ANALYSIS_FIFO_FRONTIER_MODE;
	private static String frontierAnalysisModeValue = POINTS_TO_ANALYSIS_FRONTIER_MODE_DEFAULT;
	
	/**
	 * Configures points-to analysis frontier mode to use FIFO mode
	 */
	public static void setPointsToAnalysisFIFOFrontierMode(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(POINTS_TO_ANALYSIS_FRONTIER_MODE, POINTS_TO_ANALYSIS_FIFO_FRONTIER_MODE);
		loadPreferences();
	}
	
	/**
	 * Returns true if points-to analysis FIFO frontier is enabled
	 * @return
	 */
	public static boolean isPointsToAnalysisFIFOFrontierMode(){
		if(!initialized){
			loadPreferences();
		}
		return frontierAnalysisModeValue.equals(POINTS_TO_ANALYSIS_FIFO_FRONTIER_MODE);
	}
	
	/**
	 * Configures points-to analysis frontier mode to use LIFO mode
	 */
	public static void setPointsToAnalysisLIFOFrontierMode(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(POINTS_TO_ANALYSIS_FRONTIER_MODE, POINTS_TO_ANALYSIS_LIFO_FRONTIER_MODE);
		loadPreferences();
	}
	
	/**
	 * Returns true if points-to analysis LIFO frontier is enabled
	 * @return
	 */
	public static boolean isPointsToAnalysisLIFOFrontierMode(){
		if(!initialized){
			loadPreferences();
		}
		return frontierAnalysisModeValue.equals(POINTS_TO_ANALYSIS_LIFO_FRONTIER_MODE);
	}
	
	/**
	 * Configures points-to analysis frontier mode to use LRU mode
	 */
	public static void setPointsToAnalysisLRUFrontierMode(){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(POINTS_TO_ANALYSIS_FRONTIER_MODE, POINTS_TO_ANALYSIS_LRU_FRONTIER_MODE);
		loadPreferences();
	}
	
	/**
	 * Returns true if points-to analysis LRU frontier is enabled
	 * @return
	 */
	public static boolean isPointsToAnalysisLRUFrontierMode(){
		if(!initialized){
			loadPreferences();
		}
		return frontierAnalysisModeValue.equals(POINTS_TO_ANALYSIS_LRU_FRONTIER_MODE);
	}
	
	/**
	 * Configures general logging
	 */
	public static void enableGeneralLogging(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(GENERAL_LOGGING, enabled);
		loadPreferences();
	}
	
	public static boolean isGeneralLoggingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return generalLoggingValue;
	}
	
	/**
	 * Enable/disable tracking array read/writes
	 */
	public static final String ARRAY_COMPONENT_TRACKING = "ARRAY_COMPONENT_TRACKING";
	public static final Boolean ARRAY_COMPONENT_TRACKING_DEFAULT = true;
	private static boolean arrayComponentTrackingValue = ARRAY_COMPONENT_TRACKING_DEFAULT;
	
	/**
	 * Configures array read/write tracking
	 */
	public static void enableArrayComponentTracking(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(ARRAY_COMPONENT_TRACKING, enabled);
		loadPreferences();
	}
	
	public static boolean isArrayComponentTrackingEnabled(){
		if(!initialized){
			loadPreferences();
		}
		return arrayComponentTrackingValue;
	}
	
	
	/**
	 * Enable/disable tagging aliases
	 */
	public static final String TAG_ALIASES = "TAG_ALIASES";
	public static final Boolean TAG_ALIASES_DEFAULT = true;
	private static boolean tagAliasesValue = TAG_ALIASES_DEFAULT;
	
	/**
	 * Configures alias tagging
	 */
	public static void enableTagAliases(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(TAG_ALIASES, enabled);
		loadPreferences();
	}
	
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
	
	/**
	 * Configures inferred data flow tagging
	 */
	public static void enableTagInferredDataflows(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(TAG_INFERRED_DATAFLOWS, enabled);
		loadPreferences();
	}
	
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
	
	/**
	 * Configures array component rewritting
	 */
	public static void enableRewriteArrayComponents(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(REWRITE_ARRAY_COMPONENTS, enabled);
		loadPreferences();
	}
	
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
	
	/**
	 * Configures primitive tracking
	 */
	public static void enableTrackPrimitives(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(TRACK_PRIMITIVES, enabled);
		loadPreferences();
	}
	
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
	
	/**
	 * Configures runtime type tagging
	 */
	public static void enableTagRuntimeTypes(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(TAG_RUNTIME_TYPES, enabled);
		loadPreferences();
	}
	
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
	
	/**
	 * Configures resource disposal
	 */
	public static void enableDisposeResources(boolean enabled){
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(DISPOSE_RESOURCES, enabled);
		loadPreferences();
	}
	
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
		preferences.setDefault(POINTS_TO_ANALYSIS_FRONTIER_MODE, POINTS_TO_ANALYSIS_FRONTIER_MODE_DEFAULT);
		preferences.setDefault(ARRAY_COMPONENT_TRACKING, ARRAY_COMPONENT_TRACKING_DEFAULT);
		preferences.setDefault(TAG_ALIASES, TAG_ALIASES_DEFAULT);
		preferences.setDefault(TAG_INFERRED_DATAFLOWS, TAG_INFERRED_DATAFLOWS_DEFAULT);
		preferences.setDefault(TAG_RUNTIME_TYPES, TAG_RUNTIME_TYPES_DEFAULT);
		preferences.setDefault(REWRITE_ARRAY_COMPONENTS, REWRITE_ARRAY_COMPONENTS_DEFAULT);
		preferences.setDefault(TRACK_PRIMITIVES, TRACK_PRIMITIVES_DEFAULT);
		preferences.setDefault(DISPOSE_RESOURCES, DISPOSE_RESOURCES_DEFAULT);
	}
	
	/**
	 * Restores the default preferences
	 */
	public static void restoreDefaults() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		preferences.setValue(RUN_POINTS_TO_ANALYSIS, RUN_POINTS_TO_ANALYSIS_DEFAULT);
		preferences.setValue(POINTS_TO_ANALYSIS_MODE, POINTS_TO_ANALYSIS_MODE_DEFAULT);
		preferences.setValue(GENERAL_LOGGING, GENERAL_LOGGING_DEFAULT);
		preferences.setValue(POINTS_TO_ANALYSIS_FRONTIER_MODE, POINTS_TO_ANALYSIS_FRONTIER_MODE_DEFAULT);
		preferences.setValue(ARRAY_COMPONENT_TRACKING, ARRAY_COMPONENT_TRACKING_DEFAULT);
		preferences.setValue(TAG_ALIASES, TAG_ALIASES_DEFAULT);
		preferences.setValue(TAG_INFERRED_DATAFLOWS, TAG_INFERRED_DATAFLOWS_DEFAULT);
		preferences.setValue(TAG_RUNTIME_TYPES, TAG_RUNTIME_TYPES_DEFAULT);
		preferences.setValue(REWRITE_ARRAY_COMPONENTS, REWRITE_ARRAY_COMPONENTS_DEFAULT);
		preferences.setValue(TRACK_PRIMITIVES, TRACK_PRIMITIVES_DEFAULT);
		preferences.setValue(DISPOSE_RESOURCES, DISPOSE_RESOURCES_DEFAULT);
		loadPreferences();
	}
	
	/**
	 * Loads or refreshes current preference values
	 */
	public static void loadPreferences() {
		try {
			IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
			runPointsToAnalysisValue = preferences.getBoolean(RUN_POINTS_TO_ANALYSIS);
			analysisModeValue = preferences.getString(POINTS_TO_ANALYSIS_MODE);
			frontierAnalysisModeValue = preferences.getString(POINTS_TO_ANALYSIS_FRONTIER_MODE);
			arrayComponentTrackingValue = preferences.getBoolean(ARRAY_COMPONENT_TRACKING);
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
