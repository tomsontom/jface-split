package org.eclipse.jface.preference;

import java.io.IOException;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

/**
 * IPersistentPreferenceStore is a preference store that can 
 * be saved.
 */
public interface IPersistentPreferenceStore extends IPreferenceStore {

	/**
	 * Saves the non-default-valued preferences known to this preference
	 * store to the file from which they were originally loaded.
	 *
	 * @exception java.io.IOException if there is a problem saving this store
	 */
	public void save() throws IOException;

}