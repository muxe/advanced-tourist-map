package org.mapsforge.applications.android.advancedmapviewer.routing;

import java.util.ArrayList;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.OverlayWay;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;

import android.util.Log;

public class Route {
	static final String TAG = RouteCalculator.class.getSimpleName();

	private Edge[] edges;
	private Vertex startVertex;
	private Vertex destVertex;
	private GeoPoint[] geoPoints;
	private OverlayWay overlayWay;
	private DecisionPoint[] decisionPoints;
	private OverlayItem[] overlayItems;
	public DecisionPoint currentDecisionPoint;

	public Route(Edge[] edges) {
		this.edges = edges;
		this.setGeoPoints(routeToGeoPoints(edges));
		this.overlayWay = new OverlayWay(new GeoPoint[][] { this.getGeoPoints() });
		this.decisionPoints = Route.calculateDecisionPoints(edges);
		this.overlayItems = this.decisionPointsToOverlayItems(this.decisionPoints);
		this.currentDecisionPoint = this.decisionPoints[0];
	}

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

	public void setGeoPoints(GeoPoint[] geoPoints) {
		this.geoPoints = geoPoints;
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

	public static DecisionPoint[] calculateDecisionPoints(Edge[] edges) {
		ArrayList<DecisionPoint> decisionList = new ArrayList<DecisionPoint>();

		String lastStreet = "";
		for (Edge edge : edges) {
			// TODO: externalize string
			String streetName = edge.getName() == null ? "unknown" : edge.getName();
			if (!streetName.equals(lastStreet)) {
				lastStreet = streetName;
				decisionList.add(new DecisionPoint(streetName, edge.getSource()));
				Log.d(TAG, streetName);
			}
		}

		DecisionPoint[] arr;
		arr = new DecisionPoint[decisionList.size()];
		decisionList.toArray(arr);
		return arr;
	}

	private OverlayItem[] decisionPointsToOverlayItems(DecisionPoint[] decisionPoints1) {
		OverlayItem[] arr = new OverlayItem[decisionPoints1.length];
		for (int i = 0; i < decisionPoints1.length; i++) {
			arr[i] = new OverlayItem(decisionPoints1[i].getGeoPoint(),
					decisionPoints1[i].getName(), null);
		}
		return arr;
	}

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
