package com.ensoftcorp.open.pointsto.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ensoftcorp.open.pointsto.Activator;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

/**
 * UI for setting points-to analysis preferences
 * 
 * @author Ben Holland
 */
public class PointsToPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String JIMPLE_POINTS_TO_ANALYSIS_DESCRIPTION = "Enable Jimple (Java bytecode) Points-to Analysis";
	public static final String JAVA_POINTS_TO_ANALYSIS_DESCRIPTION = "Enable Java Source Points-to Analysis (beta)";

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
		addField(new BooleanFieldEditor(PointsToPreferences.JIMPLE_POINTS_TO_ANALYSIS, "&" + JIMPLE_POINTS_TO_ANALYSIS_DESCRIPTION, getFieldEditorParent()));
		addField(new BooleanFieldEditor(PointsToPreferences.JAVA_POINTS_TO_ANALYSIS, "&" + JAVA_POINTS_TO_ANALYSIS_DESCRIPTION, getFieldEditorParent()));
	}

}
