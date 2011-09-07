/*
 * Copyright 2011 mapsforge.org
 *
 *	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.muxe.advancedtouristmap.routing;

import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.MercatorProjection;

/**
 * @author Eike Send
 */
public class AngleCalc {

	private static final int MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING = 10;

	/**
	 * Calculate the angle between two IEdge objects / streets
	 * 
	 * @param edge1
	 *            the IEdge of the street before the crossing
	 * @param edge2
	 *            the IEdge of the street after the crossing
	 * @return the angle between the given streets
	 */
	public static double getAngleOfEdges(Edge edge1, Edge edge2) {
		if (edge1 != null && edge2 != null) {
			// Let's see if i can get the angle between the last street and this
			// This is the crossing
			GeoCoordinate crossingCoordinate = edge2.getAllWaypoints()[0];
			// The following is the last coordinate before the crossing
			GeoCoordinate coordinateBefore = edge1.getAllWaypoints()[edge1.getAllWaypoints().length - 2];
			// Take a coordinate further away from the crossing if it's too close
			if (coordinateBefore.sphericalDistance(crossingCoordinate) < MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING
					&& edge1.getAllWaypoints().length > 2) {
				coordinateBefore = edge1.getAllWaypoints()[edge1.getAllWaypoints().length - 3];
			}
			// Here comes the first coordinate after the crossing
			GeoCoordinate coordinateAfter = edge2.getAllWaypoints()[1];
			if (coordinateAfter.sphericalDistance(crossingCoordinate) < MIN_DISTANCE_TO_JUNCTION_FOR_ANGLE_MEASURING
					&& edge2.getAllWaypoints().length > 2) {
				coordinateAfter = edge2.getAllWaypoints()[2];
			}
			double delta = getAngleOfCoords(coordinateBefore, crossingCoordinate,
					coordinateAfter);
			return delta;
		}
		return -360;
	}

	static double getAngleOfCoords(GeoCoordinate lastCoordinate,
			GeoCoordinate crossingCoordinate, GeoCoordinate firstCoordinate) {
		double delta;
		// calculate angles of the incoming street
		double deltaY = MercatorProjection.latitudeToMetersY(crossingCoordinate.getLatitude())
				- MercatorProjection.latitudeToMetersY(lastCoordinate.getLatitude());
		double deltaX = MercatorProjection
				.longitudeToMetersX(crossingCoordinate.getLongitude())
				- MercatorProjection.longitudeToMetersX(lastCoordinate.getLongitude());
		double alpha = java.lang.Math.toDegrees(java.lang.Math.atan(deltaX / deltaY));
		if (deltaY < 0) {
			alpha += 180; // this compensates for the atan result being between -90 and +90
		}
		// deg
		// calculate angles of the outgoing street
		deltaY = MercatorProjection.latitudeToMetersY(firstCoordinate.getLatitude())
				- MercatorProjection.latitudeToMetersY(crossingCoordinate.getLatitude());
		deltaX = MercatorProjection.longitudeToMetersX(firstCoordinate.getLongitude())
				- MercatorProjection.longitudeToMetersX(crossingCoordinate.getLongitude());
		double beta = java.lang.Math.toDegrees(java.lang.Math.atan(deltaX / deltaY));
		if (deltaY < 0) {
			beta += 180; // this compensates for the atan result being between -90 and +90
		}
		// deg
		// the angle difference is angle of the turn,
		delta = alpha - beta;
		// For some reason the angle is conterclockwise, so it's turned around
		delta = 360 - delta;
		// make sure there are no values above 360 or below 0
		delta = java.lang.Math.round((delta + 360) % 360);
		return delta;
	}
}
