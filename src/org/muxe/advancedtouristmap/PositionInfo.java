package org.muxe.advancedtouristmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.mapsforge.applications.android.advancedmapviewer.R;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Vertex;
import org.mapsforge.poi.PointOfInterest;
import org.muxe.advancedtouristmap.poi.PoiBrowserActivity;
import org.muxe.advancedtouristmap.routing.RouteCalculator;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to display infos about a geo-position, like coordinates, nearby streets/junctions
 * and nearby POIs
 */
public class PositionInfo extends BaseActivity {

	public static final String LONGITUDE_EXTRA = "LONGITUDE";
	public static final String LATITUDE_EXTRA = "LATITUDE";
	private static final String IMAGEKEY = "image";
	private static final String NAMEKEY = "name";
	private static final String INFOKEY = "info";

	private static final String TAG = PositionInfo.class.getSimpleName();
	private static final int MAX_POIS = 20;

	private TextView positionInfoLatitude;
	private TextView positionInfoLongitude;
	private ImageButton showOnMapButton;
	private ImageButton calculateRouteButton;
	private ImageButton findPoiButton;
	private ImageButton savePositionButton;
	TextView nearestJunktionText;
	ListView poiListView;
	private TextView nearestJunktionHeadline;

	ArrayList<PointOfInterest> currentPois;

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

	private class SetNearestPoisAsync extends
			AsyncTask<Void, Void, List<HashMap<String, Object>>> {

		public SetNearestPoisAsync() {
		}

		@Override
		protected List<HashMap<String, Object>> doInBackground(Void... arg0) {
			PositionInfo.this.currentPois = new ArrayList<PointOfInterest>();
			List<HashMap<String, Object>> fillPois = new ArrayList<HashMap<String, Object>>();
			Iterator<PointOfInterest> iterator = PositionInfo.this.advancedMapViewer
					.getPerstManager().neighborIterator(
							new GeoCoordinate(PositionInfo.this.latitude,
									PositionInfo.this.longitude), "Root");
			for (int i = 0; i < MAX_POIS && iterator.hasNext(); i++) {
				PointOfInterest poi = iterator.next();
				HashMap<String, Object> map = new HashMap<String, Object>();
				int distance = (int) GeoCoordinate.sphericalDistance(
						PositionInfo.this.longitude, PositionInfo.this.latitude,
						poi.getLongitude(), poi.getLatitude());
				String description;
				if (poi.getName() != null) {
					description = poi.getName() + " (" + poi.getCategory().getTitle() + ")";
				} else {
					description = poi.getCategory().getTitle();
				}
				map.put(IMAGEKEY, R.drawable.ic_menu_myplaces);
				map.put(NAMEKEY, description);
				map.put(INFOKEY, distance + " m");
				fillPois.add(map);
				PositionInfo.this.currentPois.add(poi);
			}
			return fillPois;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, Object>> result) {
			String[] from = new String[] { IMAGEKEY, NAMEKEY, INFOKEY };
			int[] to = new int[] { R.id.poi_row_image, R.id.poi_row_name, R.id.poi_row_distance };
			PositionInfo.this.poiListView.setAdapter(new SimpleAdapter(PositionInfo.this,
					result, R.layout.poi_row, from, to));
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.advancedMapViewer.setViewWithHelp(this, R.layout.activity_position_info);
		// setContentView(R.layout.activity_position_info);
		this.positionInfoLatitude = (TextView) findViewById(R.id.position_info_latitude);
		this.positionInfoLongitude = (TextView) findViewById(R.id.position_info_longitude);
		this.nearestJunktionText = (TextView) findViewById(R.id.position_info_text_nearest_junktion);
		this.nearestJunktionHeadline = (TextView) findViewById(R.id.position_info_nearest_junktion);
		this.calculateRouteButton = (ImageButton) findViewById(R.id.position_info_button_route);
		this.findPoiButton = (ImageButton) findViewById(R.id.position_info_button_find_pois);
		this.showOnMapButton = (ImageButton) findViewById(R.id.position_info_button_show_on_map);
		this.savePositionButton = (ImageButton) findViewById(R.id.position_info_save_position);
		this.poiListView = (ListView) findViewById(R.id.position_info_poi_list_view);
		this.poiListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				PointOfInterest poi = PositionInfo.this.currentPois.get(position);
				PositionInfo.this.advancedMapViewer.getCurrentPois().clear();
				PositionInfo.this.advancedMapViewer.getCurrentPois().add(poi);
				startActivity(new Intent(PositionInfo.this, AdvancedTouristMap.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
			}
		});

		Intent intent = getIntent();
		this.latitude = intent.getDoubleExtra(LATITUDE_EXTRA, 0.0);
		this.longitude = intent.getDoubleExtra(LONGITUDE_EXTRA, 0.0);

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

		this.findPoiButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(PositionInfo.this, PoiBrowserActivity.class).putExtra(
						"lat", PositionInfo.this.latitude).putExtra("lon",
						PositionInfo.this.longitude));
			}
		});

		this.showOnMapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"
						+ PositionInfo.this.latitude + "," + PositionInfo.this.longitude)));
				// startActivity(new Intent(Intent.ACTION_VIEW,
				// Uri.parse("google.navigation:ll="
				// + PositionInfo.this.latitude + "," + PositionInfo.this.longitude)));
				Toast.makeText(PositionInfo.this, "Not implemented yet", Toast.LENGTH_SHORT)
						.show();
			}
		});

		this.savePositionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(PositionInfo.this, "Not implemented yet", Toast.LENGTH_SHORT)
						.show();
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

		if (!this.advancedMapViewer.getCurrentMapBundle().isPoiable()) {
			this.findPoiButton.setVisibility(View.GONE);
		} else {
			new SetNearestPoisAsync().execute();
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
