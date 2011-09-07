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

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.Vertex;

/**
 * A decision point is a point on a route where the user has to change streets.
 */
public class DecisionPoint {
	/**
	 * A human readable name for DecisionPoint (in most cases a street name)
	 */
	private String name;

	/**
	 * The position of the DecisionPoint
	 */
	private GeoPoint geoPoint;

	private int distance;

	private double angleFromPrevious;

	/**
	 * Constructs a DecisionPint out of a name and a GeoPoint
	 * 
	 * @param name
	 *            human readable name string
	 * @param geoPoint
	 *            position in form of a GeoPoint
	 */
	public DecisionPoint(String name, GeoPoint geoPoint) {
		this.name = name;
		this.geoPoint = geoPoint;
		// this.distance = 0;
	}

	/**
	 * Constructs a DecisionPint out of a name and a Vertex
	 * 
	 * @param name
	 *            human readable name string
	 * @param vertex
	 *            position in form of a Vertex
	 */
	public DecisionPoint(String name, Vertex vertex) {
		this.name = name;
		vertex.getCoordinate();
		this.geoPoint = new GeoPoint(vertex.getCoordinate().getLatitude(), vertex
				.getCoordinate().getLongitude());
	}

	@Override
	public String toString() {
		return this.name + " (" + this.distance + "m " + angleToString(this.angleFromPrevious)
				+ ")";
	}

	/**
	 * Getter for the position
	 * 
	 * @return position in form of a GeoPoint
	 */
	public GeoPoint getGeoPoint() {
		return this.geoPoint;
	}

	/**
	 * Getter for the name
	 * 
	 * @return human readable name
	 */
	public String getName() {
		return this.name;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getDistance() {
		return this.distance;
	}

	public void setAngleFromPrevious(double angleFromPrevious) {
		this.angleFromPrevious = angleFromPrevious;
	}

	public double getAngleFromPrevious() {
		return this.angleFromPrevious;
	}

	private String angleToString(double angle) {
		if (angle > 337 || angle < 22) {
			return "straight ahead";
		} else if (angle > 22 && angle < 67) {
			return "soft right";
		} else if (angle > 67 && angle < 112) {
			return "right";
		} else if (angle > 112 && angle < 157) {
			return "hard right";
		} else if (angle > 157 && angle < 202) {
			return "u turn";
		} else if (angle > 202 && angle < 247) {
			return "hard left";
		} else if (angle > 247 && angle < 292) {
			return "left";
		} else if (angle > 292 && angle < 337) {
			return "soft left";
		}
		return "";
	}
}
