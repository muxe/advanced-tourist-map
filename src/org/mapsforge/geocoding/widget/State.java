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

import java.io.Serializable;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class State implements Serializable {

	private static final long serialVersionUID = 1909742759063153887L;

	public static final int VIEW_INPUT = 0;
	public static final int VIEW_RESULTS = 1;

	public int view = VIEW_INPUT;

	public String queryCity = null;
	public String queryRoad = null;

	public void serializeToEditor(Editor editor) {
		editor.clear();
		editor.putInt("view", this.view);
		editor.putString("queryCity", this.queryCity);
		editor.putString("queryRoad", this.queryRoad);
		editor.commit();
	}

	public void deserializeFromPreferences(SharedPreferences preferences) {
		this.view = preferences.getInt("view", VIEW_INPUT);
		this.queryCity = preferences.getString("queryCity", "Foo");
		this.queryRoad = preferences.getString("queryRoad", "Bar");
	}
}
