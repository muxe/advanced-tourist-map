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

import org.mapsforge.geocoding.Unchecked;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public abstract class SqliteCompletionAdapter<T> extends BaseAdapter implements Filterable {

	private static final String ANDROID_LOG_TAG = "geocoding";

	private InternalFilter filter = null;
	private List<T> currentResults = null;

	public abstract String getString(T element);

	public abstract List<T> getResults(String needle);

	// setter for filter
	void setCurrentResults(List<T> results) {
		this.currentResults = results;
	}

	@Override
	public int getCount() {
		Log.d(ANDROID_LOG_TAG, "getCount");
		if (this.currentResults == null) {
			return 0;
		}
		return this.currentResults.size();
	}

	@Override
	public Object getItem(int i) {
		Log.d(ANDROID_LOG_TAG, "getItem " + i);
		if (this.currentResults == null) {
			return null;
		}
		return getString(this.currentResults.get(i));
	}

	public T getItemDataObject(int i) {
		if (this.currentResults == null) {
			return null;
		}
		return this.currentResults.get(i);
	}

	@Override
	public long getItemId(int i) {
		Log.d(ANDROID_LOG_TAG, "getItemId " + i);
		return 0;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		String name = getString(this.currentResults.get(pos));
		Log.d(ANDROID_LOG_TAG, "getView " + name);
		TextView textView = new TextView(parent.getContext());
		textView.setPadding(0, 10, 0, 10);
		textView.setText(name);
		return textView;
	}

	/*
	 * Filterable interface
	 */

	@Override
	public Filter getFilter() {
		Log.d(ANDROID_LOG_TAG, "getFilter");

		if (this.filter == null) {
			this.filter = new InternalFilter();
		}

		return this.filter;
	}

	class InternalFilter extends Filter {

		@Override
		protected android.widget.Filter.FilterResults performFiltering(CharSequence prefix) {
			// this gets called by a worker thread!!! take care.
			Log.d(ANDROID_LOG_TAG, "perform " + prefix);

			String needle = prefix == null ? null : prefix.toString();
			List<T> results = getResults(needle);

			FilterResults filterResults = new FilterResults();

			filterResults.values = results;
			filterResults.count = results.size();

			Log.d(ANDROID_LOG_TAG, "perform finish");
			return filterResults;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults filterResults) {
			Log.d(ANDROID_LOG_TAG, "publish");

			List<T> results = Unchecked.cast(filterResults.values);
			SqliteCompletionAdapter.this.setCurrentResults(results);

			if (results.size() == 0) {
				notifyDataSetInvalidated();
			} else {
				notifyDataSetChanged();
			}
		}
	}
}
