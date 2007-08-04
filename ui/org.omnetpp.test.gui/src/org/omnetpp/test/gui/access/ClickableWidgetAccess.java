package org.omnetpp.test.gui.access;

import junit.framework.Assert;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

public abstract class ClickableWidgetAccess<T extends Widget> extends Access
{
	protected T widget;
	
	public ClickableWidgetAccess(T widget)
	{
		Assert.assertTrue(widget != null);
		this.widget = widget;
	}

	public MenuAccess activateContextMenuWithMouseClick() {
		rightClick();
		return new MenuAccess(getContextMenu());
	}

	protected abstract Point getPointToClick();

	protected abstract Menu getContextMenu();

	public void click(int button) {
		click(button, getPointToClick());
	}

	public void click() {
		click(Access.LEFT_MOUSE_BUTTON);
	}

	public void rightClick() {
		click(Access.RIGHT_MOUSE_BUTTON);
	}

	public void doubleClick() {
		doubleClick(LEFT_MOUSE_BUTTON, getPointToClick());
	}
}
