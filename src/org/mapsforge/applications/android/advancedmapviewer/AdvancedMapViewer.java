/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.CircleOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.OverlayItem;
import org.mapsforge.android.maps.RouteOverlay;
import org.mapsforge.android.routing.blockedHighwayHierarchies.HHRouter;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.server.routing.IEdge;
import org.mapsforge.server.routing.IVertex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A map application which uses the features from the mapsforge library. The map can be centered
 * to the current GPS coordinate. A simple file browser for selecting the map file is also
 * included. Some preferences can be adjusted via the EditPreferences activity.
 */
public class AdvancedMapViewer extends MapActivity {
	private static final int DIALOG_GPS_DISABLED = 0;
	private static final int SELECT_MAP_FILE = 0;

	/**
	 * The default size of the memory card cache.
	 */
	static final short MEMORY_CARD_CACHE_SIZE_DEFAULT = 250;

	/**
	 * The maximum size of the memory card cache.
	 */
	static final short MEMORY_CARD_CACHE_SIZE_MAX = 500;

	/**
	 * The default move speed of the map.
	 */
	static final int MOVE_SPEED_DEFAULT = 10;

	/**
	 * The maximum move speed of the map.
	 */
	static final int MOVE_SPEED_MAX = 30;

	/**
	 * Cache Size for routing in bytes
	 */
	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;

	/**
	 * Binary file for routing
	 */
	static final String ROUTING_BINARY_FILE = "/sdcard/ger_12_k_center_75_true.blockedHH";

	private Button cancelButton;
	private Paint circleOverlayFill;
	private Paint circleOverlayOutline;
	private boolean followGpsEnabled;
	private Button goButton;
	private LocationListener locationListener;
	private LocationManager locationManager;
	private MapView mapView;
	private MapViewMode mapViewMode;
	private PowerManager powerManager;
	private SharedPreferences preferences;
	private Toast toast;
	private WakeLock wakeLock;
	CircleOverlay circleOverlay;
	RelativeLayout coordinatesView;
	ImageView gpsView;
	InputMethodManager inputMethodManager;
	EditText latitudeView;
	EditText longitudeView;
	MapController mapController;
	TextView zoomlevelValue;
	SeekBar zoomlevelView;

	/* routing */
	private Drawable routeStartMarker;
	private Drawable routeEndMarker;

	HHRouter router;
	RouteOverlay routeOverlay;
	IVertex routeStart;
	IVertex routeEnd;
	private ArrayItemizedOverlay routeItemsOverlay;
	private OverlayItem routeStartItem;
	private OverlayItem routeEndItem;

	private boolean captureRouteStart;
	private boolean captureRouteEnd;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// insert code here to handle touch events on the screen
		if (this.captureRouteStart) {
			GeoPoint gp = this.mapView.getProjection().fromPixels((int) ev.getX(),
					(int) ev.getY());
			handleMenuEventRouteSetStart(gp);
			this.captureRouteStart = false;
		} else if (this.captureRouteEnd) {
			GeoPoint gp = this.mapView.getProjection().fromPixels((int) ev.getX(),
					(int) ev.getY());
			handleMenuEventRouteSetEnd(gp);
			this.captureRouteEnd = false;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (this.coordinatesView.getVisibility() == View.VISIBLE) {
				this.coordinatesView.setVisibility(View.GONE);
				return true;
			}
			// quit the application
			finish();
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_info:
				startActivity(new Intent(this, InfoView.class));
				return true;

			case R.id.menu_position:
				return true;

			case R.id.menu_position_gps_follow:
				if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					enableFollowGPS();
				} else {
					showDialog(DIALOG_GPS_DISABLED);
				}
				return true;

			case R.id.menu_position_enter_coordinates:
				this.coordinatesView.setVisibility(View.VISIBLE);
				GeoPoint mapCenter = this.mapView.getMapCenter();
				this.latitudeView.setText(Double.toString(mapCenter.getLatitude()));
				this.longitudeView.setText(Double.toString(mapCenter.getLongitude()));
				this.zoomlevelView.setMax(this.mapView.getMaxZoomLevel());
				this.zoomlevelView.setProgress(this.mapView.getZoomLevel());
				this.zoomlevelView
						.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								AdvancedMapViewer.this.zoomlevelValue.setText(String
										.valueOf(progress));
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// do nothing
							}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// do nothing
							}
						});
				this.zoomlevelValue.setText(String.valueOf(this.zoomlevelView.getProgress()));

				this.goButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// disable GPS follow mode if it is enabled
						disableFollowGPS(true);

						// set the new map center coordinates and zoom level
						AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(Double
								.parseDouble(AdvancedMapViewer.this.latitudeView.getText()
										.toString()), Double
								.parseDouble(AdvancedMapViewer.this.longitudeView.getText()
										.toString())));
						AdvancedMapViewer.this.mapController
								.setZoom(AdvancedMapViewer.this.zoomlevelView.getProgress());

						// hide the virtual keyboard
						AdvancedMapViewer.this.inputMethodManager.hideSoftInputFromWindow(
								AdvancedMapViewer.this.coordinatesView.getWindowToken(), 0);
						AdvancedMapViewer.this.coordinatesView.setVisibility(View.GONE);
					}
				});

				this.cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// hide the virtual keyboard
						AdvancedMapViewer.this.inputMethodManager.hideSoftInputFromWindow(
								AdvancedMapViewer.this.coordinatesView.getWindowToken(), 0);
						AdvancedMapViewer.this.coordinatesView.setVisibility(View.GONE);
					}
				});
				return true;

			case R.id.menu_position_map_center:
				// disable GPS follow mode if it is enabled
				disableFollowGPS(true);
				this.mapController.setCenter(this.mapView.getMapDatabase().getMapCenter());
				return true;

			case R.id.menu_routing:
				return true;

			case R.id.menu_routing_start:
				this.captureRouteStart = true;
				this.captureRouteEnd = false;
				return true;

			case R.id.menu_routing_end:
				this.captureRouteStart = false;
				this.captureRouteEnd = true;
				return true;

			case R.id.menu_routing_clear:
				handleMenuEventRouteClear();
				this.captureRouteEnd = false;
				this.captureRouteStart = false;
				return true;

			case R.id.menu_mapfile:
				startActivityForResult(new Intent(this, FileBrowser.class), SELECT_MAP_FILE);
				return true;

			case R.id.menu_preferences:
				startActivity(new Intent(this, EditPreferences.class));
				return true;
		}
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_info).setEnabled(true);

		if (this.coordinatesView.getVisibility() == View.VISIBLE) {
			menu.findItem(R.id.menu_position).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position).setEnabled(true);
		}

		if (this.locationManager == null || this.followGpsEnabled) {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(true);
		}

		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_position_map_center).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_map_center).setEnabled(true);
		}

		menu.findItem(R.id.menu_preferences).setEnabled(true);

		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_mapfile).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_mapfile).setEnabled(true);
		}

		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// forward the event to the MapView
		return this.mapView.onTrackballEvent(event);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// initialize route markers
		this.routeStartMarker = getResources().getDrawable(android.R.drawable.btn_star);
		this.routeEndMarker = getResources().getDrawable(android.R.drawable.btn_star);

		// initialize route overlay
		Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setStyle(Paint.Style.STROKE);
		fillPaint.setColor(Color.BLUE);
		fillPaint.setAlpha(160);
		fillPaint.setStrokeWidth(6);
		fillPaint.setStrokeCap(Paint.Cap.ROUND);
		fillPaint.setStrokeJoin(Paint.Join.ROUND);
		this.routeOverlay = new RouteOverlay(fillPaint, null);
		this.mapView.getOverlays().add(this.routeOverlay);

		// initialize route items overlay
		this.routeItemsOverlay = new ArrayItemizedOverlay(this.routeStartMarker, this);
		this.mapView.getOverlays().add(this.routeItemsOverlay);

		// initialize route
		try {
			if (this.router == null) {
				this.router = new HHRouter(new File(ROUTING_BINARY_FILE),
						ROUTING_MAIN_MEMORY_CACHE_SIZE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void configureMapView() {
		// configure the MapView and activate the zoomLevel buttons
		this.mapView.setClickable(true);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setFocusable(true);

		// set the localized text fields
		this.mapView
				.setText("unit_symbol_kilometer", getString(R.string.unit_symbol_kilometer));
		this.mapView.setText("unit_symbol_meter", getString(R.string.unit_symbol_meter));

		// get the map controller for this MapView
		this.mapController = this.mapView.getController();
	}

	private void enableFollowGPS() {
		this.circleOverlay = new CircleOverlay(this.circleOverlayFill,
				this.circleOverlayOutline);
		this.mapView.getOverlays().add(this.circleOverlay);

		this.followGpsEnabled = true;
		this.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
				AdvancedMapViewer.this.mapController.setCenter(point);
				AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
				AdvancedMapViewer.this.circleOverlay.setCircleData(point,
						location.getAccuracy());
			}

			@Override
			public void onProviderDisabled(String provider) {
				disableFollowGPS(false);
				showDialog(DIALOG_GPS_DISABLED);
			}

			@Override
			public void onProviderEnabled(String provider) {
				// do nothing
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (status == LocationProvider.AVAILABLE) {
					AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
				} else if (status == LocationProvider.OUT_OF_SERVICE) {
					AdvancedMapViewer.this.gpsView
							.setImageResource(R.drawable.stat_sys_gps_acquiring);
				} else {
					// must be TEMPORARILY_UNAVAILABLE
					AdvancedMapViewer.this.gpsView.setImageResource(R.anim.gps_animation);
					((AnimationDrawable) AdvancedMapViewer.this.gpsView.getDrawable()).start();
				}
			}
		};

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
				this.locationListener);
		this.gpsView.setImageResource(R.drawable.stat_sys_gps_acquiring);
		this.gpsView.setVisibility(View.VISIBLE);
		this.gpsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableFollowGPS(true);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_MAP_FILE) {
			if (resultCode == RESULT_OK) {
				disableFollowGPS(true);
				if (data != null && data.getStringExtra("mapFile") != null) {
					this.mapView.setMapFile(data.getStringExtra("mapFile"));
				}
			} else if (resultCode == RESULT_CANCELED
					&& !this.mapView.getMapViewMode().requiresInternetConnection()
					&& !this.mapView.hasValidMapFile()) {
				finish();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set up the layout views
		setContentView(R.layout.advancedmapviewer);
		this.mapView = (MapView) findViewById(R.id.mapView);
		this.gpsView = (ImageView) findViewById(R.id.gpsView);
		this.coordinatesView = (RelativeLayout) findViewById(R.id.coordinatesView);
		this.latitudeView = (EditText) findViewById(R.id.latitude);
		this.longitudeView = (EditText) findViewById(R.id.longitude);
		this.zoomlevelView = (SeekBar) findViewById(R.id.zoomlevel);
		this.zoomlevelValue = (TextView) findViewById(R.id.zoomlevelValue);
		this.goButton = (Button) findViewById(R.id.goButton);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);

		configureMapView();

		// set an empty touch listener to handle all touch events
		this.coordinatesView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// get the pointers to different system services
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		this.powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.wakeLock = this.powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AMV");

		// set up the paint objects for the location overlay
		this.circleOverlayFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.circleOverlayFill.setStyle(Paint.Style.FILL);
		this.circleOverlayFill.setColor(Color.BLUE);
		this.circleOverlayFill.setAlpha(64);

		this.circleOverlayOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.circleOverlayOutline.setStyle(Paint.Style.STROKE);
		this.circleOverlayOutline.setColor(Color.BLUE);
		this.circleOverlayOutline.setAlpha(128);
		this.circleOverlayOutline.setStrokeWidth(3);

		if (savedInstanceState != null && savedInstanceState.getBoolean("locationListener")) {
			if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				enableFollowGPS();
			} else {
				showDialog(DIALOG_GPS_DISABLED);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_GPS_DISABLED:
				builder.setMessage(getString(R.string.gps_disabled)).setPositiveButton(
						getString(R.string.ok), null);
				return builder.create();
			default:
				showToast("missing dialog implementation: " + id);
				return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// remove the toast messageText if visible
		if (this.toast != null) {
			this.toast.cancel();
			this.toast = null;
		}

		// switch off location updates
		if (this.locationManager != null) {
			if (this.locationListener != null) {
				this.locationManager.removeUpdates(this.locationListener);
				this.locationListener = null;
			}
			this.locationManager = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// release the wake lock if necessary
		if (this.wakeLock.isHeld()) {
			this.wakeLock.release();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Read the default shared preferences
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// check if the full screen mode should be activated
		if (this.preferences.getBoolean("fullscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}

		// check if the wake lock should be activated
		if (this.preferences.getBoolean("wake_lock", false)) {
			if (!this.wakeLock.isHeld()) {
				this.wakeLock.acquire();
			}
		}

		// set the operation mode for the MapView
		if (this.preferences.contains("mapViewMode")) {
			this.mapViewMode = Enum.valueOf(MapViewMode.class, this.preferences.getString(
					"mapViewMode", MapView.getDefaultMapViewMode().name()));
			this.mapView.setMapViewMode(this.mapViewMode);
		}

		// restore all other preferences
		this.mapView.setScaleBar(this.preferences.getBoolean("showScaleBar", false));
		this.mapView.setFpsCounter(this.preferences.getBoolean("showFpsCounter", false));
		this.mapView.setTileFrames(this.preferences.getBoolean("showTileFrames", false));
		this.mapView.setTileCoordinates(this.preferences.getBoolean("showTileCoordinates",
				false));
		this.mapView.setWaterTiles(this.preferences.getBoolean("showWaterTiles", false));
		this.mapView.setMemoryCardCacheSize(Math.min(
				this.preferences.getInt("cacheSize", MEMORY_CARD_CACHE_SIZE_DEFAULT),
				MEMORY_CARD_CACHE_SIZE_MAX));
		this.mapView
				.setMoveSpeed(Math.min(
						this.preferences.getInt("moveSpeed", MOVE_SPEED_DEFAULT),
						MOVE_SPEED_MAX) / 10f);

		// check if the file browser needs to be displayed
		if (!this.mapView.getMapViewMode().requiresInternetConnection()
				&& !this.mapView.hasValidMapFile()) {
			startActivityForResult(new Intent(this, FileBrowser.class), SELECT_MAP_FILE);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("locationListener", this.locationListener != null);
	}

	/**
	 * Disables the "Follow GPS mode" and removes the GPS icon.
	 * 
	 * @param showToastMessage
	 *            if a toast message should be displayed or not.
	 */
	void disableFollowGPS(boolean showToastMessage) {
		if (this.circleOverlay != null) {
			this.mapView.getOverlays().remove(this.circleOverlay);
			this.circleOverlay = null;
		}
		if (this.followGpsEnabled) {
			if (this.locationListener != null) {
				this.locationManager.removeUpdates(this.locationListener);
				this.locationListener = null;
			}
			this.gpsView.setVisibility(View.GONE);
			if (showToastMessage) {
				showToast(getString(R.string.follow_gps_disabled));
			}
			this.followGpsEnabled = false;
		}
	}

	/**
	 * Displays a text message via the toast notification system. If a previous message is still
	 * visible, the previous message is first removed.
	 * 
	 * @param text
	 *            the text message to display
	 */
	void showToast(String text) {
		if (this.toast == null) {
			this.toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		} else {
			this.toast.cancel();
			this.toast.setText(text);
		}
		this.toast.show();
	}

	private void handleMenuEventRouteClear() {
		Log.d("osm", "handleMenuEventRouteClear()");

		// clear vertices
		this.routeStart = null;
		this.routeEnd = null;

		// remove overlay items
		this.routeItemsOverlay.removeOverlay(this.routeStartItem);
		this.routeItemsOverlay.removeOverlay(this.routeEndItem);
		this.routeStartItem = null;
		this.routeEnd = null;

		// remove route overlay
		this.routeOverlay.setRouteData(null);
	}

	private boolean handleMenuEventRouteSetStart(GeoPoint gp) {
		// lookup nearest vertex
		IVertex nearestVertex = null;
		if (this.router != null) {
			nearestVertex = this.router.getNearestVertex(new GeoCoordinate(gp.getLatitude(), gp
					.getLongitude()));
		}
		if (nearestVertex == null) {
			showToast("no vertex found");
			return false;
		}

		// clear route if a route is set currently
		if (this.routeStart != null && this.routeEnd != null) {
			handleMenuEventRouteClear();
		}

		// set vertex
		this.routeStart = nearestVertex;

		// set overlay item
		this.routeItemsOverlay.removeOverlay(this.routeStartItem);
		GeoCoordinate c = nearestVertex.getCoordinate();
		this.routeStartItem = new OverlayItem(new GeoPoint(c.getLatitude(), c.getLongitude()),
				"Start", null);
		this.routeStartItem.setMarker(this.routeStartMarker);
		this.routeItemsOverlay.addOverlay(this.routeStartItem);

		// update route overlay
		if (this.routeEnd != null) {
			recomputeRoute();
		}
		return true;
	}

	private boolean handleMenuEventRouteSetEnd(GeoPoint gp) {
		// lookup nearest vertex
		IVertex nearestVertex = null;
		if (this.router != null) {
			nearestVertex = this.router.getNearestVertex(new GeoCoordinate(gp.getLatitude(), gp
					.getLongitude()));
		}
		if (nearestVertex == null) {
			showToast("no vertex found");
			return false;
		}

		// clear route if a route is set currently
		if (this.routeStart != null && this.routeEnd != null) {
			handleMenuEventRouteClear();
		}

		// set vertex
		this.routeEnd = nearestVertex;

		// set overlay item
		this.routeItemsOverlay.removeOverlay(this.routeEndItem);
		GeoCoordinate c = nearestVertex.getCoordinate();
		this.routeEndItem = new OverlayItem(new GeoPoint(c.getLatitude(), c.getLongitude()),
				"End", null);
		this.routeEndItem.setMarker(this.routeStartMarker);
		this.routeItemsOverlay.addOverlay(this.routeEndItem);

		// update route overlay
		if (this.routeStart != null) {
			recomputeRoute();
		}

		return true;
	}

	private void recomputeRoute() {
		if (this.router == null) {
			Log.d("osm",
					"could not compute a route since the routingGraphBinary was not found!");
			return;
		}
		Runnable routeComputation = new Runnable() {
			@Override
			public void run() {
				IEdge[] route = AdvancedMapViewer.this.router.getShortestPath(
						AdvancedMapViewer.this.routeStart.getId(),
						AdvancedMapViewer.this.routeEnd.getId());
				if (route != null) {
					AdvancedMapViewer.this.routeOverlay.setRouteData(routeToGeoPoints(route));
				}
			}
		};
		Thread workerThread = new Thread(routeComputation);
		workerThread.start();
	}

	static GeoPoint[] routeToGeoPoints(IEdge[] route) {
		GeoPoint[] arr = null;
		if (route != null) {
			ArrayList<GeoPoint> list = new ArrayList<GeoPoint>();
			for (int i = 0; i < route.length; i++) {
				GeoCoordinate[] coords = route[i].getAllWaypoints();
				for (int j = 0; j < coords.length; j++) {
					GeoPoint gp = new GeoPoint(coords[j].getLatitude(),
							coords[j].getLongitude());
					list.add(gp);
				}
			}
			arr = new GeoPoint[list.size()];
			list.toArray(arr);
		}
		return arr;
	}
}