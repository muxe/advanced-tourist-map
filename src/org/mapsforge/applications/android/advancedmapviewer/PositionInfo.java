package org.mapsforge.applications.android.advancedmapviewer;

import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;

import android.content.Intent;
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
	private Button showOnMapButton;
	private Button calculateRouteButton;

	double latitude;
	double longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_position_info);
		this.positionInfoLatitude = (TextView) findViewById(R.id.position_info_latitude);
		this.positionInfoLongitude = (TextView) findViewById(R.id.position_info_longitude);
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

		// TODO: display in list view or whatever
		Edge[] edges = this.advancedMapViewer.getRouter().getNearestEdges(
				new GeoCoordinate(this.latitude, this.longitude));
		for (Edge e : edges) {
			Log.d(TAG, e.getName());
		}
	}
}
