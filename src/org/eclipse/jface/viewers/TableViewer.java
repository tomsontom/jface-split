package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
/**
 * A concrete viewer based on a SWT <code>Table</code> control.
 * <p>
 * This class is not intended to be subclassed outside the viewer framework. 
 * It is designed to be instantiated with a pre-existing SWT table control and configured
 * with a domain-specific content provider, table label provider, element filter (optional),
 * and element sorter (optional).
 * </p>
 * <p>
 * Label providers for table viewers must implement either the <code>ITableLabelProvider</code>
 * or the <code>ILabelProvider</code> interface (see <code>TableViewer.setLabelProvider</code> 
 * for more details).
 * </p>
 */
public class TableViewer extends StructuredViewer {

	/**
	 * Internal table viewer implementation.
	 */
	private TableViewerImpl tableViewerImpl;

	/**
	 * This viewer's table control.
	 */
	private Table table;

	/**
	 * This viewer's table editor.
	 */
	private TableEditor tableEditor;
/**
 * Creates a table viewer on a newly-created table control under the given parent.
 * The table control is created using the SWT style bits <code>MULTI, H_SCROLL, V_SCROLL,</code> and <code>BORDER</code>.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters. The table has no columns.
 *
 * @param parent the parent control
 */
public TableViewer(Composite parent) {
	this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
}
/**
 * Creates a table viewer on a newly-created table control under the given parent.
 * The table control is created using the given style bits.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters. The table has no columns.
 *
 * @param parent the parent control
 * @param style SWT style bits
 */
public TableViewer(Composite parent, int style) {
	this(new Table(parent, style));
}
/**
 * Creates a table viewer on the given table control.
 * The viewer has no input, no content provider, a default label provider, 
 * no sorter, and no filters. 
 *
 * @param table the table control
 */
public TableViewer(Table table) {
	this.table = table;
	hookControl(table);
	tableEditor = new TableEditor(table);
	initTableViewerImpl();
}
/**
 * Adds the given elements to this table viewer.
 * If this viewer does not have a sorter, the elements are added at the end
 * in the order given; otherwise the elements are inserted at appropriate positions.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param elements the elements to add
 */
public void add(Object[] elements) {
	Object[] filtered = filter(elements);
	for (int i = 0; i < filtered.length; i++){
		Object element = filtered[i];
		int index = indexForElement(element);
		updateItem(new TableItem(getTable(), SWT.NONE, index), element);
	}
}
/**
 * Adds the given element to this table viewer.
 * If this viewer does not have a sorter, the element is added at the end;
 * otherwise the element is inserted at the appropriate position.
 * <p>
 * This method should be called (by the content provider) when a single element 
 * has been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * Note that there is another method for efficiently processing the simultaneous
 * addition of multiple elements.
 * </p>
 *
 * @param element the element to add
 */
public void add(Object element) {
	add(new Object[] { element });
}
/**
 * Cancels a currently active cell editor. All changes already done in the cell
 * editor are lost.
 */
public void cancelEditing() {
	tableViewerImpl.cancelEditing();
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected Widget doFindInputItem(Object element) {
	if (element.equals(getRoot()))
		return getTable();
	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected Widget doFindItem(Object element) {
	TableItem[] children = table.getItems();
	for (int i = 0; i < children.length; i++) {
		TableItem item = children[i];
		Object data = item.getData();
		if (data != null && data.equals(element))
			return item;
	}

	return null;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void doUpdateItem(Widget widget, Object element, boolean fullMap) {
	if (widget instanceof TableItem) {
		TableItem item = (TableItem) widget;
		
		// remember element we are showing
		if (fullMap) {
			associate(element, item);
		} else {
			item.setData(element);
			mapElement(element, item);	
		}
	
		IBaseLabelProvider prov = getLabelProvider();
		ITableLabelProvider tprov = null;
		ILabelProvider lprov = null;
		if (prov instanceof ITableLabelProvider) {
			tprov = (ITableLabelProvider) prov;
		}
		else {
			lprov = (ILabelProvider) prov;
		}
		int columnCount = table.getColumnCount();
		// Also enter loop if no columns added.  See 1G9WWGZ: JFUIF:WINNT - TableViewer with 0 columns does not work
		for (int column = 0; column < columnCount || column == 0; column++) {
			// Similar code in TableTreeViewer.doUpdateItem()
		    TableItem ti = (TableItem) item;
			String text = "";//$NON-NLS-1$
			Image image = null;
			if (tprov != null) {
				text = tprov.getColumnText(element, column);
				image = tprov.getColumnImage(element, column);
			}
			else {
				if (column == 0) {
		 	    	text = lprov.getText(element);
		    		image = lprov.getImage(element);		    	
		    	}
	    	}
			ti.setText(column, text);
			if (ti.getImage(column) != image) {
				ti.setImage(column, image);
			}
		}
	}
}
/**
 * Starts editing the given element.
 *
 * @param element the element
 * @param column the column number
 */
public void editElement(Object element, int column) {
	tableViewerImpl.editElement(element,column);
}
/**
 * Returns the cell editors of this table viewer.
 *
 * @return the list of cell editors
 */
public CellEditor[] getCellEditors() {
	return tableViewerImpl.getCellEditors();
}
/**
 * Returns the cell modifier of this table viewer.
 *
 * @return the cell modifier
 */
public ICellModifier getCellModifier() {
	return tableViewerImpl.getCellModifier();
}
/**
 * Returns the column properties of this table viewer.
 * The properties must correspond with the columns of the table control.
 * They are used to identify the column in a cell modifier.
 *
 * @return the list of column properties
 */
public Object[] getColumnProperties() {
	return tableViewerImpl.getColumnProperties();
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
public Control getControl() {
	return table;
}
/**
 * Returns the element with the given index from this table viewer.
 * Returns <code>null</code> if the index is out of range.
 * <p>
 * This method is internal to the framework.
 * </p>
 *
 * @param index the zero-based index
 * @return the element at the given index, or <code>null</code> if the
 *   index is out of range
 */
public Object getElementAt(int index) {
	if (index >= 0 && index < table.getItemCount()) {
		TableItem i = table.getItem(index);
		if(i != null)
		    return i.getData();
	}
	return null;
}
/**
 * The table viewer implementation of this <code>Viewer</code> framework
 * method returns the label provider, which in the case of table 
 * viewers will be an instance of either <code>ITableLabelProvider</code>
 * or <code>ILabelProvider</code>.
 * If it is an <code>ITableLabelProvider</code>, then it provides a
 * separate label text and image for each column. If it is an 
 * <code>ILabelProvider</code>, then it provides only the label text 
 * and image for the first column, and any remaining columns are blank.
 */
public IBaseLabelProvider getLabelProvider() {
	return super.getLabelProvider();
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected List getSelectionFromWidget() {
	Widget[] items = table.getSelection();
	ArrayList list = new ArrayList(items.length);
	for (int i = 0; i < items.length; i++) {
		Widget item = items[i];
		Object e = item.getData();
		if (e != null)
			list.add(e);
	}
	return list;
}
/**
 * Returns this table viewer's table control.
 *
 * @return the table control
 */
public Table getTable() {
	return table;
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void hookControl(Control control) {
	super.hookControl(control);
	Table tableControl = (Table)control;
	tableControl.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			tableViewerImpl.handleMouseDown(e);
		}
		public void mouseDoubleClick(MouseEvent e) {
			tableViewerImpl.handleMouseDoubleClick(e);
		}
	});
}
/*
 * Returns the index where the item should be inserted.
*/
protected int indexForElement(Object element) {
	ViewerSorter sorter = getSorter();
	if(sorter == null)
		return table.getItemCount();
	int count = table.getItemCount();
	int min = 0, max = count - 1;
	while (min <= max) {
		int mid = (min + max) / 2;
		Object data = table.getItem(mid).getData();
		int compare = sorter.compare(this, data, element);
		if (compare == 0) {
			// find first item > element
			while (compare == 0) {
				++mid;
				if (mid >= count) {
					break;
				}
				data = table.getItem(mid).getData();
				compare = sorter.compare(this, data, element);
			}
			return mid;
		}
		if (compare < 0)
			min = mid + 1;
		else
			max = mid - 1;
	}
	return min;
}
/**
 * Initializes the table viewer implementation.
 */
private void initTableViewerImpl() {
	tableViewerImpl = new TableViewerImpl() {
		Rectangle getBounds(Item item, int columnNumber) {
			return ((TableItem)item).getBounds(columnNumber);
		}
		int getColumnCount() {
			return getTable().getColumnCount();
		}
		Item[] getSelection() {
			return getTable().getSelection();
		}
		void setEditor(Control w, Item item, int columnNumber) {
			tableEditor.setEditor(w, (TableItem)item, columnNumber);
		}
		void setSelection(StructuredSelection selection, boolean b) {
			TableViewer.this.setSelection(selection,b);
		}
		void showSelection() {
			getTable().showSelection();
		}
		void setLayoutData(CellEditor.LayoutData layoutData) {
			tableEditor.grabHorizontal = layoutData.grabHorizontal;
			tableEditor.horizontalAlignment = layoutData.horizontalAlignment;
			tableEditor.minimumWidth = layoutData.minimumWidth;
		}
	};
}
/* (non-Javadoc)
 * Method declared on Viewer.
 */
protected void inputChanged(Object input, Object oldInput) {
	getControl().setRedraw(false);
	try {
		// refresh() attempts to preserve selection, which we want here
		refresh();
	}
	finally {
		getControl().setRedraw(true);
	}
}
/**
 * Inserts the given element into this table viewer at the given position.
 * If this viewer has a sorter, the position is ignored and the element is inserted
 * at the correct position in the sort order.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been added to the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param element the element
 * @param position a 0-based position relative to the model, or -1 to indicate the last position
 */
public void insert(Object element, int position) {
	tableViewerImpl.applyEditorValue();
	if (getSorter() != null || hasFilters()) {
		add(element);
		return;
	}
	if (position == -1)
		position = table.getItemCount();
	updateItem(new TableItem(table, SWT.NONE, position), element);
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void internalRefresh(Object element) {
	internalRefresh(element, true);
}

/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void internalRefresh(Object element, boolean updateLabels) {
	tableViewerImpl.applyEditorValue();
	if (element == null || element.equals(getRoot())) {
		// the parent

		// in the code below, it is important to do all disassociates
		// before any associates, since a later disassociate can undo an earlier associate
		// e.g. if (a, b) is replaced by (b, a), the disassociate of b to item 1 could undo
		// the associate of b to item 0.
		
		Object[] children = getSortedChildren(getRoot());
		TableItem[] items = table.getItems();
		int min = Math.min(children.length, items.length);
		for (int i = 0; i < min; ++i) {
			// if the element is unchanged, update its label if appropriate
			if (children[i].equals(items[i].getData())) {
				if (updateLabels) {
					updateItem(items[i], children[i]);
				}
			}
			else {
				// updateItem does an associate(...), which can mess up
				// the associations if the order of elements has changed.
				// E.g. (a, b) -> (b, a) first replaces a->0 with b->0, then replaces b->1 with a->1, but this actually removes b->0.
				// So, if the object associated with this item has changed,
				// just disassociate it for now, and update it below.
				disassociate(items[i]);
			}
		}
		
		// dispose of all items beyond the end of the current elements
		if (min < items.length) {
			for (int i = items.length; --i >= min;) {
				disassociate(items[i]);
			}
			table.remove(min, items.length-1);
		}
		
		// Workaround for 1GDGN4Q: ITPUI:WIN2000 - TableViewer icons get scrunched
		if (table.getItemCount() == 0) {
			table.removeAll();
		}

		// Update items which were removed above
		for (int i = 0; i < min; ++i) {
			if (items[i].getData() == null) {
				updateItem(items[i], children[i]);
			}
		}
	
		// add any remaining elements
		for (int i = min; i < children.length; ++i) {
			updateItem(new TableItem(table, SWT.NONE, i), children[i]);
		}
	}
	else {
		Widget w = findItem(element);
		if (w != null) {
			updateItem(w, element);
		}
	}
}
/**
 * Removes the given elements from this table viewer.
 *
 * @param elements the elements to remove
 */
private void internalRemove(final Object[] elements) {
	Object input = getInput();
	for (int i = 0; i < elements.length; ++i) {
		if (elements[i].equals(input)) {
			setInput(null);
			return;
		}
	}
	// use remove(int[]) rather than repeated TableItem.dispose() calls
	// to allow SWT to optimize multiple removals
	int[] indices = new int[elements.length];
	int count = 0;
	for (int i = 0; i < elements.length; ++i) {
		Widget w = findItem(elements[i]);
		if (w instanceof TableItem) {
			TableItem item = (TableItem) w;
			disassociate(item);
			indices[count++] = table.indexOf(item);
		}
	}
	if (count < indices.length) {
		System.arraycopy(indices, 0, indices = new int[count], 0, count);
	}
	table.remove(indices);
	
	// Workaround for 1GDGN4Q: ITPUI:WIN2000 - TableViewer icons get scrunched
	if (table.getItemCount() == 0) {
		table.removeAll();
	}
}
/**
 * Returns whether there is an active cell editor.
 *
 * @return <code>true</code> if there is an active cell editor, and 
 *   <code>false</code> otherwise
 */
public boolean isCellEditorActive() {
	return tableViewerImpl.isCellEditorActive();
}
/**
 * Removes the given elements from this table viewer.
 * The selection is updated if required.
 * <p>
 * This method should be called (by the content provider) when elements 
 * have been removed from the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * </p>
 *
 * @param elements the elements to remove
 */
public void remove(final Object[] elements) {
	preservingSelection(new Runnable() {
		public void run() {
			internalRemove(elements);
		}
	});
}
/**
 * Removes the given element from this table viewer.
 * The selection is updated if necessary.
 * <p>
 * This method should be called (by the content provider) when a single element 
 * has been removed from the model, in order to cause the viewer to accurately
 * reflect the model. This method only affects the viewer, not the model.
 * Note that there is another method for efficiently processing the simultaneous
 * removal of multiple elements.
 * </p>
 *
 * @param element the element
 */
public void remove(Object element) {
	remove(new Object[] { element });
}
/*
 * Non-Javadoc.
 * Method defined on StructuredViewer.
 */
public void reveal(Object element) {
	Widget w = findItem(element);
	if (w instanceof TableItem)
		getTable().showItem((TableItem) w);
}
/**
 * Sets the cell editors of this table viewer.
 *
 * @param editors the list of cell editors
 */
public void setCellEditors(CellEditor[] editors) {
	tableViewerImpl.setCellEditors(editors);
}
/**
 * Sets the cell modifier of this table viewer.
 *
 * @param modifier the cell modifier
 */
public void setCellModifier(ICellModifier modifier) {
	tableViewerImpl.setCellModifier(modifier);
}
/**
 * Sets the column properties of this table viewer.
 * The properties must correspond with the columns of the table control.
 * They are used to identify the column in a cell modifier.
 *
 * @param columnProperties the list of column properties
 */
public void setColumnProperties(String[] columnProperties) {
	tableViewerImpl.setColumnProperties(columnProperties);
}
/**
 * The table viewer implementation of this <code>Viewer</code> framework
 * method ensures that the given label provider is an instance
 * of either <code>ITableLabelProvider</code> or <code>ILabelProvider</code>.
 * If it is an <code>ITableLabelProvider</code>, then it provides a
 * separate label text and image for each column. If it is an 
 * <code>ILabelProvider</code>, then it provides only the label text 
 * and image for the first column, and any remaining columns are blank.
 */
public void setLabelProvider(IBaseLabelProvider labelProvider) {
	Assert.isTrue(labelProvider instanceof ITableLabelProvider || labelProvider instanceof ILabelProvider );
	super.setLabelProvider(labelProvider);
}
/* (non-Javadoc)
 * Method declared on StructuredViewer.
 */
protected void setSelectionToWidget(List list, boolean reveal) {
	if (list == null) {
		table.deselectAll();
		return;
	}
	int size = list.size();
	TableItem[] items = new TableItem[size];
	TableItem firstItem = null;
	int count = 0;
	for (int i = 0; i < size; ++i) {
		Object o = list.get(i);
		Widget w = findItem(o);
		if (w instanceof TableItem) {
			TableItem item = (TableItem) w;
			items[count++] = item;
			if (firstItem == null)
				firstItem = item;
		}
	}
	if (count < size) {
		System.arraycopy(items, 0, items = new TableItem[count], 0, count);
	}
	table.setSelection(items);
	
	if (reveal && firstItem != null) {
		table.showItem(firstItem);
	}
}
}
