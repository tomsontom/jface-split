package org.eclipse.jface.internal.base;

import java.util.Comparator;

public class Policy {
	private static Comparator viewerComparator;

	/**
	 * Return the default comparator used by JFace to sort strings.
	 * 
	 * @return a default comparator used by JFace to sort strings
	 */
	private static Comparator getDefaultComparator() {
		return new Comparator() {
			/**
			 * Compares string s1 to string s2.
			 * 
			 * @param s1
			 *            string 1
			 * @param s2
			 *            string 2
			 * @return Returns an integer value. Value is less than zero if
			 *         source is less than target, value is zero if source and
			 *         target are equal, value is greater than zero if source is
			 *         greater than target.
			 * @exception ClassCastException
			 *                the arguments cannot be cast to Strings.
			 */
			public int compare(Object s1, Object s2) {
				return ((String) s1).compareTo((String) s2);
			}
		};
	}
	
	/**
	 * Return the comparator used by JFace to sort strings.
	 * 
	 * @return the comparator used by JFace to sort strings
	 * @since 3.2
	 */
	public static Comparator getComparator() {
		if (viewerComparator == null) {
			viewerComparator = getDefaultComparator();
		}
		return viewerComparator;
	}

	/**
	 * Sets the comparator used by JFace to sort strings.
	 * 
	 * @param comparator
	 *            comparator used by JFace to sort strings
	 * @since 3.2
	 */
	public static void setComparator(Comparator comparator) {
		org.eclipse.core.runtime.Assert.isTrue(viewerComparator == null);
		viewerComparator = comparator;
	}
}
