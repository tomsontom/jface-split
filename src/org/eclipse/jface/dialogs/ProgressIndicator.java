/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
/**
 * A control for showing progress feedback for a long running operation. This
 * control supports both determinate and indeterminate SWT progress bars. For
 * indeterminate progress, we don't have to know the total amount of work in
 * advance and no <code>worked</code> method needs to be called.
 */
public class ProgressIndicator extends Composite {
	private final static int PROGRESS_MAX = 1000; // value to use for max in
												  // progress bar
	private boolean animated = true;
	private StackLayout layout;
	private ProgressBar determinateProgressBar;
	private ProgressBar indeterminateProgressBar;
	private double totalWork;
	private double sumWorked;
	/**
	 * Create a ProgressIndicator as a child under the given parent.
	 * 
	 * @param parent
	 *            The widgets parent
	 */
	public ProgressIndicator(Composite parent) {
		super(parent, SWT.NULL);
		determinateProgressBar = new ProgressBar(this, SWT.HORIZONTAL);
		indeterminateProgressBar = new ProgressBar(this, SWT.HORIZONTAL
				| SWT.INDETERMINATE);
		layout = new StackLayout();
		setLayout(layout);
	}
	/**
	 * Initialize the progress bar to be animated.
	 */
	public void beginAnimatedTask() {
		done();
		layout.topControl = indeterminateProgressBar;
		layout();
		animated = true;
	}
	/**
	 * Initialize the progress bar.
	 * 
	 * @param max
	 *            The maximum value.
	 */
	public void beginTask(int max) {
		done();
		this.totalWork = max;
		this.sumWorked = 0;
		determinateProgressBar.setMinimum(0);
		determinateProgressBar.setMaximum(PROGRESS_MAX);
		determinateProgressBar.setSelection(0);
		layout.topControl = determinateProgressBar;
		layout();
		animated = false;
	}
	/**
	 * Progress is done.
	 */
	public void done() {
		if (!animated) {
			determinateProgressBar.setMinimum(0);
			determinateProgressBar.setMaximum(0);
			determinateProgressBar.setSelection(0);
		}
		layout.topControl = null;
		layout();
	}
	/**
	 * Moves the progress indicator to the end.
	 */
	public void sendRemainingWork() {
		worked(totalWork - sumWorked);
	}
	/**
	 * Moves the progress indicator by the given amount of work units
	 * @param work the amount of work to increment by.
	 */
	public void worked(double work) {
		if (work == 0 || animated) {
			return;
		}
		sumWorked += work;
		if (sumWorked > totalWork) {
			sumWorked = totalWork;
		}
		if (sumWorked < 0) {
			sumWorked = 0;
		}
		int value = (int) (sumWorked / totalWork * PROGRESS_MAX);
		if (determinateProgressBar.getSelection() < value) {
			determinateProgressBar.setSelection(value);
		}
	}
}