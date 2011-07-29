/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import org.mapsforge.android.maps.ArrayCircleOverlay;
import org.mapsforge.android.maps.ArrayWayOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapController;
import org.mapsforge.android.maps.MapDatabase;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapView.TextField;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.OverlayCircle;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.applications.android.advancedmapviewer.routing.DecisionOverlay;
import org.mapsforge.applications.android.advancedmapviewer.routing.Route;
import org.mapsforge.applications.android.advancedmapviewer.routing.RouteList;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.FileManagerActivity;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.MapBundle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A map application which uses the features from the mapsforge library. The map can be centered
 * to the current GPS coordinate. A simple file browser for selecting the map file is also
 * included. Some preferences can be adjusted via the EditPreferences activity and screenshots
 * of the map may be taken in different image formats.
 */
public class AdvancedMapViewer extends MapActivity {
	private static final int DIALOG_ENTER_COORDINATES = 0;
	private static final int DIALOG_GPS_DISABLED = 1;
	private static final int DIALOG_INFO_MAP_FILE = 2;
	private static final String SCREENSHOT_DIRECTORY = "Pictures";
	private static final String SCREENSHOT_FILE_NAME = "Map screenshot";
	private static final int SCREENSHOT_QUALITY = 90;
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

	AdvancedMapViewerApplication advancedMapViewerApplication;

	ArrayCircleOverlay circleOverlay;
	private Paint circleOverlayFill;
	private Paint circleOverlayOutline;
	private ArrayWayOverlay routeOverlay;
	private DecisionOverlay decisionPointOverlay;
	private boolean displayRoute;

	// routing menu
	LinearLayout routeMenu;
	private ImageButton nextDecisionPointButton;
	private ImageButton previousDecisionPointButton;
	private ImageButton showRoutingListButton;

	private boolean followGpsEnabled;
	boolean centerGpsEnabled;
	GeoPoint lastPosition;
	private LocationListener locationListener;
	private LocationManager locationManager;
	private MapViewMode mapViewMode;
	private PowerManager powerManager;
	private SharedPreferences preferences;
	private Toast toast;
	private WakeLock wakeLock;
	ImageView gpsView;
	ImageView centerView;
	MapController mapController;
	MapView mapView;
	OverlayCircle overlayCircle;
	boolean locationPickerMode;

	private GestureDetector mGestureDetector;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// disable auto following position on scrolling the map
		if (this.followGpsEnabled && this.centerGpsEnabled) {
			this.mGestureDetector.onTouchEvent(ev);
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_info:
				return true;

			case R.id.menu_info_map_file:
				showDialog(DIALOG_INFO_MAP_FILE);
				return true;

			case R.id.menu_info_about:
				startActivity(new Intent(this, InfoView.class));
				return true;

			case R.id.menu_position:
				return true;

			case R.id.menu_position_gps_toggle:
				if (this.followGpsEnabled) {
					disableFollowGPS(true);
				} else {
					if (this.locationManager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
							|| this.locationManager
									.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						enableFollowGPS();
					} else {
						showDialog(DIALOG_GPS_DISABLED);
					}
				}
				return true;

			case R.id.menu_position_enter_coordinates:
				showDialog(DIALOG_ENTER_COORDINATES);
				return true;

			case R.id.menu_position_map_center:
				// disable GPS follow mode if it is enabled
				disableFollowGPS(true);
				this.mapController.setCenter(this.mapView.getMapDatabase().getMapCenter());
				return true;

			case R.id.menu_screenshot:
				return true;

			case R.id.menu_screenshot_jpeg:
				captureScreenshotAsync(CompressFormat.JPEG);
				return true;

			case R.id.menu_screenshot_png:
				captureScreenshotAsync(CompressFormat.PNG);
				return true;

			case R.id.menu_preferences:
				startActivity(new Intent(this, EditPreferences.class));
				return true;

				// case R.id.menu_mapfile:
				// startFileBrowser();
				// return true;
			case R.id.menu_mapfile:
				startBundleBrowser();
				return true;

			default:
				return false;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_info_map_file).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_info_map_file).setEnabled(true);
		}

		MenuItem toggleGps = menu.findItem(R.id.menu_position_gps_toggle);
		if (this.locationManager == null) {
			toggleGps.setEnabled(false);
		} else if (this.followGpsEnabled) {
			toggleGps.setTitle(R.string.menu_position_gps_disable);
		} else {
			toggleGps.setTitle(R.string.menu_position_gps_enable);
		}

		if (this.mapView.getMapViewMode().requiresInternetConnection()) {
			menu.findItem(R.id.menu_position_map_center).setEnabled(false);
		} else {
			menu.findItem(R.id.menu_position_map_center).setEnabled(true);
		}

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

	private void captureScreenshotAsync(final CompressFormat format) {
		new Thread() {
			@Override
			public void run() {
				try {
					File path = new File(Environment.getExternalStorageDirectory(),
							SCREENSHOT_DIRECTORY);
					// make sure the Pictures directory exists
					if (!path.exists() && !path.mkdirs()) {
						showToastOnUiThread("Could not create target directory");
						return;
					}

					// assemble the complete name for the screenshot file
					String fileName = path.getAbsolutePath() + File.separatorChar
							+ SCREENSHOT_FILE_NAME + "." + format.name().toLowerCase();

					if (AdvancedMapViewer.this.mapView.makeScreenshot(format,
							SCREENSHOT_QUALITY, fileName)) {
						// success
						showToastOnUiThread(fileName);
					} else {
						// failure
						showToastOnUiThread("Screenshot could not be saved");
					}
				} catch (IOException e) {
					showToastOnUiThread(e.getLocalizedMessage());
				}
			}

			private void showToastOnUiThread(final String message) {
				AdvancedMapViewer.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast(message);
					}
				});
			}
		}.start();
	}

	private void configureMapView() {
		// configure the MapView and activate the zoomLevel buttons
		this.mapView.setClickable(true);
		this.mapView.setBuiltInZoomControls(true);
		this.mapView.setFocusable(true);

		// set the localized text fields
		this.mapView.setText(TextField.KILOMETER, getString(R.string.unit_symbol_kilometer));
		this.mapView.setText(TextField.METER, getString(R.string.unit_symbol_meter));

		// get the map controller for this MapView
		this.mapController = this.mapView.getController();
	}

	private void enableFollowGPS() {
		this.circleOverlay = new ArrayCircleOverlay(this.circleOverlayFill,
				this.circleOverlayOutline);
		this.overlayCircle = new OverlayCircle();
		this.circleOverlay.addCircle(this.overlayCircle);
		this.mapView.getOverlays().add(this.circleOverlay);

		this.followGpsEnabled = true;
		this.centerGpsEnabled = true;
		this.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
				AdvancedMapViewer.this.lastPosition = point;
				if (AdvancedMapViewer.this.centerGpsEnabled) {
					AdvancedMapViewer.this.mapController.setCenter(point);
				} else {
					AdvancedMapViewer.this.circleOverlay.requestRedraw();
				}
				AdvancedMapViewer.this.gpsView.setImageResource(R.drawable.stat_sys_gps_on);
				AdvancedMapViewer.this.overlayCircle.setCircleData(point,
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

		if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
					this.locationListener);
		} else {
			this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,
					0, this.locationListener);
			showToast("Enable GPS for blaa");
		}
		this.gpsView.setImageResource(R.drawable.stat_sys_gps_acquiring);
		this.gpsView.setVisibility(View.VISIBLE);
		this.gpsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableFollowGPS(true);
			}
		});

		// the icon to center the view to position
		this.centerView.setImageResource(android.R.drawable.ic_secure);
		this.centerView.setVisibility(View.VISIBLE);
		this.centerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdvancedMapViewer.this.centerGpsEnabled = true;
				AdvancedMapViewer.this.centerView
						.setImageResource(android.R.drawable.ic_secure);
				if (AdvancedMapViewer.this.lastPosition != null) {
					AdvancedMapViewer.this.mapController
							.setCenter(AdvancedMapViewer.this.lastPosition);
				}
			}
		});
	}

	// /**
	// * Sets all file filters and starts the FilePicker.
	// */
	// private void startFileBrowser() {
	// // set the FileDisplayFilter
	// FilePicker.setFileDisplayFilter(new FileFilter() {
	// @Override
	// public boolean accept(File file) {
	// // accept only readable files
	// if (file.canRead()) {
	// if (file.isDirectory()) {
	// // accept all directories
	// return true;
	// } else if (file.isFile() && file.getName().endsWith(".map")) {
	// // accept all files with a ".map" extension
	// return true;
	// }
	// }
	// return false;
	// }
	// });
	//
	// // set the FileSelectFilter
	// FilePicker.setFileSelectFilter(new FileFilter() {
	// @Override
	// public boolean accept(File file) {
	// // accept only valid map files
	// return MapDatabase.isValidMapFile(file.getAbsolutePath());
	// }
	// });
	//
	// // start the FilePicker
	// startActivityForResult(new Intent(this, FilePicker.class), SELECT_MAP_FILE);
	// }

	private void startBundleBrowser() {
		startActivity(new Intent(this, FileManagerActivity.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SELECT_MAP_FILE) {
			if (resultCode == RESULT_OK) {
				disableFollowGPS(true);
				if (data != null && data.getStringExtra("selectedFile") != null) {
					this.mapView.setMapFile(data.getStringExtra("selectedFile"));
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
		Log.d("lifecycle", "amv onCreate");

		this.advancedMapViewerApplication = (AdvancedMapViewerApplication) getApplication();

		// set up the layout views
		setContentView(R.layout.activity_advanced_map_viewer);
		this.mapView = (MapView) findViewById(R.id.mapView);
		this.gpsView = (ImageView) findViewById(R.id.gpsView);
		this.centerView = (ImageView) findViewById(R.id.centerView);
		this.routeMenu = (LinearLayout) findViewById(R.id.routeMenu);

		configureMapView();

		// get the pointers to different system services
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

		// initialize route overlay
		Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fillPaint.setStyle(Paint.Style.STROKE);
		fillPaint.setColor(Color.BLUE);
		fillPaint.setAlpha(160);
		fillPaint.setStrokeWidth(6);
		fillPaint.setStrokeCap(Paint.Cap.ROUND);
		fillPaint.setStrokeJoin(Paint.Join.ROUND);
		this.routeOverlay = new ArrayWayOverlay(fillPaint, null);
		this.mapView.getOverlays().add(this.routeOverlay);

		this.decisionPointOverlay = new DecisionOverlay(getResources().getDrawable(
				R.drawable.jog_tab_target_gray));
		this.mapView.getOverlays().add(this.decisionPointOverlay);

		if (savedInstanceState != null && savedInstanceState.getBoolean("locationListener")) {
			if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				enableFollowGPS();
			} else {
				showDialog(DIALOG_GPS_DISABLED);
			}
		}

		// Basic "invisible" Overlay to handle long presses on map
		this.mapView.getOverlays().add(new Overlay() {
			@Override
			protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
					Projection projection, byte drawZoomLevel) {

			}

			@Override
			public boolean onLongPress(GeoPoint geoPoint, MapView mv) {
				// map view is in locationPicerMode, so just return tap point
				if (AdvancedMapViewer.this.locationPickerMode) {
					Log.d("RouteCalculator", "location picker mode on");
					AdvancedMapViewer.this.setResult(RESULT_OK,
							new Intent().putExtra("LONGITUDE", geoPoint.getLongitude())
									.putExtra("LATITUDE", geoPoint.getLatitude()));
					AdvancedMapViewer.this.finish();
					return true;
				}
				startActivity(new Intent(AdvancedMapViewer.this, PositionInfo.class).putExtra(
						"LATITUDE", geoPoint.getLatitude()).putExtra("LONGITUDE",
						geoPoint.getLongitude()));
				return true;
			}

		});

		this.mGestureDetector = new GestureDetector(this, new ScrollListener());

		/*
		 * POI STUFF EXPERIMENTAL
		 */

		// Log.d("Restaurant", "achtung jetzt:");
		// IPersistenceManager perstManager = PersistenceManagerFactory
		// .getPerstPersistenceManager("/sdcard/perstPoi.dbs");
		// PoiCategory restCat = new CategoryBuilder("Restaurant").build();
		// long id = 1337;
		// PointOfInterest poi = new PoiBuilder(id, 52.455667, 13.298357, "My Test", "",
		// restCat)
		// .build();
		// perstManager.insertPointOfInterest(poi);
		// TODO: isnt permanent
		// LinkedList<PointOfInterest> restaurants = new LinkedList<PointOfInterest>(
		// perstManager.findNearPosition(new GeoCoordinate(52.4561009222, 13.297641277),
		// 1000, "Restaurant", 50));
		// perstManager.close();
		//
		// // create the default paint objects for overlay circles
		// Paint circleDefaultPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
		// circleDefaultPaintFill.setStyle(Paint.Style.FILL);
		// circleDefaultPaintFill.setColor(Color.BLUE);
		// circleDefaultPaintFill.setAlpha(64);
		//
		// Paint circleDefaultPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
		// circleDefaultPaintOutline.setStyle(Paint.Style.STROKE);
		// circleDefaultPaintOutline.setColor(Color.BLUE);
		// circleDefaultPaintOutline.setAlpha(128);
		// circleDefaultPaintOutline.setStrokeWidth(3);
		//
		// // create the CircleOverlay and add the circles
		// ArrayCircleOverlay circleOverlay2 = new ArrayCircleOverlay(circleDefaultPaintFill,
		// circleDefaultPaintOutline, this);
		// // OverlayCircle circle1 = new OverlayCircle(restaurants.get(0).getGeoPoint(), 10,
		// // restaurants.get(0).getName());
		// // circleOverlay2.addCircle(circle1);
		//
		// for (int i = 0; i < restaurants.size(); i++) {
		// Log.d("Restaurant", restaurants.get(i).getName());
		// circleOverlay2.addCircle(new OverlayCircle(restaurants.get(i).getGeoPoint(), 50,
		// restaurants.get(i).getName()));
		// }
		//
		// this.mapView.getOverlays().add(circleOverlay2);
	}

	/**
	 * This Listener checks if the map gets moved/scrolled a given distance and if so, disables
	 * the auto following of the position.
	 */
	class ScrollListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			double delta = Math.sqrt(Math.pow((e1.getX() - e2.getX()), 2)
					+ Math.pow((e1.getY() - e2.getY()), 2));
			// only disable, if scrolled for a certain distance
			if (delta > 50) {
				AdvancedMapViewer.this.centerGpsEnabled = false;
				AdvancedMapViewer.this.centerView
						.setImageResource(android.R.drawable.ic_lock_idle_lock);
			}
			return true;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (id == DIALOG_ENTER_COORDINATES) {
			builder.setIcon(android.R.drawable.ic_menu_mylocation);
			builder.setTitle(R.string.menu_position_enter_coordinates);
			LayoutInflater factory = LayoutInflater.from(this);
			final View view = factory.inflate(R.layout.dialog_enter_coordinates, null);
			builder.setView(view);
			builder.setPositiveButton(R.string.go_to_position,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// disable GPS follow mode if it is enabled
							disableFollowGPS(true);

							// set the map center and zoom level
							AdvancedMapViewer.this.mapController.setCenter(new GeoPoint(Double
									.parseDouble(((EditText) view.findViewById(R.id.latitude))
											.getText().toString()), Double
									.parseDouble(((EditText) view.findViewById(R.id.longitude))
											.getText().toString())));
							AdvancedMapViewer.this.mapController.setZoom(((SeekBar) view
									.findViewById(R.id.zoomlevel)).getProgress());
						}
					});
			builder.setNegativeButton(R.string.cancel, null);
			return builder.create();
		} else if (id == DIALOG_GPS_DISABLED) {
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.gps_disabled);
			builder.setPositiveButton(R.string.ok, null);
			return builder.create();
		} else if (id == DIALOG_INFO_MAP_FILE) {
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setTitle(R.string.menu_info_map_file);
			LayoutInflater factory = LayoutInflater.from(this);
			builder.setView(factory.inflate(R.layout.dialog_info_map_file, null));
			builder.setPositiveButton(R.string.ok, null);
			return builder.create();
		} else {
			// do dialog will be created
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("lifecycle", "amv onDestroy");

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
		Log.d("lifecycle", "amv onPause");
		// release the wake lock if necessary
		if (this.wakeLock.isHeld()) {
			this.wakeLock.release();
		}
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		if (id == DIALOG_ENTER_COORDINATES) {
			// latitude
			EditText editText = (EditText) dialog.findViewById(R.id.latitude);
			GeoPoint mapCenter = this.mapView.getMapCenter();
			editText.setText(Double.toString(mapCenter.getLatitude()));

			// longitude
			editText = (EditText) dialog.findViewById(R.id.longitude);
			editText.setText(Double.toString(mapCenter.getLongitude()));

			// zoom level
			SeekBar zoomlevel = (SeekBar) dialog.findViewById(R.id.zoomlevel);
			zoomlevel.setMax(this.mapView.getMaxZoomLevel());
			zoomlevel.setProgress(this.mapView.getZoomLevel());

			// zoom level value
			final TextView textView = (TextView) dialog.findViewById(R.id.zoomlevelValue);
			textView.setText(String.valueOf(zoomlevel.getProgress()));
			zoomlevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					textView.setText(String.valueOf(progress));
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
		} else if (id == DIALOG_INFO_MAP_FILE) {
			// map file name
			TextView textView = (TextView) dialog.findViewById(R.id.infoMapFileViewName);
			textView.setText(this.mapView.getMapFile());

			// map file name
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewDebug);
			MapDatabase mapDatabase = this.mapView.getMapDatabase();
			if (mapDatabase.isDebugFile()) {
				textView.setText(R.string.info_map_file_debug_yes);
			} else {
				textView.setText(R.string.info_map_file_debug_no);
			}

			// map file date
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewDate);
			Date date = new Date(mapDatabase.getMapDate());
			textView.setText(DateFormat.getDateTimeInstance().format(date));

			// map file area
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewArea);
			Rect mapArea = mapDatabase.getMapBoundary();
			textView.setText(mapArea.top / 1000000d + ", " + mapArea.left / 1000000d + " – \n"
					+ mapArea.bottom / 1000000d + ", " + mapArea.right / 1000000d);

			// map file start position
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewStart);
			GeoPoint startPosition = mapDatabase.getStartPosition();
			if (startPosition == null) {
				textView.setText(null);
			} else {
				textView.setText(startPosition.getLatitude() + ", "
						+ startPosition.getLongitude());
			}

			// map file comment text
			textView = (TextView) dialog.findViewById(R.id.infoMapFileViewComment);
			String commentText = mapDatabase.getCommentText();
			if (commentText == null) {
				textView.setText(null);
			} else {
				textView.setText(mapDatabase.getCommentText());
			}
		} else {
			super.onPrepareDialog(id, dialog);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("lifecycle", "amv onResume");
		// check if mapview was started, just to return a position
		Intent startingIntent = getIntent();
		if (startingIntent.hasExtra("mode")
				&& startingIntent.getStringExtra("mode").equals("LOCATION_PICKER")) {
			this.locationPickerMode = true;
		}
		// Read the default shared preferences
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// set the map settings
		this.mapView.setScaleBar(this.preferences.getBoolean("showScaleBar", false));
		if (this.preferences.contains("mapViewMode")) {
			this.mapViewMode = Enum.valueOf(MapViewMode.class, this.preferences.getString(
					"mapViewMode", MapView.getDefaultMapViewMode().name()));
			this.mapView.setMapViewMode(this.mapViewMode);
		}
		try {
			this.mapView.setTextScale(Float.parseFloat(this.preferences.getString("textScale",
					"1")));
		} catch (NumberFormatException e) {
			this.mapView.setTextScale(1);
		}

		// set the general settings
		if (this.preferences.getBoolean("fullscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		if (this.preferences.getBoolean("wakeLock", false)) {
			if (!this.wakeLock.isHeld()) {
				this.wakeLock.acquire();
			}
		}
		this.mapView.setMemoryCardCachePersistence(this.preferences.getBoolean(
				"cachePersistence", false));
		this.mapView.setMemoryCardCacheSize(Math.min(
				this.preferences.getInt("cacheSize", MEMORY_CARD_CACHE_SIZE_DEFAULT),
				MEMORY_CARD_CACHE_SIZE_MAX));
		this.mapView
				.setMoveSpeed(Math.min(
						this.preferences.getInt("moveSpeed", MOVE_SPEED_DEFAULT),
						MOVE_SPEED_MAX) / 10f);

		// set the debug settings
		this.mapView.setFpsCounter(this.preferences.getBoolean("showFpsCounter", false));
		this.mapView.setTileFrames(this.preferences.getBoolean("showTileFrames", false));
		this.mapView.setTileCoordinates(this.preferences.getBoolean("showTileCoordinates",
				false));
		this.mapView.setWaterTiles(this.preferences.getBoolean("showWaterTiles", false));

		// check if the file browser needs to be displayed
		// if (!this.mapView.getMapViewMode().requiresInternetConnection()
		// && !this.mapView.hasValidMapFile()) {
		// startFileBrowser();
		// }

		// TODO: imma workin here
		// problem: ändert man den basepath über die settings, kann er zwar das bundle noch
		// laden (da voller path drinsteht), aber dann die einzelnen files nicht mehr (da
		// diesese zusammengesetzt werden)
		MapBundle mapBundle = this.advancedMapViewerApplication.getCurrentMapBundle();
		if (mapBundle == null) {
			this.startBundleBrowser();
		} else {

			// String mapBinary = this.preferences.getString("mapBinary", null);
			String mapBinary = this.advancedMapViewerApplication.getBaseBundlePath()
					+ File.separator + mapBundle.getMapFile().getRelativePath();
			Log.d("Application", mapBinary);
			// if offlinemap AND (map isnt set yet OR current set map differs from map to set)
			if (!this.mapView.getMapViewMode().requiresInternetConnection()
					&& (this.mapView.getMapFile() == null || !this.mapView.getMapFile().equals(
							mapBinary))) {
				Log.d("Application", "Had to reset mapfile! old: " + this.mapView.getMapFile()
						+ ", new: " + mapBinary);
				this.mapView.setMapFile(mapBinary);
			}
		}

		// draw the route, if there is any
		this.decisionPointOverlay.clear();
		this.routeOverlay.clear();
		if (this.advancedMapViewerApplication.currentRoute != null) {
			this.displayRoute = true;
			this.routeOverlay.addWay(this.advancedMapViewerApplication.currentRoute
					.getOverlayWay());
			this.decisionPointOverlay.addItems(Arrays
					.asList(this.advancedMapViewerApplication.currentRoute.getOverlayItems()));
			this.setupRoutingMenu(this.advancedMapViewerApplication.currentRoute);
			if (startingIntent.getBooleanExtra("ROUTE_OVERVIEW", false)) {
				// TODO: better centering of the route
				this.mapController.setCenter(this.advancedMapViewerApplication.currentRoute
						.getGeoPoints()[0]);
				this.mapController.setZoom(16);
			}
			if (startingIntent.getBooleanExtra("CENTER_DP", false)) {
				this.mapController
						.setCenter(this.advancedMapViewerApplication.currentRoute.currentDecisionPoint
								.getGeoPoint());
				// this.mapController.setZoom(16);
			}
		} else {
			this.displayRoute = false;
			this.routeMenu.setVisibility(View.GONE);
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
			this.lastPosition = null;
			this.gpsView.setVisibility(View.GONE);
			this.centerView.setVisibility(View.GONE);
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
			this.toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
		} else {
			this.toast.cancel();
			this.toast.setText(text);
		}
		this.toast.show();
	}

	/**
	 * Sets up the Routing menu for a given Route and makes it visible. Has a button to display
	 * the decision point list and two buttons to jump to the next/previous decision point in
	 * list
	 * 
	 * @param route
	 *            the route to set the menu for
	 */
	private void setupRoutingMenu(final Route route) {
		if (this.showRoutingListButton == null) {
			this.showRoutingListButton = (ImageButton) findViewById(R.id.route_menu_list);
		}
		if (this.nextDecisionPointButton == null) {
			this.nextDecisionPointButton = (ImageButton) findViewById(R.id.route_menu_next);
		}
		if (this.previousDecisionPointButton == null) {
			this.previousDecisionPointButton = (ImageButton) findViewById(R.id.route_menu_prev);
		}

		this.showRoutingListButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(AdvancedMapViewer.this, RouteList.class));
			}
		});
		this.nextDecisionPointButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdvancedMapViewer.this.mapController.setCenter(route.getNextDP().getGeoPoint());
			}
		});
		this.previousDecisionPointButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AdvancedMapViewer.this.mapController.setCenter(route.getPreviousDP()
						.getGeoPoint());
			}
		});
		this.routeMenu.setVisibility(View.VISIBLE);
	}
}