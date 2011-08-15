package org.mapsforge.applications.android.advancedmapviewer.routing;

import org.mapsforge.applications.android.advancedmapviewer.AdvancedMapViewer;
import org.mapsforge.applications.android.advancedmapviewer.BaseActivity;
import org.mapsforge.applications.android.advancedmapviewer.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Activity to display all DecisionPoints (Streets) of a Route in a list, which clicking of
 * street names
 */
public class RouteList extends BaseActivity {

	private ListView routingList;
	private Button viewOnMapButton;
	private ArrayAdapter<DecisionPoint> routingAdapter;
	private TextView routeLengthView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: check if route is null
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_list);

		this.routingList = (ListView) findViewById(R.id.routing_list);
		this.viewOnMapButton = (Button) findViewById(R.id.view_on_map_button);
		this.routeLengthView = (TextView) findViewById(R.id.routing_list_length);

		Route route = this.advancedMapViewer.currentRoute;

		if (route != null) {
			DecisionPoint[] decisionPoints = route.getDecisionPoints();
			this.routeLengthView.setText(route.getLength() + "m");

			this.routingAdapter = new ArrayAdapter<DecisionPoint>(this,
					android.R.layout.simple_list_item_1, decisionPoints);
			this.routingList.setAdapter(this.routingAdapter);

			this.routingList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					DecisionPoint dp = (DecisionPoint) parent.getItemAtPosition(position);
					Log.d("RouteCalculator", dp.getName());
					// TODO: check if route exists (which should, but you never know, right?)
					RouteList.this.advancedMapViewer.currentRoute.currentDecisionPoint = dp;
					startActivity(new Intent(RouteList.this, AdvancedMapViewer.class).setFlags(
							Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("CENTER_DP", true));
				}
			});
		}

		this.viewOnMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// currently clears all routing stuff from the activity stack and displays the
				// main AMV activity
				startActivity(new Intent(RouteList.this, AdvancedMapViewer.class).putExtra(
						"ROUTE_OVERVIEW", true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
