package com.ensoftcorp.open.pointsto.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.commons.ui.components.LabelFieldEditor;
import com.ensoftcorp.open.commons.ui.components.SpacerFieldEditor;
import com.ensoftcorp.open.pointsto.Activator;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

/**
 * UI for setting points-to analysis preferences
 * 
 * @author Ben Holland
 */
public class PointsToPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final String RUN_POINTS_TO_ANALYSIS_DESCRIPTION = "Run points-to analysis";
	
	private static final String JIMPLE_POINTS_TO_ANALYSIS_MODE_DESCRIPTION = "Enable Jimple (Java bytecode) points-to analysis";
	private static final String JAVA_POINTS_TO_ANALYSIS_MODE_DESCRIPTION = "Enable Java source points-to analysis (beta)";
	
	private static final String GENERAL_LOGGING_DESCRIPTION = "Enable general logging";
	private static final String TAG_ALIASES_DESCRIPTION = "Tag aliases";
	private static final String TAG_INFERRED_DATAFLOWS_DESCRIPTION = "Tag inferred dataflows";
	private static final String TAG_RUNTIME_TYPES_DESCRIPTION = "Tag runtime types";
	private static final String REWRITE_ARRAYS_DESCRIPTION = "Rewrite array components";
	private static final String DISPOSE_RESOURCES_DESCRIPTION = "Dispose points-to resources";
	private static final String TRACK_PRIMITIVES_DESCRIPTION = "Track primitive values (expensive)";
	
	private static boolean changeListenerAdded = false;

	public PointsToPreferencesPage() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		setPreferenceStore(preferences);
		setDescription("Configure preferences for the Points-to Analysis Toolbox plugin.");

		// use to update cached values if user edits a preference
		if (!changeListenerAdded) {
			getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
					PointsToPreferences.loadPreferences();
				}
			});
			changeListenerAdded = true;
		}
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PointsToPreferences.RUN_POINTS_TO_ANALYSIS, "&" + RUN_POINTS_TO_ANALYSIS_DESCRIPTION, getFieldEditorParent()));

		RadioGroupFieldEditor analysisMode = new RadioGroupFieldEditor(
				PointsToPreferences.POINTS_TO_ANALYSIS_MODE,
				"Analysis Mode",
				1,
				new String[][] {
					{ "&" + JAVA_POINTS_TO_ANALYSIS_MODE_DESCRIPTION, 
						PointsToPreferences.JAVA_POINTS_TO_ANALYSIS_MODE
					},
					{ "&" + JIMPLE_POINTS_TO_ANALYSIS_MODE_DESCRIPTION, 
						PointsToPreferences.JIMPLE_POINTS_TO_ANALYSIS_MODE
					}
				},
				getFieldEditorParent(),
				true);
		addField(analysisMode);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		addField(new LabelFieldEditor("Advanced Options", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.GENERAL_LOGGING, "&" + GENERAL_LOGGING_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.TAG_ALIASES, "&" + TAG_ALIASES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.TAG_INFERRED_DATAFLOWS, "&" + TAG_INFERRED_DATAFLOWS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.TAG_RUNTIME_TYPES, "&" + TAG_RUNTIME_TYPES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.REWRITE_ARRAY_COMPONENTS, "&" + REWRITE_ARRAYS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.DISPOSE_RESOURCES, "&" + DISPOSE_RESOURCES_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.TRACK_PRIMITIVES, "&" + TRACK_PRIMITIVES_DESCRIPTION, getFieldEditorParent()));
	}

}
