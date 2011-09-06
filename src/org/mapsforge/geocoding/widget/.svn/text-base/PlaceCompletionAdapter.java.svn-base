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

import model.address.android.SqPlace;
import model.address.android.SqPostcode;
import android.database.sqlite.SQLiteDatabase;

public class PlaceCompletionAdapter extends SqliteCompletionAdapter<SqPlace> {

	private static final int limit = 20;

	SQLiteDatabase db;

	public PlaceCompletionAdapter(SQLiteDatabase db) {
		super();
		this.db = db;
	}

	@Override
	public String getString(SqPlace place) {
		String result = place.getName();

		SqPostcode postcode = place.getPostcode();
		if (postcode != null) {
			result += " (" + postcode.getPostcode() + ")";
		}
		return result;
	}

	@Override
	public List<SqPlace> getResults(String needle) {
		if (needle == null) {
			return new ArrayList<SqPlace>();
		}
		List<SqPlace> places = SqPlace.getPlaces(this.db, needle, false, limit);
		return places;
	}

}
