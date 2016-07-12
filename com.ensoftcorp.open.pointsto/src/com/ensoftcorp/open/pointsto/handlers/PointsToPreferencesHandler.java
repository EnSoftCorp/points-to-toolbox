package com.ensoftcorp.open.pointsto.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A menu handler for configuring the points-to analysis preferences
 * 
 * @author Ben Holland
 */
public class PointsToPreferencesHandler extends AbstractHandler {
	public PointsToPreferencesHandler() {}

	/**
	 * Runs the purity analysis
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String id = "com.ensoftcorp.open.pointsto.ui.preferences";
		return PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), id, new String[] {id}, null).open();
	}
	
}
