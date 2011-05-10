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
