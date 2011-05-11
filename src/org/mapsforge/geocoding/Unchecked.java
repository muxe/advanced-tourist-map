/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
