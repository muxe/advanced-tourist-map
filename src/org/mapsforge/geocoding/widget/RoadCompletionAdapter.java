package org.mapsforge.geocoding.widget;

import java.util.ArrayList;
import java.util.List;

import model.address.android.SqRoad;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RoadCompletionAdapter extends SqliteCompletionAdapter<SqRoad> {

	private static final int limit = 20;

	private SQLiteDatabase db;
	private int cityId;

	public RoadCompletionAdapter(SQLiteDatabase db, int cityId) {
		super();
		this.db = db;
		this.cityId = cityId;
	}

	@Override
	public String getString(SqRoad road) {
		return road.getName();
	}

	@Override
	public List<SqRoad> getResults(String needle) {
		Log.i("sql", "request " + needle);
		if (needle == null) {
			return new ArrayList<SqRoad>();
		}
		List<SqRoad> cities = SqRoad.getRoadsByCity(this.db, this.cityId, needle, limit);
		Log.i("sql", "results: " + cities.size());
		return cities;
	}

}
