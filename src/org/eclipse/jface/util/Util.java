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

package org.eclipse.jface.util;

import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>
 * A static class providing utility methods to all of JFace.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 */
public final class Util {

	/**
	 * An unmodifiable, empty, sorted set. This value is guaranteed to never
	 * change and never be <code>null</code>.
	 */
	public static final SortedSet EMPTY_SORTED_SET = Collections
			.unmodifiableSortedSet(new TreeSet());

	/**
	 * A common zero-length string. It avoids needing write <code>NON-NLS</code>
	 * next to code fragments. It's also a bit clearer to read.
	 */
	public static final String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	/**
	 * Verifies that the given object is an instance of the given class.
	 * 
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be
	 *            <code>null</code>.
	 */
	public static final void assertInstance(final Object object, final Class c) {
		assertInstance(object, c, false);
	}

	/**
	 * Verifies the given object is an instance of the given class. It is
	 * possible to specify whether the object is permitted to be
	 * <code>null</code>.
	 * 
	 * @param object
	 *            The object to check; may be <code>null</code>.
	 * @param c
	 *            The class which the object should be; must not be
	 *            <code>null</code>.
	 * @param allowNull
	 *            Whether the object is allowed to be <code>null</code>.
	 */
	private static final void assertInstance(final Object object,
			final Class c, final boolean allowNull) {
		if (object == null && allowNull)
			return;

		if (object == null || c == null)
			throw new NullPointerException();
		else if (!c.isInstance(object))
			throw new IllegalArgumentException();
	}

	/**
	 * Compares two boolean values. <code>false</code> is considered to be
	 * "less than" <code>true</code>.
	 * 
	 * @param left
	 *            The left value to compare
	 * @param right
	 *            The right value to compare
	 * @return <code>-1</code> if the left is <code>false</code> and the
	 *         right is <code>true</code>. <code>1</code> if the opposite
	 *         is true. If they are equal, then it returns <code>0</code>.
	 */
	public static final int compare(final boolean left, final boolean right) {
		return left == false ? (right == true ? -1 : 0) : 1;
	}

	/**
	 * Compares to comparable objects -- defending against <code>null</code>.
	 * 
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value.
	 */
	public static final int compare(final Comparable left,
			final Comparable right) {
		if (left == null && right == null)
			return 0;
		else if (left == null)
			return -1;
		else if (right == null)
			return 1;
		else
			return left.compareTo(right);
	}

	/**
	 * Compares two arrays of comparable objects -- accounting for
	 * <code>null</code>.
	 * 
	 * @param left
	 *            The left array to be compared; may be <code>null</code>.
	 * @param right
	 *            The right array to be compared; may be <code>null</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value. A shorter array is considered
	 *         less than a longer array.
	 */
	public static final int compare(final Comparable[] left,
			final Comparable[] right) {
		if (left == null && right == null)
			return 0;
		else if (left == null)
			return -1;
		else if (right == null)
			return 1;
		else {
			int l = left.length;
			int r = right.length;

			if (l != r)
				return l - r;

			for (int i = 0; i < l; i++) {
				int compareTo = compare(left[i], right[i]);

				if (compareTo != 0)
					return compareTo;
			}

			return 0;
		}
	}

	/**
	 * Compares two lists -- account for <code>null</code>. The lists must
	 * contain comparable objects.
	 * 
	 * @param left
	 *            The left list to compare; may be <code>null</code>. This
	 *            list must only contain instances of <code>Comparable</code>.
	 * @param right
	 *            The right list to compare; may be <code>null</code>. This
	 *            list must only contain instances of <code>Comparable</code>.
	 * @return The result of the comparison. <code>null</code> is considered
	 *         to be the least possible value. A shorter list is considered less
	 *         than a longer list.
	 */
	public static final int compare(final List left, final List right) {
		if (left == null && right == null)
			return 0;
		else if (left == null)
			return -1;
		else if (right == null)
			return 1;
		else {
			int l = left.size();
			int r = right.size();

			if (l != r)
				return l - r;

			for (int i = 0; i < l; i++) {
				int compareTo = compare((Comparable) left.get(i),
						(Comparable) right.get(i));

				if (compareTo != 0)
					return compareTo;
			}

			return 0;
		}
	}

	/**
	 * Tests whether the first list ends with the second list.
	 * 
	 * @param left
	 *            The list to check (larger); may be <code>null</code>.
	 * @param right
	 *            The list that should be a subsequence (smaller); may be
	 *            <code>null</code>.
	 * @param equals
	 *            Whether the two lists are allowed to be equal.
	 * @return <code>true</code> if the second list is a subsequence of the
	 *         first list, and they share end elements.
	 */
	public static final boolean endsWith(final List left, final List right,
			final boolean equals) {
		if (left == null || right == null)
			return false;

		int l = left.size();
		int r = right.size();

		if (r > l || !equals && r == l)
			return false;

		for (int i = 0; i < r; i++)
			if (!equals(left.get(l - i - 1), right.get(r - i - 1)))
				return false;

		return true;
	}

	/**
	 * Checks whether the two objects are <code>null</code> -- allowing for
	 * <code>null</code>.
	 * 
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return <code>true</code> if the two objects are equivalent;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean equals(final Object left, final Object right) {
		return left == null ? right == null : ((right != null) && left
				.equals(right));
	}

	/**
	 * Tests whether two arrays of objects are equal to each other. The arrays
	 * must not be <code>null</code>, but their elements may be
	 * <code>null</code>.
	 * 
	 * @param leftArray
	 *            The left array to compare; must not be <code>null</code>,
	 *            but may be empty and may contain <code>null</code> elements.
	 * @param rightArray
	 *            The right array to compare; must not be <code>null</code>,
	 *            but may be empty and may contain <code>null</code> elements.
	 * @return <code>true</code> if the arrays are equal length and the
	 *         elements at the same position are equal; <code>false</code>
	 *         otherwise.
	 */
	public static final boolean equals(final Object[] leftArray,
			final Object[] rightArray) {
		if (leftArray.length != rightArray.length) {
			return false;
		}

		for (int i = 0; i < leftArray.length; i++) {
			final Object left = leftArray[i];
			final Object right = rightArray[i];
			final boolean equal = (left == null) ? (right == null) : (left
					.equals(right));
			if (!equal) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Provides a hash code based on the given integer value.
	 * 
	 * @param i
	 *            The integer value
	 * @return <code>i</code>
	 */
	public static final int hashCode(final int i) {
		return i;
	}

	/**
	 * Provides a hash code for the object -- defending against
	 * <code>null</code>.
	 * 
	 * @param object
	 *            The object for which a hash code is required.
	 * @return <code>object.hashCode</code> or <code>0</code> if
	 *         <code>object</code> if <code>null</code>.
	 */
	public static final int hashCode(final Object object) {
		return object != null ? object.hashCode() : 0;
	}

	/**
	 * Computes the hash code for an array of objects, but with defense against
	 * <code>null</code>.
	 * 
	 * @param objects
	 *            The array of objects for which a hash code is needed; may be
	 *            <code>null</code>.
	 * @return The hash code for <code>objects</code>; or <code>0</code> if
	 *         <code>objects</code> is <code>null</code>.
	 */
	public static final int hashCode(final Object[] objects) {
		if (objects == null) {
			return 0;
		}

		int hashCode = 89;
		for (int i = 0; i < objects.length; i++) {
			final Object object = objects[i];
			if (object != null) {
				hashCode = hashCode * 31 + object.hashCode();
			}
		}

		return hashCode;
	}

	/**
	 * Checks whether the second list is a subsequence of the first list, and
	 * that they share common starting elements.
	 * 
	 * @param left
	 *            The first list to compare (large); may be <code>null</code>.
	 * @param right
	 *            The second list to compare (small); may be <code>null</code>.
	 * @param equals
	 *            Whether it is allowed for the two lists to be equivalent.
	 * @return <code>true</code> if the first list starts with the second
	 *         list; <code>false</code> otherwise.
	 */
	public static final boolean startsWith(final List left, final List right,
			final boolean equals) {
		if (left == null || right == null)
			return false;

		int l = left.size();
		int r = right.size();

		if (r > l || !equals && r == l)
			return false;

		for (int i = 0; i < r; i++)
			if (!equals(left.get(i), right.get(i)))
				return false;

		return true;
	}

	/**
	 * Provides a translation of a particular key from the resource bundle.
	 * 
	 * @param resourceBundle
	 *            The key to look up in the resource bundle; should not be
	 *            <code>null</code>.
	 * @param key
	 *            The key to look up in the resource bundle; should not be
	 *            <code>null</code>.
	 * @param defaultString
	 *            The value to return if the resource cannot be found; may be
	 *            <code>null</code>.
	 * @return The value of the translated resource at <code>key</code>. If
	 *         the key cannot be found, then it is simply the
	 *         <code>defaultString</code>.
	 */
	public static final String translateString(
			final ResourceBundle resourceBundle, final String key,
			final String defaultString) {
		if (resourceBundle != null && key != null)
			try {
				final String translatedString = resourceBundle.getString(key);

				if (translatedString != null)
					return translatedString;
			} catch (MissingResourceException eMissingResource) {
				// Such is life. We'll return the key
			}

		return defaultString;
	}

	/**
	 * This class should never be constructed.
	 */
	private Util() {
		// Not allowed.
	}
}
