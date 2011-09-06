/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.muxe.advancedtouristmap;

import java.io.File;
import java.util.List;

import model.address.android.SqCity;
import model.address.android.SqRoad;

import org.mapsforge.applications.android.advancedmapviewer.R;
import org.mapsforge.geocoding.widget.CityCompletionAdapter;
import org.mapsforge.geocoding.widget.RoadCompletionAdapter;
import org.mapsforge.geocoding.widget.RoadListAdapter;
import org.mapsforge.geocoding.widget.State;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class Search extends BaseActivity {

	State state;
	public static final String PREFERENCES_FILE = "GeocoderPreferences";
	static final String PREFERENCES_FILE_TEMP = "GeocoderTempPreferences";

	private static final String ANDROID_LOG_TAG = "geocoding";

	private static final int MAX_RESULTS = 100;

	private String filename = null;
	protected SQLiteDatabase db;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(ANDROID_LOG_TAG, "create");

		this.filename = this.advancedMapViewer.getBaseBundlePath()
				+ File.separator
				+ this.advancedMapViewer.getCurrentMapBundle().getAddressFile()
						.getRelativePath();
		if (this.filename == null) {
			finish();
		}

		// Toast toast = Toast.makeText(this, "file: " + this.filename, Toast.LENGTH_SHORT);
		// toast.show();

		this.state = new State();

		// if (savedInstanceState == null) {
		// this.state = new State();
		// } else {
		// this.state = (State) savedInstanceState.getSerializable("state");
		// }

		// showDependingOnState();
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.d(ANDROID_LOG_TAG, "saveInstanceState");
		// bundle.putSerializable("state", this.state);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(ANDROID_LOG_TAG, "pause");
		Editor editor = getSharedPreferences(PREFERENCES_FILE_TEMP, MODE_PRIVATE).edit();

		this.state.serializeToEditor(editor);

		this.db.close();
		this.db = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ANDROID_LOG_TAG, "resume");
		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE_TEMP,
				MODE_PRIVATE);

		this.state.deserializeFromPreferences(preferences);

		showDependingOnState();
	}

	public void showDependingOnState() {
		/*
		 * Database.
		 */
		Log.d(ANDROID_LOG_TAG, "opening db: " + this.filename);
		// this.db = SQLiteDatabase
		// .openDatabase(this.filename, null, SQLiteDatabase.OPEN_READONLY);
		if (this.db == null) {
			this.db = SQLiteDatabase.openDatabase(this.filename, null,
					SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}

		this.advancedMapViewer.setViewWithHelp(this, R.layout.search_main);

		// if (this.state.view == State.VIEW_INPUT) {
		showViewInput();
		// }
		// if (this.state.view == State.VIEW_RESULTS) {
		// showViewResults();
		// }
	}

	// AutoCompleteTextView editCity = null;
	AutoCompleteTextView editPlace = null;
	AutoCompleteTextView editRoad = null;

	private void showViewInput() {
		this.state.view = State.VIEW_INPUT;

		setContentView(R.layout.search_main);

		int queryCity = this.state.queryCity;
		// int queryPlace = this.state.queryPlace;
		String queryRoad = this.state.queryRoad;

		final CityCompletionAdapter cityCompletionAdapter = new CityCompletionAdapter(this.db);
		// final PlaceCompletionAdapter placeCompletionAdapter = new PlaceCompletionAdapter(
		// this.db);
		SqCity city = SqCity.getCity(this.db, queryCity);
		// SqPlace place = SqPlace.getPlace(this.db, queryPlace);

		/*
		 * Input fields.
		 */
		this.editPlace = (AutoCompleteTextView) findViewById(R.id.EditText01);
		if (city == null) {
			// if (place == null) {
			this.editPlace.setText("");
		} else {
			this.editPlace.setText(cityCompletionAdapter.getString(city));
			// this.editPlace.setText(placeCompletionAdapter.getString(place));
		}
		this.editPlace.setAdapter(cityCompletionAdapter);
		this.editPlace.setThreshold(1);

		this.editRoad = (AutoCompleteTextView) findViewById(R.id.EditText02);
		this.editRoad.setText(queryRoad);
		this.editRoad.setThreshold(1);
		if (city != null) {
			// if (place != null) {
			// SqCity city = place.getCity();
			this.editRoad.setAdapter(new RoadCompletionAdapter(Search.this.db, city.getId()));
		}

		// this.editCity.setOnFocusChangeListener(new OnFocusChangeListener() {
		//
		// @Override
		// public void onFocusChange(View view, boolean focusState) {
		// Log.d(ANDROID_LOG_TAG, "focus on text: " + focusState);
		// if (!focusState) {
		// // the user is finished typing in the city
		//
		// String cityQuery = Search.this.editCity.getText().toString();
		// List<SqCity> cities = SqCity.getCities(Search.this.db, cityQuery, true, 1);
		// if (cities.size() > 0) {
		// SqCity city = cities.get(0);
		// Search.this.editRoad.setAdapter(new RoadCompletionAdapter(
		// Search.this.db, city.getId()));
		// }
		// }
		// }
		// });

		this.editRoad.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				performSearch();
				// Object item = parent.getAdapter().getItem(position);
				// if (!(item instanceof SqRoad)) {
				// return;
				// }
				// SqRoad road = (SqRoad) item;
			}
		});

		this.editPlace.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object item = cityCompletionAdapter.getItemDataObject(position);
				SqCity newCity = (SqCity) item;
				// SqPlace newPlace = (SqPlace) item;

				Search.this.state.queryCity = newCity.getId();
				// Search.this.state.queryPlace = newPlace.getId();
				// SqCity newCity = newPlace.getCity();
				Search.this.editRoad.setAdapter(new RoadCompletionAdapter(Search.this.db,
						newCity.getId()));

				Search.this.editRoad.requestFocus();
			}
		});

		/*
		 * Search Button.
		 */

		Button button = (Button) findViewById(R.id.Button01);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				performSearch();
			}
		});

	}

	private void showViewResults() {
		this.state.view = State.VIEW_RESULTS;

		hideKeyboard();

		setContentView(R.layout.search_results);

		int queryCity = this.state.queryCity;
		// int queryPlace = this.state.queryPlace;
		String queryRoad = this.state.queryRoad;

		SqCity city = SqCity.getCity(this.db, queryCity);
		// SqPlace place = SqPlace.getPlace(this.db, queryPlace);
		// if (place == null) {
		// // invalid place id...
		// return;
		// }
		// SqCity city = place.getCity();

		List<SqRoad> roads = SqRoad.getRoadsByCity(this.db, city.getId(), queryRoad,
				MAX_RESULTS);
		if (roads.size() == 0) {
			// invalid road name...
			return;
		}

		/*
		 * Result list.
		 */

		showResults(roads);
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		Log.d(ANDROID_LOG_TAG, inputManager + "");
		View view = this.getCurrentFocus();
		if (view == null) {
			return;
		}
		Log.d(ANDROID_LOG_TAG, view + "");
		IBinder token = view.getWindowToken();
		Log.d(ANDROID_LOG_TAG, token + "");
		inputManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// if (this.state.view == State.VIEW_RESULTS) {
		// menu.add("back");
		// return true;
		// }
		// if (this.state.view == State.VIEW_INPUT) {
		// menu.add("search");
		// return true;
		// }
		return false;
	}

	// onPrepareOptionsMenu...

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (this.state.view == State.VIEW_RESULTS) {
				showViewInput();
				return true;
			}
			finish();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			finish();
			return true;
		}
		return false;
	}

	void performSearch() {
		// EditText edit_city = (EditText) findViewById(R.id.EditText01);
		EditText edit_road = (EditText) findViewById(R.id.EditText02);
		// String queryCity = edit_city.getText().toString();
		String queryRoad = edit_road.getText().toString();

		// this.state.queryCity = queryCity;
		this.state.queryRoad = queryRoad;

		showViewResults();
	}

	private void showResults(List<SqRoad> roads) {

		ListView list = (ListView) findViewById(R.id.ListView01);
		RoadListAdapter rla = new RoadListAdapter(this.db, roads);
		list.setAdapter(rla);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int row,
					long whatsthis) {
				Object item = adapterView.getItemAtPosition(row);
				if (!(item instanceof SqRoad)) {
					return;
				}
				SqRoad road = (SqRoad) item;
				finish(road.getLon(), road.getLat());
			}
		});
	}

	void finish(double lon, double lat) {
		setResult(RESULT_OK, new Intent().putExtra("lon", lon).putExtra("lat", lat));
		finish();
	}

	// currently unused...
	void showDiag(String displayText) {
		String shownText = displayText;
		if (displayText == null) {
			EditText edit_city = (EditText) findViewById(R.id.EditText01);
			Editable text = edit_city.getText();
			shownText = text.toString();
		}

		new AlertDialog.Builder(this).setMessage(shownText)
				.setPositiveButton("hey", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
					}
				}).setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						// TODO Auto-generated method stub
					}
				}).show();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (this.state.view == State.VIEW_INPUT) {
				if (this.editPlace != null && this.editPlace.hasFocus()) {
					this.editRoad.requestFocus();
					return true;
				} else if (this.editRoad != null && this.editRoad.hasFocus()) {
					performSearch();
					return true;
				}
			}
		}
		return false;
	}
}
