package org.mapsforge.applications.android.advancedmapviewer;

import java.util.List;

import model.address.android.SqCity;
import model.address.android.SqRoad;

import org.mapsforge.geocoding.widget.CityCompletionAdapter;
import org.mapsforge.geocoding.widget.RoadCompletionAdapter;
import org.mapsforge.geocoding.widget.RoadListAdapter;
import org.mapsforge.geocoding.widget.State;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class Search extends Activity {

	private State state;
	static final String PREFERENCES_FILE = "GeocoderPreferences";
	static final String PREFERENCES_FILE_TEMP = "GeocoderTempPreferences";

	private static final String tag = "geocoding";

	private static final int MAX_RESULTS = 100;

	// TODO: use dynamic file
	private String filename = null;
	protected SQLiteDatabase db;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("states", "create");

		SharedPreferences prefs = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
		this.filename = prefs.getString("addressFile", null);
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

		showDependingOnState();
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.i("states", "saveInstanceState");
		// bundle.putSerializable("state", this.state);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("states", "pause");
		Editor editor = getSharedPreferences(PREFERENCES_FILE_TEMP, MODE_PRIVATE).edit();

		this.state.serializeToEditor(editor);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("states", "resume");
		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE_TEMP,
				MODE_PRIVATE);

		this.state.deserializeFromPreferences(preferences);

		showDependingOnState();
	}

	public void showDependingOnState() {
		/*
		 * Database.
		 */
		Log.i("foo", "opening db: " + this.filename);
		// this.db = SQLiteDatabase
		// .openDatabase(this.filename, null, SQLiteDatabase.OPEN_READONLY);
		this.db = SQLiteDatabase.openOrCreateDatabase(this.filename, null);

		setContentView(R.layout.search_main);

		if (this.state.view == State.VIEW_INPUT) {
			showViewInput();
		}
		if (this.state.view == State.VIEW_RESULTS) {
			showViewResults();
		}
	}

	private void showViewInput() {
		this.state.view = State.VIEW_INPUT;

		setContentView(R.layout.search_main);

		String queryCity = this.state.queryCity;
		String queryRoad = this.state.queryRoad;

		/*
		 * Input fields.
		 */
		final AutoCompleteTextView editCity = (AutoCompleteTextView) findViewById(R.id.EditText01);
		editCity.setText(queryCity);
		editCity.setAdapter(new CityCompletionAdapter(this.db));
		editCity.setThreshold(1);

		final AutoCompleteTextView editRoad = (AutoCompleteTextView) findViewById(R.id.EditText02);
		editRoad.setText(queryRoad);
		editRoad.setThreshold(1);

		editCity.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean focusState) {
				Log.i(tag, "focus on text: " + focusState);
				if (!focusState) {
					// the user is finished typing in the city
					String cityQuery = editCity.getText().toString();
					List<SqCity> cities = SqCity.getCities(Search.this.db, cityQuery, true, 1);
					if (cities.size() > 0) {
						SqCity city = cities.get(0);
						editRoad.setAdapter(new RoadCompletionAdapter(Search.this.db, city
								.getId()));
					}
				}
			}
		});

		if (queryCity != null) {
			List<SqCity> cities = SqCity.getCities(Search.this.db, queryCity, true, 1);
			if (cities.size() > 0) {
				SqCity city = cities.get(0);
				editRoad.setAdapter(new RoadCompletionAdapter(Search.this.db, city.getId()));
			}
		}

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

		setContentView(R.layout.search_results);

		String queryCity = this.state.queryCity;
		String queryRoad = this.state.queryRoad;

		List<SqCity> cities = SqCity.getCities(this.db, queryCity, true, 1);
		if (cities.size() == 0) {
			// invalid city name...
			return;
		}
		SqCity city = cities.get(0);

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(tag, "onCreateOptionMenu");
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
		EditText edit_city = (EditText) findViewById(R.id.EditText01);
		EditText edit_road = (EditText) findViewById(R.id.EditText02);
		String queryCity = edit_city.getText().toString();
		String queryRoad = edit_road.getText().toString();

		this.state.queryCity = queryCity;
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
}
