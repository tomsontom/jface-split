/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.Comparator;
import java.util.ResourceBundle;

import org.eclipse.jface.bindings.keys.Key;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ModifierKey;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A key formatter providing the Emacs-style accelerators using single letters
 * to represent the modifier keys.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class EmacsKeyFormatter extends AbstractKeyFormatter {

	/**
	 * A comparator that guarantees that modifier keys will be sorted the same
	 * across different platforms.
	 */
	private static final Comparator EMACS_MODIFIER_KEY_COMPARATOR = new AlphabeticModifierKeyComparator();

	/**
	 * The resource bundle used by <code>format()</code> to translate formal
	 * string representations by locale.
	 */
	private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(EmacsKeyFormatter.class.getName());

	/**
	 * Formats an individual key into a human readable format. This converts the
	 * key into a format similar to Xemacs.
	 * 
	 * @param key
	 *            The key to format; must not be <code>null</code>.
	 * @return The key formatted as a string; should not be <code>null</code>.
	 */
	public String format(Key key) {
		if (key instanceof ModifierKey) {
			String formattedName = Util.translateString(RESOURCE_BUNDLE, key
					.toString(), null);
			if (formattedName != null) {
				return formattedName;
			}
		}

		return super.format(key).toLowerCase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyDelimiter()
	 */
	protected String getKeyDelimiter() {
		return Util.translateString(RESOURCE_BUNDLE, KEY_DELIMITER_KEY,
				KeyStroke.KEY_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
	 */
	protected String getKeyStrokeDelimiter() {
		return Util.translateString(RESOURCE_BUNDLE, KEY_STROKE_DELIMITER_KEY,
				KeySequence.KEY_STROKE_DELIMITER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.keys.AbstractKeyFormatter#getModifierKeyComparator()
	 */
	protected Comparator getModifierKeyComparator() {
		return EMACS_MODIFIER_KEY_COMPARATOR;
	}
}
