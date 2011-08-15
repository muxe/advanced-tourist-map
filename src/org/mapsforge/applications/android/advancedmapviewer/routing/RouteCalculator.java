package org.mapsforge.applications.android.advancedmapviewer.routing;

import java.io.File;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.applications.android.advancedmapviewer.BaseActivity;
import org.mapsforge.applications.android.advancedmapviewer.LocationPicker;
import org.mapsforge.applications.android.advancedmapviewer.R;
import org.mapsforge.applications.android.advancedmapviewer.Search;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.RoutingFile;
import org.mapsforge.core.Edge;
import org.mapsforge.core.GeoCoordinate;
import org.mapsforge.core.Router;
import org.mapsforge.core.Vertex;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class RouteCalculator extends BaseActivity {
	static final String TAG = RouteCalculator.class.getSimpleName();

	protected static final int INTENT_SEARCH = 0;
	protected static final int INTENT_MAP = 1;

	protected static final int START_FIELD = 0;
	protected static final int DEST_FIELD = 1;

	protected static final int DIALOG_CHOOSE_INPUT = 0;

	private static final int TWO_MINUTES = 1000 * 60 * 2;

	GeoPoint startPoint;
	GeoPoint destPoint;

	ImageButton chooseStartButton;
	ImageButton chooseDestButton;
	private Button calcRouteButton;

	private EditText startEditText;
	private EditText destEditText;
	int viewToSet;

	ProgressDialog progressDialog;

	private LocationManager locationManager;
	private LocationListener locationListener;
	Location currentBestLocation;

	Spinner routingFileSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("lifecycle", "routeCalculator onCreate");
		setContentView(R.layout.activity_calculate_route);

		Intent startingIntent = getIntent();
		if (startingIntent.hasExtra("lat") && startingIntent.hasExtra("lon")) {
			this.destPoint = new GeoPoint(startingIntent.getDoubleExtra("lat", 0.0),
					startingIntent.getDoubleExtra("lon", 0.0));
		}

		this.startEditText = (EditText) findViewById(R.id.calculate_route_edittext_start);
		this.destEditText = (EditText) findViewById(R.id.calculate_route_edittext_dest);

		this.chooseStartButton = (ImageButton) findViewById(R.id.calculate_route_button_choose_start);
		this.chooseDestButton = (ImageButton) findViewById(R.id.calculate_route_button_choose_dest);
		this.calcRouteButton = (Button) findViewById(R.id.calculate_route_button_calculate);

		this.routingFileSpinner = (Spinner) findViewById(R.id.calculate_route_spinner_routing_file);

		OnClickListener startDestChooserListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getId() == RouteCalculator.this.chooseStartButton.getId()) {
					RouteCalculator.this.viewToSet = RouteCalculator.START_FIELD;
				} else if (v.getId() == RouteCalculator.this.chooseDestButton.getId()) {
					RouteCalculator.this.viewToSet = RouteCalculator.DEST_FIELD;
				}
				// open a dialog to select method to chose start/dest
				showDialog(DIALOG_CHOOSE_INPUT);
			}
		};

		this.chooseStartButton.setOnClickListener(startDestChooserListener);
		this.chooseDestButton.setOnClickListener(startDestChooserListener);

		this.calcRouteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: if no routing file selected?
				// still nullpointer exception on no routing files
				RoutingFile rf = (RoutingFile) RouteCalculator.this.routingFileSpinner
						.getSelectedItem();
				if (RouteCalculator.this.startPoint == null) {
					Toast.makeText(RouteCalculator.this,
							getString(R.string.routing_no_start_selected), Toast.LENGTH_LONG)
							.show();
					return;
				}
				if (RouteCalculator.this.destPoint == null) {
					Toast.makeText(RouteCalculator.this,
							getString(R.string.routing_no_destination_selected),
							Toast.LENGTH_LONG).show();
					return;
				}
				new CalculateRouteAsync().execute(rf);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_SEARCH) {
			if (resultCode == RESULT_OK) {
				if (data != null && data.hasExtra("lon") && data.hasExtra("lat")) {
					double lon = data.getDoubleExtra("lon", 0.0);
					double lat = data.getDoubleExtra("lat", 0.0);
					GeoPoint point = new GeoPoint(lat, lon);
					if (this.viewToSet == RouteCalculator.START_FIELD) {
						this.startPoint = point;
					} else {
						this.destPoint = point;
					}
				}
			}
		} else if (requestCode == INTENT_MAP) {
			// TODO: not DRY yet
			// TODO: find nearest vertex first?
			if (resultCode == RESULT_OK) {
				if (data != null && data.hasExtra("LONGITUDE") && data.hasExtra("LATITUDE")) {
					double lon = data.getDoubleExtra("LONGITUDE", 0.0);
					double lat = data.getDoubleExtra("LATITUDE", 0.0);
					GeoPoint point = new GeoPoint(lat, lon);
					if (this.viewToSet == RouteCalculator.START_FIELD) {
						this.startPoint = point;
					} else {
						this.destPoint = point;
					}
				}
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (dialogId == DIALOG_CHOOSE_INPUT) {
			final String[] items = getResources().getStringArray(
					R.array.routing_point_picker_values);
			final String[] items_keys = getResources().getStringArray(
					R.array.routing_point_picker_keys);
			builder.setTitle(R.string.dialog_title_find_location);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					if (items_keys[item].equals("ADDRESS")) {
						if (RouteCalculator.this.advancedMapViewer.getCurrentMapBundle()
								.isSearchable()) {
							startActivityForResult(new Intent(RouteCalculator.this,
									Search.class), INTENT_SEARCH);
						} else {
							// TODO:
							Toast.makeText(RouteCalculator.this,
									getString(R.string.addressfile_not_avaiable),
									Toast.LENGTH_LONG).show();
						}
					} else if (items_keys[item].equals("POSITION")) {
						startPositionSearch();
					} else if (items_keys[item].equals("MAP")) {
						startActivityForResult(new Intent(RouteCalculator.this,
								LocationPicker.class), INTENT_MAP);
						// new Intent(RouteCalculator.this, AdvancedMapViewer.class)
						// .putExtra("mode", "LOCATION_PICKER"), INTENT_MAP);
					}
				}
			});
			return builder.create();
		}
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("lifecycle", "routeCalculator onResume");

		if (this.locationManager == null) {
			this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}

		if (this.startPoint != null) {
			this.startEditText.setText(this.startPoint.getLatitude() + " "
					+ this.startPoint.getLongitude());
		}
		if (this.destPoint != null) {
			this.destEditText.setText(this.destPoint.getLatitude() + " "
					+ this.destPoint.getLongitude());
		}

		RoutingFile[] routingFiles = this.advancedMapViewer.getCurrentMapBundle()
				.getRoutingFilesArray();

		ArrayAdapter<RoutingFile> adapter = new ArrayAdapter<RoutingFile>(this,
				android.R.layout.simple_spinner_item, routingFiles);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.routingFileSpinner.setAdapter(adapter);
	}

	void startPositionSearch() {
		// TODO: exit strategy (timer and/or when signal stabilized)
		// TODO: user feedback about background progress (like spinning animation or something)
		// check if already running, if so, stop first
		if (this.locationListener != null) {
			stopPositionSearch();
		}

		// get cached locations first
		Location currentLocation;
		for (String provider : this.locationManager.getProviders(true)) {
			currentLocation = this.locationManager.getLastKnownLocation(provider);
			if (currentLocation != null) {
				if (isBetterLocation(currentLocation, this.currentBestLocation)) {
					this.currentBestLocation = currentLocation;
					changeStartStop(RouteCalculator.this.viewToSet, new GeoPoint(
							currentLocation.getLatitude(), currentLocation.getLongitude()));
					Log.d(TAG, "got better cached location from: " + provider + " ("
							+ currentLocation.getAccuracy() + ")");
				} else {
					Log.d(TAG,
							"dismissed location from: " + provider + " ("
									+ currentLocation.getAccuracy() + ")");
				}
			}
		}

		this.locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				// GeoPoint point = new GeoPoint(location.getLatitude(),
				// location.getLongitude());
				if (isBetterLocation(location, RouteCalculator.this.currentBestLocation)) {
					RouteCalculator.this.currentBestLocation = location;
					Log.d(TAG, "better location from: " + location.getProvider() + " ("
							+ location.getAccuracy() + ")");
					changeStartStop(RouteCalculator.this.viewToSet,
							new GeoPoint(location.getLatitude(), location.getLongitude()));
				} else {
					Log.d(TAG, "dismissed location from: " + location.getProvider() + " ("
							+ location.getAccuracy() + ")");
				}
			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onProviderEnabled(String provider) {
				// do nothing
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				if (status == LocationProvider.AVAILABLE) {

				} else if (status == LocationProvider.OUT_OF_SERVICE) {

				} else {
					// must be TEMPORARILY_UNAVAILABLE
				}
			}
		};

		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
				this.locationListener);
		this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0,
				this.locationListener);
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation1
	 *            The current Location fix, to which you want to compare the new one
	 * @return boolean whether the location is better than the current best location
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation1) {
		if (currentBestLocation1 == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation1.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation1.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation1.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 * 
	 * @param provider1
	 *            First Provider Name
	 * @param provider2
	 *            Second Provider Name
	 * @return boolean if the two providers are the same
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("lifecycle", "routeCalculator onPause");
		stopPositionSearch();
	}

	private void stopPositionSearch() {
		if (this.locationListener != null) {
			this.locationManager.removeUpdates(this.locationListener);
			this.locationListener = null;
		}
	}

	void changeStartStop(int field, GeoPoint point) {
		if (field == RouteCalculator.START_FIELD) {
			this.startPoint = point;
			this.startEditText.setText(this.startPoint.getLatitude() + " "
					+ this.startPoint.getLongitude());
		} else if (field == RouteCalculator.DEST_FIELD) {
			this.destPoint = point;
			this.destEditText.setText(this.destPoint.getLatitude() + " "
					+ this.destPoint.getLongitude());
		}
	}

	private class CalculateRouteAsync extends AsyncTask<RoutingFile, Void, Route> {

		public CalculateRouteAsync() {
			super();
		}

		@Override
		protected void onPreExecute() {
			RouteCalculator.this.progressDialog = ProgressDialog.show(RouteCalculator.this, "",
					"Loading. Please wait...", true);
		}

		@Override
		protected Route doInBackground(RoutingFile... routingFiles) {
			// calculate
			RoutingFile rf = null;
			try {
				rf = routingFiles[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}

			String path = RouteCalculator.this.advancedMapViewer.getBaseBundlePath()
					+ File.separator + rf.getRelativePath();
			Router router = RouteCalculator.this.advancedMapViewer.getRouter(path);
			if (router == null) {
				return null;
			}
			Vertex start = router.getNearestVertex(new GeoCoordinate(
					RouteCalculator.this.startPoint.getLatitude(),
					RouteCalculator.this.startPoint.getLongitude()));
			Vertex dest = router.getNearestVertex(new GeoCoordinate(
					RouteCalculator.this.destPoint.getLatitude(),
					RouteCalculator.this.destPoint.getLongitude()));

			Edge[] edges = router.getShortestPath(start.getId(), dest.getId());

			// try {
			// Thread.sleep(10000);
			// } catch (InterruptedException e) {
			//
			// }

			if (edges.length > 0) {
				Route route = new Route(edges);
				Log.d(TAG, "done");
				Log.d(TAG, "length: " + edges.length);
				return route;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Route route) {
			// remove progress bar
			RouteCalculator.this.progressDialog.dismiss();
			// show route
			if (route != null) {
				RouteCalculator.this.advancedMapViewer.currentRoute = route;
				startActivity(new Intent(RouteCalculator.this, RouteList.class));
			} else {
				Log.d(TAG, "No Route Found");
				Toast.makeText(RouteCalculator.this,
						getString(R.string.routing_no_route_found), Toast.LENGTH_LONG).show();
			}
		}
	}
}
