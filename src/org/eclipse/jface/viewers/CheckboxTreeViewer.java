/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;

/**
 * A concrete tree-structured viewer based on an SWT <code>Tree</code>
 * control with checkboxes on each node.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT tree control and configured
 * with a domain-specific content provider, label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 */
public class CheckboxTreeViewer extends TreeViewer implements ICheckable {
	
	/**
	 * List of check state listeners (element type: <code>ICheckStateListener</code>).
	 */
	private ListenerList checkStateListeners = new ListenerList(3);

	/**
	 * Last item clicked on, or <code>null</code> if none.
	 */
	private TreeItem lastClickedItem = null;	
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the SWT style bits: <code>CHECK</code> and <code>BORDER</code>.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 */
public CheckboxTreeViewer(Composite parent) {
	this(parent, SWT.BORDER);
}
/**
 * Creates a tree viewer on a newly-created tree control under the given parent.
 * The tree control is created using the given SWT style bits, plus the <code>CHECK</code> style bit.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param parent the parent control
 * @param style the SWT style bits
 */
public CheckboxTreeViewer(Composite parent, int style) {
	this(new Tree(parent, SWT.CHECK | style));
}
/**
 * Creates a tree viewer on the given tree control.
 * The <code>SWT.CHECK</code> style bit must be set on the given tree control.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters.
 *
 * @param tree the tree control
 */
public CheckboxTreeViewer(Tree tree) {
	super(tree);
}
/* (non-Javadoc)
 * Method declared on ICheckable.
 */
public void addCheckStateListener(ICheckStateListener listener) {
	checkStateListeners.add(listener);
}
/**
 * Applies the checked and grayed states of the given widget and its
 * descendents.
 *
 * @param checked a set of elements (element type: <code>Object</code>) 
 * @param grayed a set of elements (element type: <code>Object</code>) 
 * @param widget the widget
 */
private void applyState(CustomHashtable checked, CustomHashtable grayed, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		if (item instanceof TreeItem) {
			Object data = item.getData();
			if (data != null) {
				TreeItem ti = (TreeItem) item;
				ti.setChecked(checked.containsKey(data));
				ti.setGrayed(grayed.containsKey(data));
			}
		}
		applyState(checked, grayed, item);
	}
}
/**
 * Notifies any check state listeners that the check state of an element has changed.
 * Only listeners registered at the time this method is called are notified.
 *
 * @param event a check state changed event
 *
 * @see ICheckStateListener#checkStateChanged
 */
protected void fireCheckStateChanged(final CheckStateChangedEvent event) {
	Object[] array = checkStateListeners.getListeners();
	for (int i = 0; i < array.length; i ++) {
		final ICheckStateListener l = (ICheckStateListener)array[i];
		Platform.run(new SafeRunnable() {
			public void run() {
				l.checkStateChanged(event);
			}
		});
	}	
	
}
/**
 * Gathers the checked and grayed states of the given widget and its
 * descendents.
 *
 * @param checked a writeable set of elements (element type: <code>Object</code>) 
 * @param grayed a writeable set of elements (element type: <code>Object</code>) 
 * @param widget the widget
 */
private void gatherState(CustomHashtable checked, CustomHashtable grayed, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		if (item instanceof TreeItem) {
			Object data = item.getData();
			if (data != null) {
				TreeItem ti = (TreeItem) item;
				if (ti.getChecked())
					checked.put(data, data);
				if (ti.getGrayed())
					grayed.put(data, data);
			}
		}
		gatherState(checked, grayed, item);
	}
}
/* (non-Javadoc)
 * Method declared on ICheckable.
 */
public boolean getChecked(Object element) {
	Widget widget = findItem(element);
	if (widget instanceof TreeItem)
		return ((TreeItem) widget).getChecked();
	return false;
}
/**
 * Returns a list of checked elements in this viewer's tree, 
 * including currently hidden ones that are marked as
 * checked but are under a collapsed ancestor.
 * <p>
 * This method is typically used when preserving the interesting
 * state of a viewer; <code>setCheckedElements</code> is used during the restore.
 * </p>
 *
 * @return the array of checked elements
 *
 * @see #setCheckedElements
 */
public Object[] getCheckedElements() {
	ArrayList v = new ArrayList();
	Control tree = getControl();
	internalCollectChecked(v, tree);
	return v.toArray();
}
/**
 * Returns the grayed state of the given element.
 *
 * @param element the element
 * @return <code>true</code> if the element is grayed,
 *   and <code>false</code> if not grayed
 */
public boolean getGrayed(Object element) {
	Widget widget = findItem(element);
	if (widget instanceof TreeItem) {
		return ((TreeItem) widget).getGrayed();
	}
	return false;
}
/**
 * Returns a list of grayed elements in this viewer's tree, 
 * including currently hidden ones that are marked as
 * grayed but are under a collapsed ancestor.
 * <p>
 * This method is typically used when preserving the interesting
 * state of a viewer; <code>setGrayedElements</code> is used during the restore.
 * </p>
 *
 * @return the array of grayed elements
 *
 * @see #setGrayedElements
 */
public Object[] getGrayedElements() {
	List result = new ArrayList();
	internalCollectGrayed(result, getControl());
	return result.toArray();
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void handleDoubleSelect(SelectionEvent event) {

	if (lastClickedItem != null) {
		TreeItem item = lastClickedItem;
		Object data = item.getData();
		if (data != null) {
			boolean state = item.getChecked();
			setChecked(data, !state);
			fireCheckStateChanged(new CheckStateChangedEvent(this, data, !state));
		}
		lastClickedItem = null;
	} else
		super.handleDoubleSelect(event);
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void handleSelect(SelectionEvent event) {

	lastClickedItem = null;
	if (event.detail == SWT.CHECK) {
		TreeItem item = (TreeItem) event.item;
		lastClickedItem = item;
		super.handleSelect(event);

		Object data = item.getData();
		if (data != null) {
			fireCheckStateChanged(new CheckStateChangedEvent(this, data, item.getChecked()));
		}
	} else
		super.handleSelect(event);
}
/**
 * Gathers the checked states of the given widget and its
 * descendents, following a pre-order traversal of the tree.
 *
 * @param result a writeable list of elements (element type: <code>Object</code>)
 * @param widget the widget
 */
private void internalCollectChecked(List result, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		if (item instanceof TreeItem && ((TreeItem) item).getChecked()) {
			Object data = item.getData();
			if (data != null)
				result.add(data);
		}
		internalCollectChecked(result, item);
	}
}
/**
 * Gathers the grayed states of the given widget and its
 * descendents, following a pre-order traversal of the tree.
 *
 * @param result a writeable list of elements (element type: <code>Object</code>)
 * @param widget the widget
 */
private void internalCollectGrayed(List result, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		Item item = items[i];
		if (item instanceof TreeItem && ((TreeItem) item).getGrayed()) {
			Object data = item.getData();
			if (data != null)
				result.add(data);
		}
		internalCollectGrayed(result, item);
	}
}
/**
 * Sets the checked state of all items to correspond to the given set of checked elements.
 *
 * @param checkedElements the set (element type: <code>Object</code>) of elements which are checked
 * @param widget the widget
 */
private void internalSetChecked(CustomHashtable checkedElements, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		TreeItem item = (TreeItem) items[i];
		Object data = item.getData();
		if (data != null) {
			boolean checked = checkedElements.containsKey(data);
			if (checked != item.getChecked()) {
				item.setChecked(checked);
			}
		}
		internalSetChecked(checkedElements, item);
	}
}
/**
 * Sets the grayed state of all items to correspond to the given set of grayed elements.
 *
 * @param grayedElements the set (element type: <code>Object</code>) of elements which are grayed
 * @param widget the widget
 */
private void internalSetGrayed(CustomHashtable grayedElements, Widget widget) {
	Item[] items = getChildren(widget);
	for (int i = 0; i < items.length; i++) {
		TreeItem item = (TreeItem) items[i];
		Object data = item.getData();
		if (data != null) {
			boolean grayed = grayedElements.containsKey(data);
			if (grayed != item.getGrayed()) {
				item.setGrayed(grayed);
			}
		}
		internalSetGrayed(grayedElements, item);
	}
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
protected void preservingSelection(Runnable updateCode) {

	int n = getItemCount(getControl());
	CustomHashtable checkedNodes = newHashtable(n*2+1);
	CustomHashtable grayedNodes = newHashtable(n*2+1);

	gatherState(checkedNodes, grayedNodes, getControl());

	super.preservingSelection(updateCode);

	applyState(checkedNodes, grayedNodes, getControl());
}
/* (non-Javadoc)
 * Method declared on ICheckable.
 */
public void removeCheckStateListener(ICheckStateListener listener) {
	checkStateListeners.remove(listener);
}
/* (non-Javadoc)
 * Method declared on ICheckable.
 */
public boolean setChecked(Object element, boolean state) {
	Assert.isNotNull(element);
	Widget widget = internalExpand(element, false);
	if (widget instanceof TreeItem) {
		((TreeItem) widget).setChecked(state);
		return true;
	}
	return false;
}
/**
 * Sets the checked state for the children of the given item.
 *
 * @param item the item
 * @param state <code>true</code> if the item should be checked,
 *  and <code>false</code> if it should be unchecked
 */
private void setCheckedChildren(Item item, boolean state) {
	createChildren(item);
	Item[] items = getChildren(item);
	if (items != null) {
		for (int i = 0; i < items.length; i++) {
			Item it = items[i];
			if (it.getData() != null && (it instanceof TreeItem)) {
				TreeItem treeItem = (TreeItem) it;
				treeItem.setChecked(state);
				setCheckedChildren(treeItem, state);
			}
		}
	}
}
/**
 * Sets which elements are checked in this viewer's tree.
 * The given list contains the elements that are to be checked;
 * all other elements are to be unchecked.
 * <p>
 * This method is typically used when restoring the interesting
 * state of a viewer captured by an earlier call to <code>getCheckedElements</code>.
 * </p>
 *
 * @param elements the array of checked elements
 * @see #getCheckedElements
 */
public void setCheckedElements(Object[] elements) {
	assertElementsNotNull(elements);
	CustomHashtable checkedElements = newHashtable(elements.length*2+1);
	for (int i = 0; i < elements.length; ++i) {
	    Object element = elements[i];
		// Ensure item exists for element
		internalExpand(element, false);
		checkedElements.put(element, element);
	}
	Control tree = getControl();
	tree.setRedraw(false);
	internalSetChecked(checkedElements, tree);
	tree.setRedraw(true);
}
/**
 * Sets the grayed state for the given element in this viewer.
 *
 * @param element the element
 * @param state <code>true</code> if the item should be grayed,
 *  and <code>false</code> if it should be ungrayed
 * @return <code>true</code> if the gray state could be set, 
 *  and <code>false</code> otherwise
 */
public boolean setGrayed(Object element, boolean state) {
	Assert.isNotNull(element);
	Widget widget = internalExpand(element, false);
	if (widget instanceof TreeItem) {
		((TreeItem) widget).setGrayed(state);
		return true;
	}
	return false;
}

/**
 * Check and gray the selection rather than calling both
 * setGrayed and setChecked as an optimization.
 * @param element the item being checked
 * @param state a boolean indicating selection or deselection
 * @return boolean indicating success or failure.
 */
public boolean setGrayChecked(Object element, boolean state) {
	Assert.isNotNull(element);
	Widget widget = internalExpand(element, false);
	if (widget instanceof TreeItem) {
		TreeItem item = (TreeItem) widget;
		item.setChecked(state);
		item.setGrayed(state);
		return true;
	}
	return false;
}

/**
 * Sets which elements are grayed in this viewer's tree.
 * The given list contains the elements that are to be grayed;
 * all other elements are to be ungrayed.
 * <p>
 * This method is typically used when restoring the interesting
 * state of a viewer captured by an earlier call to <code>getGrayedElements</code>.
 * </p>
 *
 * @param elements the array of grayed elements
 *
 * @see #getGrayedElements
 */
public void setGrayedElements(Object[] elements) {
	assertElementsNotNull(elements);
	CustomHashtable grayedElements = newHashtable(elements.length*2+1);
	for (int i = 0; i < elements.length; ++i) {
	    Object element = elements[i];
		// Ensure item exists for element
		internalExpand(element, false);
		grayedElements.put(element, element);
	}
	Control tree = getControl();
	tree.setRedraw(false);
	internalSetGrayed(grayedElements, tree);
	tree.setRedraw(true);
}
/**
 * Sets the grayed state for the given element and its parents
 * in this viewer.
 *
 * @param element the element
 * @param state <code>true</code> if the item should be grayed,
 *  and <code>false</code> if it should be ungrayed
 * @return <code>true</code> if the element is visible and the gray
 *  state could be set, and <code>false</code> otherwise
 * @see #setGrayed
 */
public boolean setParentsGrayed(Object element, boolean state) {
	Assert.isNotNull(element);
	Widget widget = internalExpand(element, false);
	if (widget instanceof TreeItem) {
		TreeItem item = (TreeItem) widget;
		item.setGrayed(state);
		item = item.getParentItem();
		while (item != null) {
			item.setGrayed(state);
			item = item.getParentItem();
		}
		return true;
	}
	return false;
}
/**
 * Sets the checked state for the given element and its visible
 * children in this viewer.
 * Assumes that the element has been expanded before. To enforce
 * that the item is expanded, call <code>expandToLevel</code>
 * for the element.
 *
 * @param element the element
 * @param state <code>true</code> if the item should be checked,
 *  and <code>false</code> if it should be unchecked
 * @return <code>true</code> if the checked state could be set, 
 *  and <code>false</code> otherwise
 */
public boolean setSubtreeChecked(Object element, boolean state) {
	Widget widget = internalExpand(element, false);
	if (widget instanceof TreeItem) {
		TreeItem item = (TreeItem) widget;
		item.setChecked(state);
		setCheckedChildren(item, state);
		return true;
	}
	return false;
}
}
