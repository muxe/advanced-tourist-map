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
