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
package org.eclipse.jface.wizard;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Shell;
/**
 * The IWizardBlockedHandler is the handler used by
 * WizardDialogs to provide extra information when a
 * blocking has occured. There is one static instance
 * of this class used by WizardDialog.
 * @see IProgressMonitor.clearBlocked()
 * @see IProgressMonitor.setBlocked();
 * @see WizardDialog
 */
public interface IWizardBlockedHandler {
	/**
	 * The blockage has been cleared. Clear the
	 * extra information and resume.
	 */
	public void clearBlocked();
	
	/**
	 * A blockage has occured. Show the blockage and 
	 * forward any actions to blockingMonitor.
	 * @param parentShell The shell this is being sent from.
	 * @param blockingMonitor The monitor to forward to. This is most
	 * important for calls to <code>cancel()</code>.
	 * @param blockingStatus The status that describes the blockage
	 * @param blockedName The name of the locked operation.
	 */
	public void showBlocked(Shell parentShell, IProgressMonitor blocking, IStatus blockingStatus,
			String blockedName);
}