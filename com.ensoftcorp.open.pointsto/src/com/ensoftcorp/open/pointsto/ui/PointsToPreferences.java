package com.ensoftcorp.open.pointsto.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.pointsto.Activator;

/**
 * UI for setting points-to analysis preferences
 * 
 * @author Ben Holland
 */
public class PointsToPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	// enable/disable jimple points-to analysis
	public static final String ENABLE_JIMPLE_POINTS_TO_ANALYSIS_BOOLEAN = "ENABLE_JIMPLE_POINTS_TO_ANALYSIS_BOOLEAN";
	public static final String ENABLE_JIMPLE_POINTS_TO_ANALYSIS_DESCRIPTION = "Enable Jimple Points-to Analysis";
	
	public static boolean isJimplePointsToAnalysisEnabled(){
		return Activator.getDefault().getPreferenceStore().getBoolean(ENABLE_JIMPLE_POINTS_TO_ANALYSIS_BOOLEAN);
	}
	
	public PointsToPreferences() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Configure preferences for the Points-to Analysis Toolbox plugin.");
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(ENABLE_JIMPLE_POINTS_TO_ANALYSIS_BOOLEAN, "&" + ENABLE_JIMPLE_POINTS_TO_ANALYSIS_DESCRIPTION, getFieldEditorParent()));
	}

}
