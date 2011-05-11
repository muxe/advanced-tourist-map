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

import java.util.List;

import model.address.android.SqRoad;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RoadListAdapter extends BaseAdapter {

	private SQLiteDatabase db;
	private List<SqRoad> roads;

	public RoadListAdapter(SQLiteDatabase db, List<SqRoad> roads) {
		this.db = db;
		this.roads = roads;
	}

	@Override
	public int getCount() {
		return this.roads.size();
	}

	@Override
	public Object getItem(int i) {
		return this.roads.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int arg0, View view, ViewGroup parent) {
		TextView textView = new TextView(parent.getContext());
		textView.setText(this.roads.get(arg0).toVerboseString(this.db, false));
		return textView;
	}

}
