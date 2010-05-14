/*
 * Copyright 2010 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.advancedmapviewer;

import org.mapsforge.applications.android.advancedmapviewer.R;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Preferences class for adjusting the cache size.
 */
public class CacheSizePreference extends SeekBarPreference {
	/**
	 * Construct a new cache size preference seek bar.
	 * 
	 * @param context
	 *            the context of the cache size preferences activity.
	 * @param attrs
	 *            A set of attributes (currently ignored).
	 */
	public CacheSizePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// define the text message
		this.message = getContext().getString(R.string.preferences_cache_size_desc);

		// define the current and maximum value of the seek bar
		this.progress = this.preferencesDefault.getInt(this.getKey(),
				AdvancedMapViewer.FILE_CACHE_SIZE_DEFAULT);
		this.max = AdvancedMapViewer.FILE_CACHE_SIZE_MAX;

		// define the progress increment via the arrow keys
		this.increment = 1;
	}
}