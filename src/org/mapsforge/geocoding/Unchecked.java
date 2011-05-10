package org.mapsforge.geocoding;

/**
 * @author Sebastian Kuerten (sebastian.kuerten@fu-berlin.de)
 * 
 * 
 *         Sometimes an unchecked cast cannot be circumvented. Such casts produce warnings. If
 *         you're sure your cast cannot be circumvented you can use the methods of this class to
 *         reduce the number of warnings, since this class 'catches' the warning.
 */
public class Unchecked {

	/**
	 * Perform an unchecked cast.
	 * 
	 * @param <T>
	 *            the implicit type to cast to.
	 * @param o
	 *            the instance to cast.
	 * @return the casted instance.
	 */
	public static <T> T cast(Object o) {
		return (T) o;
	}
}
