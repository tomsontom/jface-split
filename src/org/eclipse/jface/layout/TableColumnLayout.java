/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/

package org.eclipse.jface.layout;


import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The TableColumnAdapter is the ControlAdapter used to maintain Table sizes
 * {@link Table}.
 * 
 * @since 3.3
 */
public class TableColumnLayout extends AbstractColumnLayout {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnLayout#getColumnCount(org.eclipse.swt.widgets.Scrollable)
	 */
	int getColumnCount(Scrollable tableTree) {
		return ((Table) tableTree).getColumnCount();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.layout.AbstractColumnLayout#setColumnWidths(org.eclipse.swt.widgets.Scrollable, int[])
	 */
	void setColumnWidths(Scrollable tableTree, int[] widths) {
		TableColumn[] columns = ((Table) tableTree).getColumns();
		for (int i = 0; i < widths.length; i++) {
			columns[i].setWidth(widths[i]);
		}
	}
}