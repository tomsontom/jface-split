/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.bindings.keys.formatting;

import java.util.Comparator;

import org.eclipse.jface.bindings.keys.Key;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;


/**
 * <p>
 * Formats the keys in the internal key sequence grammar. This is used for
 * persistence, and is not really intended for display to the user.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class FormalKeyFormatter extends AbstractKeyFormatter {

    /**
     * A comparator that guarantees that modifier keys will be sorted the same
     * across different platforms.
     */
    private static final Comparator FORMAL_MODIFIER_KEY_COMPARATOR = new AlphabeticModifierKeyComparator();

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.KeyFormatter#format(org.eclipse.ui.keys.KeySequence)
     */
    public String format(Key key) {
        return key.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyDelimiter()
     */
    protected String getKeyDelimiter() {
        return KeyStroke.KEY_DELIMITER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getKeyStrokeDelimiter()
     */
    protected String getKeyStrokeDelimiter() {
        return KeySequence.KEY_STROKE_DELIMITER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.keys.AbstractKeyFormatter#getModifierKeyComparator()
     */
    protected Comparator getModifierKeyComparator() {
        return FORMAL_MODIFIER_KEY_COMPARATOR;
    }
}
