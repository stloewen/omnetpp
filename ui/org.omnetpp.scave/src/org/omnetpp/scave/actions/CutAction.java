package org.omnetpp.scave.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

import org.omnetpp.scave.editors.DatasetEditor;


public class CutAction extends Action {

	private final IWorkbenchWindow window;

	public CutAction(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run() {
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (editor instanceof DatasetEditor) {
			DatasetEditor dsEditor = (DatasetEditor)editor;
			dsEditor.getFilterPanel().doCut();
		}
	}
}
