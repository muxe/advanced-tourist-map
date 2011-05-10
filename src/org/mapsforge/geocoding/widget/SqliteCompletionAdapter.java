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

	private static final String tag = "geocoding";

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
		Log.i(tag, "getCount");
		if (this.currentResults == null) {
			return 0;
		}
		return this.currentResults.size();
	}

	@Override
	public Object getItem(int i) {
		Log.i(tag, "getItem " + i);
		if (this.currentResults == null) {
			return null;
		}
		return getString(this.currentResults.get(i));
	}

	@Override
	public long getItemId(int i) {
		Log.i(tag, "getItemId " + i);
		return 0;
	}

	@Override
	public View getView(int pos, View view, ViewGroup parent) {

		String name = getString(this.currentResults.get(pos));
		Log.i(tag, "getView " + name);
		TextView textView = new TextView(parent.getContext());
		textView.setText(name);
		return textView;
		// return view;
	}

	/*
	 * Filterable interface
	 */

	@Override
	public Filter getFilter() {
		Log.i(tag, "getFilter");

		if (this.filter == null) {
			this.filter = new InternalFilter();
		}

		return this.filter;
	}

	class InternalFilter extends Filter {

		@Override
		protected android.widget.Filter.FilterResults performFiltering(CharSequence prefix) {
			// this gets called by a worker thread!!! take care.
			Log.i(tag, "perform " + prefix);

			String needle = prefix == null ? null : prefix.toString();
			List<T> results = getResults(needle);

			FilterResults filterResults = new FilterResults();

			filterResults.values = results;
			filterResults.count = results.size();

			Log.i(tag, "perform finish");
			return filterResults;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults filterResults) {
			Log.i(tag, "publish");

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
