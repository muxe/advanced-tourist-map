/*
 * Copyright 2010 mapsforge.org
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

import org.mapsforge.android.map.GeoPoint;
import org.mapsforge.android.map.MapActivity;
import org.mapsforge.android.map.MapController;
import org.mapsforge.android.map.MapView;
import org.mapsforge.android.map.MapViewMode;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * A map application which demonstrates how to use the MapView. The map can be centered to
 * current GPS coordinates. A simple file browser for selecting the map file is also included.
 * Some preferences can be adjusted via the EditPreferences activity.
 */
public class AdvancedMapViewer extends MapActivity {
	private static final int DIALOG_GPS_DISABLED = 0;
	private static final int DIALOG_MAP_FILE_INVALID = 1;
	private static final int DIALOG_MAP_FILE_SELECT = 2;
	static final short FILE_CACHE_SIZE_DEFAULT = 100;
	static final short FILE_CACHE_SIZE_MAX = 500;
	static final int MOVE_SPEED_DEFAULT = 10;
	static final int MOVE_SPEED_MAX = 30;
	private Button cancelButton;
	private FileBrowser fileBrowser;
	private GridView fileBrowserView;
	private boolean followGpsEnabled;
	private Button goButton;
	private RelativeLayout mainView;
	private MapView mapView;
	private MapViewMode mapViewMode;
	private Toast toast;
	RelativeLayout coordinatesView;
	ImageView gpsView;
	InputMethodManager inputMethodManager;
	EditText latitudeView;
	LocationListener locationListener;
	LocationManager locationManager;
	EditText longitudeView;
	MapController mapController;
	SharedPreferences preferencesDefault;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set up the layout views
		setContentView(R.layout.main);
		this.mainView = (RelativeLayout) findViewById(R.id.mainView);
		this.fileBrowserView = (GridView) findViewById(R.id.fileBrowserView);
		this.gpsView = (ImageView) findViewById(R.id.gpsView);
		this.coordinatesView = (RelativeLayout) findViewById(R.id.coordinatesView);
		this.latitudeView = (EditText) findViewById(R.id.latitude);
		this.longitudeView = (EditText) findViewById(R.id.longitude);
		this.goButton = (Button) findViewById(R.id.goButton);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);

		// create the file browser
		this.fileBrowser = new FileBrowser(this, "/", this.fileBrowserView);

		// set an empty touch listener to handle all touch events
		this.coordinatesView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// get the location manager and the input method manager
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
				setWindowTitle();
				return true;
			} else if (this.fileBrowserView.getVisibility() == View.VISIBLE) {
				if (this.mapView.getMapViewMode() != MapViewMode.TILE_DOWNLOAD
						&& this.mapView.hasValidMapFile()) {
					// close the file browser and show the map
					this.fileBrowserView.setVisibility(View.GONE);
					this.mainView.setVisibility(View.VISIBLE);
					setWindowTitle();
				} else {
					// quit the application
					finish();
				}
				return true;
			} else {
				// quit the application
				finish();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_info:
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
				setTitle(R.string.menu_position_enter_coordinates);

				this.goButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// disable gps follow mode if it is enabled
						disableFollowGPS(true);
						// set the new map center coordinates
						AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(Double
								.parseDouble(AdvancedMapViewer.this.latitudeView.getText()
										.toString()), Double
								.parseDouble(AdvancedMapViewer.this.longitudeView.getText()
										.toString())));
						// hide the virtual keyboard
						AdvancedMapViewer.this.inputMethodManager.hideSoftInputFromWindow(
								AdvancedMapViewer.this.coordinatesView.getWindowToken(), 0);
						AdvancedMapViewer.this.coordinatesView.setVisibility(View.GONE);
						setWindowTitle();
					}
				});

				this.cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// hide the virtual keyboard
						AdvancedMapViewer.this.inputMethodManager.hideSoftInputFromWindow(
								AdvancedMapViewer.this.coordinatesView.getWindowToken(), 0);
						AdvancedMapViewer.this.coordinatesView.setVisibility(View.GONE);
						setWindowTitle();
					}
				});
				return true;

			case R.id.menu_position_map_center:
				// disable gps follow mode if it is enabled
				disableFollowGPS(true);
				this.mapController.setCenter(this.mapView.getMapFileCenter());
				return true;

			case R.id.menu_mapfile:
				this.fileBrowser.browseToCurrentDirectory();
				this.mainView.setVisibility(View.GONE);
				this.fileBrowserView.setVisibility(View.VISIBLE);
				return true;

			case R.id.menu_preferences:
				startActivity(new Intent(this, EditPreferences.class));
				return true;

			case R.id.menu_exit:
				finish();
				return true;
		}
		showToast("not yet implemented");
		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_info).setEnabled(true);

		if (this.mapView.getMapViewMode() == MapViewMode.TILE_DOWNLOAD
				|| this.fileBrowserView.getVisibility() == View.VISIBLE) {
			menu.findItem(R.id.menu_mapfile).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_mapfile).setEnabled(true);
		}

		if (this.coordinatesView.getVisibility() == View.VISIBLE
				|| this.fileBrowserView.getVisibility() == View.VISIBLE) {
			menu.findItem(R.id.menu_position).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position).setEnabled(true);
		}

		menu.findItem(R.id.menu_preferences).setEnabled(true);

		if (this.locationManager == null || this.followGpsEnabled) {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_gps_follow).setEnabled(true);
		}

		return true;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (this.locationListener != null) {
			return new Boolean(true);
		}
		return null;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		// forward the event to the MapView
		return this.mapView.onTrackballEvent(event);
	}

	private void enableFollowGPS() {
		this.followGpsEnabled = true;
		this.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(location
						.getLatitude(), location.getLongitude()));
				AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
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

	private void setupMapView() {
		// make the MapView clickable and activate the zoomLevel buttons
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

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_GPS_DISABLED:
				builder.setMessage(getString(R.string.gps_disabled)).setPositiveButton(
						getString(R.string.ok), null);
				return builder.create();
			case DIALOG_MAP_FILE_INVALID:
				builder.setMessage(getString(R.string.map_file_invalid)).setTitle(
						getString(R.string.error)).setPositiveButton(getString(R.string.ok),
						null);
				return builder.create();
			case DIALOG_MAP_FILE_SELECT:
				builder.setMessage(getString(R.string.map_file_select)).setPositiveButton(
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
	protected void onResume() {
		super.onResume();

		// Read the default shared preferences
		this.preferencesDefault = PreferenceManager.getDefaultSharedPreferences(this);

		// get the operation mode for the MapView
		this.mapViewMode = Enum.valueOf(MapViewMode.class, this.preferencesDefault.getString(
				"mapViewMode", MapView.getDefaultMapViewMode().name()));

		if (this.mapView == null) {
			// create a new MapView with the correct operation mode
			this.mapView = new MapView(this, this.mapViewMode);
			setupMapView();
			this.mainView.addView(this.mapView, 0, new ViewGroup.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		} else {
			// set the correct operation mode
			this.mapView.setMapViewMode(this.mapViewMode);
		}

		// restore all other preferences
		this.mapView.setMapScale(this.preferencesDefault.getBoolean("showMapScale", false));
		this.mapView.setFpsCounter(this.preferencesDefault.getBoolean("showFpsCounter", false));
		this.mapView.setFileCacheSize(Math.min(this.preferencesDefault.getInt("cacheSize",
				FILE_CACHE_SIZE_DEFAULT), FILE_CACHE_SIZE_MAX));
		this.mapView.setMoveSpeed(Math.min(this.preferencesDefault.getInt("moveSpeed",
				MOVE_SPEED_DEFAULT), MOVE_SPEED_MAX) / 10f);

		// check if the file browser needs to be displayed
		if (this.mapView.getMapViewMode() == MapViewMode.TILE_DOWNLOAD
				|| this.mapView.hasValidMapFile()) {
			this.fileBrowserView.setVisibility(View.GONE);
			this.mainView.setVisibility(View.VISIBLE);
			setWindowTitle();
			if (getLastNonConfigurationInstance() != null) {
				enableFollowGPS();
			}
		} else {
			this.mainView.setVisibility(View.GONE);
			this.fileBrowserView.setVisibility(View.VISIBLE);
			this.fileBrowser.browseToCurrentDirectory();
			showDialog(DIALOG_MAP_FILE_SELECT);
		}
	}

	/**
	 * Disables the "Follow GPS mode" and removes the GPS icon.
	 * 
	 * @param showToastMessage
	 *            if a toast message should be displayed or not.
	 */
	void disableFollowGPS(boolean showToastMessage) {
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
	 * This method is called when a new map file is selected in the file browser.
	 * 
	 * @param newMapFile
	 *            the path to the new map file
	 */
	void onMapFileSelected(String newMapFile) {
		if (!MapView.isValidMapFile(newMapFile)) {
			showDialog(DIALOG_MAP_FILE_INVALID);
			return;
		}
		disableFollowGPS(true);
		this.mapView.setMapFile(newMapFile);
		this.fileBrowserView.setVisibility(View.GONE);
		this.mainView.setVisibility(View.VISIBLE);
		setWindowTitle();
	}

	/**
	 * Sets the current window title.
	 */
	void setWindowTitle() {
		if (this.mapView.getMapViewMode() != MapViewMode.TILE_DOWNLOAD) {
			if (this.mapView.getMapFile() == null) {
				setTitle(null);
			} else {
				setTitle(this.mapView.getMapFile().substring(
						this.mapView.getMapFile().lastIndexOf("/") + 1));
			}
		} else {
			setTitle(this.mapView.getMapTileDownloadServer());
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
}