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
package org.eclipse.jface.action;


/**
 * This interface is used by instances of <code>IContributionItem</code>
 * to determine if the values for certain properties have been overriden
 * by their manager.
 * <p>
 * This interface is internal to the framework; it should not be implemented outside
 * the framework.
 * </p>
 * 
 * @since 2.0
 */
public interface IContributionManagerOverrides {
	/**
	 * Id for the enabled property. Value is <code>"enabled"</code>.
	 * 
	 * @since 2.0
	 */
	public final static String P_ENABLED = "enabled"; //$NON-NLS-1$
	
	/**
	 * Returns <code>Boolean.TRUE</code> if the given contribution item should 
	 * be enabled, <code>Boolean.FALSE</code> if the item should be disabled, and
	 * <code>null</code> if the item may determine its own enablement.
	 * 
	 * @param item the contribution item for which the enable override value is 
	 * determined
	 * @since 2.0
	 */
	public Boolean getEnabled(IContributionItem item);
	
	/**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1
     */
    public Integer getAccelerator(IContributionItem item);

    /**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1
     */
    public String getAcceleratorText(IContributionItem item);

    /**
     * This is not intended to be called outside of the workbench. This method
     * is intended to be deprecated in 3.1.
     * 
     * TODO deprecate for 3.1
     */
    public String getText(IContributionItem item);
}
