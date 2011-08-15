package org.mapsforge.applications.android.advancedmapviewer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mapsforge.applications.android.advancedmapviewer.routing.RouteCalculator;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Activity to display infos about a geo-position, like coordinates, nearby streets/junctions
 * and nearby POIs
 */
public class PositionInfo extends BaseActivity {
	private static final String TAG = PositionInfo.class.getSimpleName();
	private TextView positionInfoLatitude;
	private TextView positionInfoLongitude;
	// private Button showOnMapButton;
	private Button calculateRouteButton;
	TextView nearestJunktionText;
	private TextView nearestJunktionHeadline;

	double latitude;
	double longitude;

	/**
	 * Class to asynchronously set the info about the nearest junction, since this may take some
	 * time.
	 */
	private class SetNearestJunctionInfo extends AsyncTask<Void, Void, Edge[]> {

		public SetNearestJunctionInfo() {
			super();
		}

		@Override
		protected Edge[] doInBackground(Void... arg0) {
			if (PositionInfo.this.advancedMapViewer.getRouter() == null) {
				return null;
			}
			Vertex nearestVertex = PositionInfo.this.advancedMapViewer.getRouter()
					.getNearestVertex(
							new GeoCoordinate(PositionInfo.this.latitude,
									PositionInfo.this.longitude));
			// double distance = GeoCoordinate.sphericalDistance(PositionInfo.this.longitude,
			// PositionInfo.this.latitude, nearestVertex.getCoordinate().getLongitude(),
			// nearestVertex.getCoordinate().getLatitude());
			if (nearestVertex == null) {
				// TODO: quickfix
				return new Edge[0];
			}
			Edge[] edges = nearestVertex.getOutboundEdges();
			return edges;
		}

		@Override
		protected void onPostExecute(Edge[] edges) {
			String stringInfo = edgesToStringInfo(edges);
			if (stringInfo.equals("")) {
				stringInfo = getString(R.string.positioninfo_unknown_road);
			}
			PositionInfo.this.nearestJunktionText.setText(stringInfo);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position_info);
		this.positionInfoLatitude = (TextView) findViewById(R.id.position_info_latitude);
		this.positionInfoLongitude = (TextView) findViewById(R.id.position_info_longitude);
		this.nearestJunktionText = (TextView) findViewById(R.id.position_info_text_nearest_junktion);
		this.nearestJunktionHeadline = (TextView) findViewById(R.id.position_info_nearest_junktion);
		this.calculateRouteButton = (Button) findViewById(R.id.position_info_button_route);

		Intent intent = getIntent();
		this.latitude = intent.getDoubleExtra("LATITUDE", 0.0);
		this.longitude = intent.getDoubleExtra("LONGITUDE", 0.0);

		this.positionInfoLatitude.setText(String.valueOf(this.latitude));
		this.positionInfoLongitude.setText(String.valueOf(this.longitude));

		this.calculateRouteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PositionInfo.this, RouteCalculator.class).putExtra(
						"lat", PositionInfo.this.latitude).putExtra("lon",
						PositionInfo.this.longitude));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!this.advancedMapViewer.getCurrentMapBundle().isRoutable()) {
			this.calculateRouteButton.setVisibility(View.GONE);
			this.nearestJunktionHeadline.setVisibility(View.GONE);
			this.nearestJunktionText.setVisibility(View.GONE);
		} else {
			new SetNearestJunctionInfo().execute();
		}
	}

	/**
	 * Converts an array of Edges to a human readable string representation. Most likely used to
	 * display the name of a junction. Filters duplicates and Edges without name.
	 * 
	 * @param edges
	 *            array of Edges to convert
	 * @return a human readable string
	 */
	public static String edgesToStringInfo(Edge[] edges) {
		if (edges == null || edges.length <= 0) {
			// alternative: pass context and call getString on it to get a not-found-string
			return "";
		}
		List<String> names = new LinkedList<String>();
		for (Edge e : edges) {
			if (e.getName() != null) {
				names.add(e.getName());
			}
		}

		// filter duplicates
		Set<String> set = new HashSet<String>(names);
		String[] nameArray = new String[set.size()];
		set.toArray(nameArray);

		// build string
		String result = "";
		for (int i = 0; i < nameArray.length; i++) {
			if (i != 0) {
				result += " / ";
			}
			result += nameArray[i];
		}
		return result;
	}
}
