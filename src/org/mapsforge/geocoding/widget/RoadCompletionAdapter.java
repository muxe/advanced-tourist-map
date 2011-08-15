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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.address.android.SqRoad;
import android.database.sqlite.SQLiteDatabase;

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
		if (needle == null) {
			return new ArrayList<SqRoad>();
		}
		List<SqRoad> roads = SqRoad.getRoadsByCity(this.db, this.cityId, needle, limit);
		roads = dropDuplicates(roads);
		return roads;
	}

	private List<SqRoad> dropDuplicates(List<SqRoad> roads) {
		List<SqRoad> filtered = new ArrayList<SqRoad>();
		Set<String> names = new HashSet<String>();
		for (SqRoad road : roads) {
			String name = road.getName();
			if (!names.contains(name)) {
				filtered.add(road);
				names.add(name);
			}
		}
		return filtered;
	}

}
