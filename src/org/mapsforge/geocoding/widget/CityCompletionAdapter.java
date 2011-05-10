package org.mapsforge.geocoding.widget;

import java.util.ArrayList;
import java.util.List;

import model.address.android.SqCity;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class CityCompletionAdapter extends SqliteCompletionAdapter<SqCity> {

	private static final int limit = 20;

	SQLiteDatabase db;

	public CityCompletionAdapter(SQLiteDatabase db) {
		super();
		this.db = db;
	}

	@Override
	public String getString(SqCity city) {
		return city.getName();
	}

	@Override
	public List<SqCity> getResults(String needle) {
		Log.i("sql", "request " + needle);
		if (needle == null) {
			return new ArrayList<SqCity>();
		}
		List<SqCity> cities = SqCity.getCities(this.db, needle, false, limit);
		Log.i("sql", "results: " + cities.size());
		return cities;
	}

}
