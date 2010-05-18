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

import org.mapsforge.android.map.MapView;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Preferences class for adjusting the cache size.
 */
public class CacheSizePreference extends SeekBarPreference {
	private final StringBuilder stringBuilder;

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
		this.messageText = getContext().getString(R.string.preferences_cache_size_desc);

		// define the current and maximum value of the seek bar
		this.seekBarCurrentValue = this.preferencesDefault.getInt(this.getKey(),
				AdvancedMapViewer.FILE_CACHE_SIZE_DEFAULT);
		this.max = AdvancedMapViewer.FILE_CACHE_SIZE_MAX;
		this.stringBuilder = new StringBuilder(32);
	}

	private void appendHumanReadableSize(int fileSize) {
		if (fileSize < 1000000) {
			this.stringBuilder.append(fileSize / 1000);
			this.stringBuilder.append(getContext().getString(R.string.unit_symbol_kilobyte));
		} else {
			// round to first decimal place
			this.stringBuilder.append((fileSize / 100000) / 10d);
			this.stringBuilder.append(getContext().getString(R.string.unit_symbol_megabyte));
		}
	}

	@Override
	String getCurrentValueText(int progress) {
		this.stringBuilder.delete(0, Integer.MAX_VALUE);
		appendHumanReadableSize(MapView.getTileSizeInBytes() * progress);
		this.stringBuilder.append(getContext().getString(R.string.out_of));
		appendHumanReadableSize(MapView.getTileSizeInBytes()
				* AdvancedMapViewer.FILE_CACHE_SIZE_MAX);
		return this.stringBuilder.toString();
	}
}