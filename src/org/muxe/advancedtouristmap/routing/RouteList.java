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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.muxe.advancedtouristmap.R;
import org.muxe.advancedtouristmap.AdvancedTouristMap;
import org.muxe.advancedtouristmap.BaseActivity;
import org.muxe.advancedtouristmap.Utility;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Activity to display all DecisionPoints (Streets) of a Route in a list, which clicking of
 * street names
 */
public class RouteList extends BaseActivity {

	private static final String NAMEKEY = "name";
	private static final String DESCKEY = "description";
	private static final String IMAGEKEY = "image";

	private ListView routingList;
	private Button viewOnMapButton;
	private SimpleAdapter routingAdapter;
	private TextView routeLengthView;
	DecisionPoint[] decisionPoints;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: check if route is null
		super.onCreate(savedInstanceState);
		this.advancedMapViewer.setViewWithHelp(this, R.layout.activity_route_list);

		this.routingList = (ListView) findViewById(R.id.routing_list);
		this.viewOnMapButton = (Button) findViewById(R.id.view_on_map_button);
		this.routeLengthView = (TextView) findViewById(R.id.routing_list_length);

		Route route = this.advancedMapViewer.currentRoute;

		if (route != null) {
			this.decisionPoints = route.getDecisionPoints();
			this.routeLengthView.setText(Utility.meterToReadableDistance(route.getLength()));

			List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
			for (DecisionPoint dp : this.decisionPoints) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put(NAMEKEY, dp.getName());
				map.put(DESCKEY, Utility.meterToReadableDistance(dp.getDistance()));
				double angle = dp.getAngleFromPrevious();
				if (angle > 337 || angle < 22) {
					map.put(IMAGEKEY, R.drawable.routing_forward);
				} else if (angle > 22 && angle < 67) {
					// soft right
					map.put(IMAGEKEY, R.drawable.routing_turn_right);
				} else if (angle > 67 && angle < 112) {
					// right
					map.put(IMAGEKEY, R.drawable.routing_turn_right);
				} else if (angle > 112 && angle < 157) {
					// hard right
					map.put(IMAGEKEY, R.drawable.routing_turn_right);
				} else if (angle > 157 && angle < 202) {
					// u turn
					map.put(IMAGEKEY, R.drawable.routing_around);
				} else if (angle > 202 && angle < 247) {
					// hard left
					map.put(IMAGEKEY, R.drawable.routing_turn_left);
				} else if (angle > 247 && angle < 292) {
					// left";
					map.put(IMAGEKEY, R.drawable.routing_turn_left);
				} else if (angle > 292 && angle < 337) {
					// soft left
					map.put(IMAGEKEY, R.drawable.routing_turn_left);
				} else {
					map.put(IMAGEKEY, R.drawable.routing_forward);
				}

				fillMaps.add(map);
			}

			String[] from = new String[] { NAMEKEY, DESCKEY, IMAGEKEY };
			int[] to = new int[] { R.id.decision_point_row_name,
					R.id.decision_point_row_description, R.id.decision_point_row_image };

			this.routingAdapter = new SimpleAdapter(this, fillMaps,
					R.layout.decision_point_row, from, to);

			// this.routingAdapter = new ArrayAdapter<DecisionPoint>(this,
			// android.R.layout.simple_list_item_1, decisionPoints);
			this.routingList.setAdapter(this.routingAdapter);

			this.routingList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ProgressDialog.show(RouteList.this, "",
							getString(R.string.loading_message), true);
					DecisionPoint dp = RouteList.this.decisionPoints[position];
					// TODO: check if route exists (which should, but you never know, right?)
					RouteList.this.advancedMapViewer.currentRoute.currentDecisionPoint = dp;
					startActivity(new Intent(RouteList.this, AdvancedTouristMap.class).setFlags(
							Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("CENTER_DP", true));
				}
			});
		}

		this.viewOnMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// currently clears all routing stuff from the activity stack and displays the
				// main AMV activity
				ProgressDialog.show(RouteList.this, "", getString(R.string.loading_message),
						true);
				startActivity(new Intent(RouteList.this, AdvancedTouristMap.class).putExtra(
						"ROUTE_OVERVIEW", true).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
