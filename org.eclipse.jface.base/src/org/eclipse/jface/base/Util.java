package org.eclipse.jface.base;

public class Util {
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
	 *            The left array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @param rightArray
	 *            The right array to compare; may be <code>null</code>, and
	 *            may be empty and may contain <code>null</code> elements.
	 * @return <code>true</code> if the arrays are equal length and the
	 *         elements at the same position are equal; <code>false</code>
	 *         otherwise.
	 */
	public static final boolean equals(final Object[] leftArray,
			final Object[] rightArray) {
		if (leftArray == rightArray) {
			return true;
		}

		if (leftArray == null) {
			return (rightArray == null);
		} else if (rightArray == null) {
			return false;
		}

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
	 * Tests whether the first array ends with the second array.
	 * 
	 * @param left
	 *            The array to check (larger); may be <code>null</code>.
	 * @param right
	 *            The array that should be a subsequence (smaller); may be
	 *            <code>null</code>.
	 * @param equals
	 *            Whether the two array are allowed to be equal.
	 * @return <code>true</code> if the second array is a subsequence of the
	 *         array list, and they share end elements.
	 */
	public static final boolean endsWith(final Object[] left,
			final Object[] right, final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!equals(left[l - i - 1], right[r - i - 1])) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Checks whether the second array is a subsequence of the first array, and
	 * that they share common starting elements.
	 * 
	 * @param left
	 *            The first array to compare (large); may be <code>null</code>.
	 * @param right
	 *            The second array to compare (small); may be <code>null</code>.
	 * @param equals
	 *            Whether it is allowed for the two arrays to be equivalent.
	 * @return <code>true</code> if the first arrays starts with the second
	 *         list; <code>false</code> otherwise.
	 */
	public static final boolean startsWith(final Object[] left,
			final Object[] right, final boolean equals) {
		if (left == null || right == null) {
			return false;
		}

		int l = left.length;
		int r = right.length;

		if (r > l || !equals && r == l) {
			return false;
		}

		for (int i = 0; i < r; i++) {
			if (!equals(left[i], right[i])) {
				return false;
			}
		}

		return true;
	}
}
