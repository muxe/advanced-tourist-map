package org.mapsforge.applications.android.advancedmapviewer.routing;

import java.util.ArrayList;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;

import android.util.Log;

/**
 * Holds all information about a calculated Route
 */
public class Route {
	static final String TAG = RouteCalculator.class.getSimpleName();

	private Edge[] edges;
	private Vertex startVertex;
	private Vertex destVertex;

	/** contains all points of the route to draw the route */
	private GeoPoint[] geoPoints;

	/** The Overlay which displays the route */
	private OverlayWay overlayWay;

	/** contains all DecisionPoints along the route */
	private DecisionPoint[] decisionPoints;

	/** all DecisionPoints as OverlayItems to display them in an ItemizedOverlay */
	private OverlayItem[] overlayItems;

	/** to hold the state of the last watched DecisionPoint, needed to jump from point */
	public DecisionPoint currentDecisionPoint;

	private int length;

	/**
	 * Constructs a route out of an Edge array returned by the Router class
	 * 
	 * @param edges
	 *            the Edge Array defining the Route
	 */
	public Route(Edge[] edges) {
		this.edges = edges;
		this.geoPoints = routeToGeoPoints(edges);
		this.overlayWay = new OverlayWay(new GeoPoint[][] { this.getGeoPoints() });
		this.decisionPoints = this.calculateDecisionPoints(edges);
		this.overlayItems = this.decisionPointsToOverlayItems(this.decisionPoints);
		this.currentDecisionPoint = this.decisionPoints[0];
	}

	/**
	 * Converts an Edge array to an Array of GeoPoints needed to display a Route on the map
	 * 
	 * @param route
	 *            edge array like the output of the Router class
	 * @return array of GeoPoints
	 */
	static GeoPoint[] routeToGeoPoints(Edge[] route) {
		GeoPoint[] arr = null;
		if (route != null) {
			ArrayList<GeoPoint> list = new ArrayList<GeoPoint>();
			if (route.length > 0) {
				GeoCoordinate src = route[0].getSource().getCoordinate();
				list.add(new GeoPoint(src.getLatitude(), src.getLongitude()));
				for (int i = 0; i < route.length; i++) {
					GeoCoordinate[] coords = route[i].getAllWaypoints();
					for (int j = 1; j < coords.length; j++) {
						GeoPoint gp = new GeoPoint(coords[j].getLatitude(),
								coords[j].getLongitude());
						list.add(gp);
					}
				}
			}
			arr = new GeoPoint[list.size()];
			list.toArray(arr);
		}
		return arr;
	}

	public GeoPoint[] getGeoPoints() {
		return this.geoPoints;
	}

	public OverlayWay getOverlayWay() {
		return this.overlayWay;
	}

	public DecisionPoint[] getDecisionPoints() {
		return this.decisionPoints;
	}

	public OverlayItem[] getOverlayItems() {
		return this.overlayItems;
	}

	public int getLength() {
		return this.length;
	}

	/**
	 * Converts an array of Edges to an array of DecisionPoints. Duplicate street names (in a
	 * row) get filtered
	 * 
	 * @param edges
	 *            ArrayList of DecisionPoints
	 * @return Array of DecisionPoints
	 */
	private DecisionPoint[] calculateDecisionPoints(Edge[] edges) {
		ArrayList<DecisionPoint> decisionList = new ArrayList<DecisionPoint>();

		String lastStreet = "";
		int distance = 0;
		int part_distance = 0;
		DecisionPoint dp = null;
		// DecisionPoint lastDp = null;
		for (Edge edge : edges) {
			// TODO: externalize string
			// TODO: setting of route length stuff (siehe log output)

			String streetName = edge.getName() == null ? "unknown" : edge.getName();
			Log.d("RouteCalculator", streetName + " - " + edgeLength(edge));
			// set length of LAST decision point
			if (!streetName.equals(lastStreet)) {
				if (dp != null) {
					dp.setDistance(part_distance);
					distance += part_distance;
				}
				lastStreet = streetName;
				// lastDp = dp;
				dp = new DecisionPoint(streetName, edge.getSource());
				decisionList.add(dp);
				part_distance = edgeLength(edge);
				// Log.d(TAG, streetName);
			} else {
				part_distance += edgeLength(edge);
			}

		}
		if (dp != null) {
			dp.setDistance(part_distance);
			distance += part_distance;
		}
		Log.d("RouteCalculator", "dist to set: " + distance);
		this.length = distance;

		DecisionPoint[] arr;
		arr = new DecisionPoint[decisionList.size()];
		decisionList.toArray(arr);
		return arr;
	}

	private int edgeLength(Edge edge) {
		int distance = 0;
		GeoCoordinate lastGC = null;
		for (GeoCoordinate gc : edge.getAllWaypoints()) {
			if (lastGC != null) {
				distance += gc.sphericalDistance(lastGC);
			}
			lastGC = gc;
		}
		return distance;

	}

	/**
	 * Converts an array of DecisionPoints to an array of OverlayItems which is needed to
	 * display them in an ItemizedOverlay
	 * 
	 * @param decisionPoints1
	 *            array of DecisionPoints
	 * @return array of OverlayItems
	 */
	private OverlayItem[] decisionPointsToOverlayItems(DecisionPoint[] decisionPoints1) {
		OverlayItem[] arr = new OverlayItem[decisionPoints1.length];
		for (int i = 0; i < decisionPoints1.length; i++) {
			arr[i] = new OverlayItem(decisionPoints1[i].getGeoPoint(),
					decisionPoints1[i].getName(), null);
		}
		return arr;
	}

	/**
	 * Gets the following DecisionPoint of the currently active DecisionPoint in the route
	 * 
	 * @return following DecisionPoint or the current DecisionPoint if current has no following
	 *         (is the last in the route)
	 */
	public DecisionPoint getNextDP() {
		for (int i = 0; i < this.decisionPoints.length; i++) {
			if (this.decisionPoints[i] == this.currentDecisionPoint) {
				try {
					this.currentDecisionPoint = this.decisionPoints[i + 1];
				} catch (ArrayIndexOutOfBoundsException e) {
					this.currentDecisionPoint = this.decisionPoints[i];
				}
				break;
			}
		}
		return this.currentDecisionPoint;
	}

	/**
	 * Gets the parent DecisionPoint to the currently active DecisionPoint in the route
	 * 
	 * @return parent DecisionPoint or the current DecisionPoint if current has no parent (is
	 *         the first in the route)
	 */
	public DecisionPoint getPreviousDP() {
		for (int i = 0; i < this.decisionPoints.length; i++) {
			if (this.decisionPoints[i] == this.currentDecisionPoint) {
				try {
					this.currentDecisionPoint = this.decisionPoints[i - 1];
				} catch (ArrayIndexOutOfBoundsException e) {
					this.currentDecisionPoint = this.decisionPoints[i];
				}
				break;
			}
		}
		return this.currentDecisionPoint;
	}

}
