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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This abstract class provides all code for a seek bar preference. Deriving classes only need
 * to set the current and maximum value of the seek bar. An optional text message is also
 * supported.
 */
abstract class SeekBarPreference extends DialogPreference {
	private Editor editor;
	private LinearLayout linearLayout;
	private SeekBar seekBar;
	private TextView textView;

	/**
	 * How much the value should increase when the seek bar is moved.
	 */
	int increment;

	/**
	 * The maximum value of the seek bar.
	 */
	int max;

	/**
	 * Optional text message to display on top of the seek bar.
	 */
	String message;

	/**
	 * The SharedPreferences instance that is used.
	 */
	final SharedPreferences preferencesDefault;

	/**
	 * The current value of the seek bar.
	 */
	int progress;

	/**
	 * Create a new seek bar preference.
	 * 
	 * @param context
	 *            the context of the seek bar preferences activity.
	 * @param attrs
	 *            A set of attributes (currently ignored).
	 */
	SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.preferencesDefault = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// check if the "OK" button was pressed
		if (which == DialogInterface.BUTTON_POSITIVE) {
			// check if the value of the seek bar differs from the initial value
			if (this.progress != this.seekBar.getProgress()) {
				// get the value of the seek bar and save it in the preferences
				this.progress = this.seekBar.getProgress();
				this.editor = this.preferencesDefault.edit();
				this.editor.putInt(this.getKey(), this.progress);
				this.editor.commit();
			}
		}
	}

	@Override
	protected View onCreateDialogView() {
		// create a layout for the optional text message and the seek bar
		this.linearLayout = new LinearLayout(getContext());
		this.linearLayout.setOrientation(LinearLayout.VERTICAL);
		this.linearLayout.setPadding(20, 10, 20, 10);

		// check if a text message should appear above the seek bar
		if (this.message != null) {
			// create a text view for the text message
			this.textView = new TextView(getContext());
			this.textView.setText(this.message);
			this.textView.setPadding(0, 0, 0, 10);

			// add the text view to the layout
			this.linearLayout.addView(this.textView);
		}

		// create the seek bar and set the maximum and current value
		this.seekBar = new SeekBar(getContext());
		this.seekBar.setMax(this.max);
		this.seekBar.setProgress(Math.min(this.progress, this.max));
		this.seekBar.setKeyProgressIncrement(this.increment);

		// add the seek bar to the layout
		this.linearLayout.addView(this.seekBar);
		return this.linearLayout;
	}
}