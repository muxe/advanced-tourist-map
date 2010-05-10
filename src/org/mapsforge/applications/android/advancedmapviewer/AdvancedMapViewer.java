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
import org.mapsforge.applications.android.advancedmapviewer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * A map application which demonstrates how to use the MapView. The map can be centered to
 * current GPS coordinates. A file viewer for selecting the map file is also included. Some
 * preferences can be adjusted via the standard Android preferences activity.
 */
public class AdvancedMapViewer extends MapActivity {
	private static final int DIALOG_GPS_DISABLED = 0;
	private static final int DIALOG_MAP_FILE_INVALID = 1;
	private static final int DIALOG_MAP_FILE_SELECT = 2;
	private static final String THREAD_NAME = "AdvancedMapViewer";
	static final byte IMAGE_CACHE_SIZE_DEFAULT = 25;
	static final byte IMAGE_CACHE_SIZE_MAX = 50;
	static void d(String str) {
		Log.d("osm", Thread.currentThread().getName() + ": " + str);
	}
	private Button cancelButton;
	private FileBrowser fileBrowser;
	private GridView fileBrowserView;
	private Button goButton;
	private RelativeLayout mainView;
	private MapView mapView;
	private Toast toast;
	RelativeLayout coordinatesView;
	AnimationDrawable gpsAnimation;
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
		Thread.currentThread().setName(THREAD_NAME);

		// set up the layout views
		setContentView(R.layout.main);
		this.mainView = (RelativeLayout) findViewById(R.id.mainView);
		this.mapView = (MapView) findViewById(R.id.mapView);
		this.fileBrowserView = (GridView) findViewById(R.id.fileBrowserView);
		this.gpsView = (ImageView) findViewById(R.id.gpsView);
		this.coordinatesView = (RelativeLayout) findViewById(R.id.coordinatesView);
		this.latitudeView = (EditText) findViewById(R.id.latitude);
		this.longitudeView = (EditText) findViewById(R.id.longitude);
		this.goButton = (Button) findViewById(R.id.goButton);
		this.cancelButton = (Button) findViewById(R.id.cancelButton);

		// get the map controller for this MapView
		this.mapController = this.mapView.getController();

		// create the file browser
		this.fileBrowser = new FileBrowser(this, "/", this.fileBrowserView);

		// get the animation drawable for the gps symbol
		this.gpsAnimation = (AnimationDrawable) this.gpsView.getDrawable();

		// set an empty touch listener to handle all touch events
		this.coordinatesView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// make the MapView clickable and activate the zoomLevel buttons
		this.mapView.setClickable(true);
		this.mapView.setBuiltInZoomControls(true);

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
				setMapFileTitle();
				return true;
			} else if (this.fileBrowserView.getVisibility() == View.VISIBLE) {
				if (this.mapView.hasValidMapFile()) {
					// close the file browser and show the map
					this.fileBrowserView.setVisibility(View.GONE);
					this.mainView.setVisibility(View.VISIBLE);
					setMapFileTitle();
				} else {
					// quit the application
					finish();
				}
				return true;
			} else if (this.mainView.getVisibility() == View.VISIBLE) {
				// quit the application
				finish();
				return true;
			}
		}
		// forward the event to the MapView
		return this.mapView.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// forward the event to the MapView
		return this.mapView.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_position:
				return true;

			case R.id.menu_position_gps_follow:
				if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					this.locationListener = new LocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(
									location.getLatitude(), location.getLongitude()));
						}

						@Override
						public void onProviderDisabled(String provider) {
							disableFollowGPS(this);
							showDialog(DIALOG_GPS_DISABLED);
						}

						@Override
						public void onProviderEnabled(String provider) {
							// do nothing
						}

						@Override
						public void onStatusChanged(String provider, int status, Bundle extras) {
							// do nothing
						}
					};

					this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
							2000, 0, this.locationListener);
					this.gpsAnimation.start();
					this.gpsView.setVisibility(View.VISIBLE);
					this.gpsView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							disableFollowGPS(AdvancedMapViewer.this.locationListener);
						}
					});
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
						disableFollowGPS(AdvancedMapViewer.this.locationListener);
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
						setMapFileTitle();
					}
				});

				this.cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// hide the virtual keyboard
						AdvancedMapViewer.this.inputMethodManager.hideSoftInputFromWindow(
								AdvancedMapViewer.this.coordinatesView.getWindowToken(), 0);
						AdvancedMapViewer.this.coordinatesView.setVisibility(View.GONE);
						setMapFileTitle();
					}
				});
				return true;

			case R.id.menu_position_map_center:
				// disable gps follow mode if it is enabled
				disableFollowGPS(AdvancedMapViewer.this.locationListener);
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
		if (this.coordinatesView.getVisibility() == View.VISIBLE
				|| this.fileBrowserView.getVisibility() == View.VISIBLE) {
			menu.findItem(R.id.menu_info).setEnabled(false);
			menu.findItem(R.id.menu_mapfile).setEnabled(false);
			menu.findItem(R.id.menu_position).setEnabled(false);
			menu.findItem(R.id.menu_preferences).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_info).setEnabled(true);
			menu.findItem(R.id.menu_mapfile).setEnabled(true);
			menu.findItem(R.id.menu_position).setEnabled(true);
			menu.findItem(R.id.menu_preferences).setEnabled(true);

			if (this.locationManager == null || this.gpsView.getVisibility() == View.VISIBLE) {
				menu.findItem(R.id.menu_position_gps_follow).setEnabled(false);
			} else {
				menu.findItem(R.id.menu_position_gps_follow).setEnabled(true);
			}
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_MAP_FILE_INVALID:
				builder.setMessage(getString(R.string.map_file_invalid)).setTitle(
						getString(R.string.error)).setPositiveButton(getString(R.string.ok),
						null);
				return builder.create();
			case DIALOG_MAP_FILE_SELECT:
				builder.setMessage(getString(R.string.map_file_select)).setPositiveButton(
						getString(R.string.ok), null);
				return builder.create();
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
		// remove the toast message if visible
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
		this.mapView.setMapScale(this.preferencesDefault.getBoolean("showMapScale", false));
		this.mapView.setCacheSize(this.preferencesDefault.getInt("cacheSize",
				IMAGE_CACHE_SIZE_DEFAULT));

		if (this.mapView.hasValidMapFile()) {
			setMapFileTitle();
		} else {
			this.mainView.setVisibility(View.GONE);
			this.fileBrowserView.setVisibility(View.VISIBLE);
			this.fileBrowser.browseToCurrentDirectory();
			showDialog(DIALOG_MAP_FILE_SELECT);
		}
	}

	void disableFollowGPS(LocationListener listener) {
		if (listener != null) {
			this.locationManager.removeUpdates(listener);
		}
		this.gpsView.setVisibility(View.GONE);
		this.gpsAnimation.stop();
	}

	/**
	 * This method is called when a new map file is selected in the file browser.
	 * 
	 * @param newMapFile
	 *            the path to the new map file
	 */
	void onMapFileSelected(String newMapFile) {
		if (!this.mapView.isValidMapFile(newMapFile)) {
			showDialog(DIALOG_MAP_FILE_INVALID);
			return;
		}
		this.mapView.setMapFile(newMapFile);
		this.fileBrowserView.setVisibility(View.GONE);
		this.mainView.setVisibility(View.VISIBLE);
		setMapFileTitle();
	}

	/**
	 * Sets the window title to the current map file name.
	 */
	void setMapFileTitle() {
		setTitle(this.mapView.getMapFile().substring(
				this.mapView.getMapFile().lastIndexOf("/") + 1));
	}

	/**
	 * Displays a text message via the toast notification system. If a previous message is still
	 * visible, the previous message is first removed to avoid jam.
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