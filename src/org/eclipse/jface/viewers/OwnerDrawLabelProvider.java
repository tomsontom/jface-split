/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * OwnerDrawLabelProvider is an abstract implementation of a label provider that
 * handles custom draw.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
public abstract class OwnerDrawLabelProvider extends CellLabelProvider {

	/**
	 * Set up the owner draw callbacks for the viewer.
	 * 
	 * @param viewer
	 */
	public static void setUpOwnerDraw(final ColumnViewer viewer) {
		viewer.getControl().addListener(SWT.MeasureItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				CellLabelProvider provider = viewer
						.getViewerColumn(event.index).getLabelProvider();
				Object element = event.item.getData();

				if (provider instanceof OwnerDrawLabelProvider)
					((OwnerDrawLabelProvider) provider).measure(event, element);
			}
		});

		viewer.getControl().addListener(SWT.PaintItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				CellLabelProvider provider = viewer
						.getViewerColumn(event.index).getLabelProvider();
				Object element = event.item.getData();

				if (provider instanceof OwnerDrawLabelProvider)
					((OwnerDrawLabelProvider) provider).paint(event, element);
			}
		});

		viewer.getControl().addListener(SWT.EraseItem, new Listener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {

				CellLabelProvider provider = getLabelProvider(viewer, event);
				Object element = getElement(event);

				if (provider instanceof OwnerDrawLabelProvider)
					((OwnerDrawLabelProvider) provider).erase(event, element);

			}

			/**
			 * Return the item for the event
			 * 
			 * @param event
			 * @return Object
			 */
			private Object getElement(Event event) {
				return event.item.getData();
			}

			/**
			 * Return the label provider for the column.
			 * 
			 * @param viewer
			 * @param event
			 * @return CellLabelProvider
			 */
			private CellLabelProvider getLabelProvider(
					final ColumnViewer viewer, Event event) {
				return viewer.getViewerColumn(event.index).getLabelProvider();
			}
		});
	}

	/**
	 * Handle the erase event.
	 * 
	 * @param event
	 * @param element
	 * @see SWT#EraseItem
	 */
	protected abstract void erase(Event event, Object element);

	/**
	 * Handle the paint event.
	 * 
	 * @param event
	 * @param element
	 * @see SWT#PaintItem
	 */
	protected abstract void paint(Event event, Object element);

	/**
	 * Handle the measure event.
	 * 
	 * @param event
	 * @param element
	 * @see SWT#MeasureItem
	 */
	protected abstract void measure(Event event, Object element);

	/**
	 * Create a new instance of the receiver based on a column viewer.
	 * 
	 */
	public OwnerDrawLabelProvider() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	public void update(ViewerCell cell) {
		// Force a redraw
		Rectangle cellBounds = cell.getBounds();
		cell.getControl().redraw(cellBounds.x, cellBounds.y, cellBounds.width,
				cellBounds.height, true);

	}

}