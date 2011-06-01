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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PositionInfo extends BaseActivity {
	private static final String TAG = PositionInfo.class.getSimpleName();
	private TextView positionInfoLatitude;
	private TextView positionInfoLongitude;
	// private Button showOnMapButton;
	private Button calculateRouteButton;
	TextView nearestJunktionText;

	double latitude;
	double longitude;

	private class SetNearestJunctionInfo extends AsyncTask<Void, Void, Edge[]> {

		public SetNearestJunctionInfo() {
		}

		@Override
		protected Edge[] doInBackground(Void... arg0) {
			Vertex nearestVertex = PositionInfo.this.advancedMapViewer.getRouter()
					.getNearestVertex(
							new GeoCoordinate(PositionInfo.this.latitude,
									PositionInfo.this.longitude));
			double distance = GeoCoordinate.sphericalDistance(PositionInfo.this.longitude,
					PositionInfo.this.latitude, nearestVertex.getCoordinate().getLongitude(),
					nearestVertex.getCoordinate().getLatitude());
			Edge[] edges = nearestVertex.getOutboundEdges();
			return edges;
		}

		@Override
		protected void onPostExecute(Edge[] edges) {
			PositionInfo.this.nearestJunktionText.setText(edgesToStringInfo(edges));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position_info);
		this.positionInfoLatitude = (TextView) findViewById(R.id.position_info_latitude);
		this.positionInfoLongitude = (TextView) findViewById(R.id.position_info_longitude);
		this.nearestJunktionText = (TextView) findViewById(R.id.position_info_text_nearest_junktion);
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

		new SetNearestJunctionInfo().execute();
	}

	String edgesToStringInfo(Edge[] edges) {
		// TODO: filter null (52.482675, 13.30337)
		List<String> names = new LinkedList<String>();
		for (Edge e : edges) {
			names.add(e.getName());
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
