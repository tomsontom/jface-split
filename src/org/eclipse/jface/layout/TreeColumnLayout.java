/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                               - fix for bug 178280
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/

package org.eclipse.jface.layout;


import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;

/**
 * The TreeColumnLayout is the {@link Layout} used to maintain {@link TreeColumn} sizes in a 
 * {@link Tree}.
 * 
 * <p>
 * <b>You can only add the {@link Layout} to a container whose <i>only</i>
 * child is the {@link Tree} control you want the {@link Layout} applied to.
 * Don't assign the layout directly the {@link Tree}</b>
 * </p>
 * 
 * @since 3.3
 */
public class TreeColumnLayout extends AbstractColumnLayout {
	private boolean addListener = true;
	
	private static class TreeLayoutListener implements TreeListener {

		public void treeCollapsed(TreeEvent e) {
			update((Tree) e.widget);
		}

		public void treeExpanded(TreeEvent e) {
			update((Tree) e.widget);
		}
		
		private void update(final Tree tree) {
			tree.getDisplay().asyncExec(new Runnable() {

				public void run() {
					tree.update();
					tree.getParent().layout();
				}
				
			});
		}
		
	}
	
	private static final TreeLayoutListener listener = new TreeLayoutListener();
	
	protected void layout(Composite composite, boolean flushCache) {
		super.layout(composite, flushCache);
		if( addListener ) {
			addListener=false;
			((Tree)getControl(composite)).addTreeListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnLayout#getColumnCount(org.eclipse.swt.widgets.Scrollable)
	 */
	int getColumnCount(Scrollable tree) {
		return ((Tree) tree).getColumnCount();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnLayout#setColumnWidths(org.eclipse.swt.widgets.Scrollable, int[])
	 */
	void setColumnWidths(Scrollable tree, int[] widths) {
		TreeColumn[] columns = ((Tree) tree).getColumns();
		for (int i = 0; i < widths.length; i++) {
			columns[i].setWidth(widths[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnLayout#getLayoutData(org.eclipse.swt.widgets.Scrollable, int)
	 */
	ColumnLayoutData getLayoutData(Scrollable tableTree, int columnIndex) {
		TreeColumn column = ((Tree) tableTree).getColumn(columnIndex);
		return (ColumnLayoutData) column.getData(LAYOUT_DATA);
	}
	
	void updateColumnData(Widget column) {
		TreeColumn tColumn = (TreeColumn) column;
		Tree t = tColumn.getParent();
		
		if( ! IS_GTK || t.getColumn(t.getColumnCount()-1) != tColumn ){
			layout(t.getParent(), true);
			tColumn.setData(LAYOUT_DATA,new ColumnPixelData(tColumn.getWidth()));
		}
	}
}
