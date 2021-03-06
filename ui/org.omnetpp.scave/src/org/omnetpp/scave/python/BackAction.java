package org.omnetpp.scave.python;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.omnetpp.scave.actions.AbstractScaveAction;
import org.omnetpp.scave.editors.ChartScriptEditor;
import org.omnetpp.scave.editors.ScaveEditor;

public class BackAction extends AbstractScaveAction {
    public BackAction() {
        setText("Back");
        setDescription("TODO.");
        setImageDescriptor(
                PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
    }

    @Override
    protected void doRun(ScaveEditor scaveEditor, IStructuredSelection selection) {
        ChartScriptEditor editor = scaveEditor.getActiveChartScriptEditor();
        if (editor != null)
            editor.getMatplotlibChartViewer().back();
    }

    @Override
    protected boolean isApplicable(ScaveEditor scaveEditor, IStructuredSelection selection) {
        ChartScriptEditor editor = scaveEditor.getActiveChartScriptEditor();
        return editor != null && editor.isPythonProcessAlive();
    }
}