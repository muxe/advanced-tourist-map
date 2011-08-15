/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.WindowManager;

/**
 * Activity to edit the application preferences.
 */
public class EditPreferences extends PreferenceActivity {

	private static final int SELECT_MAP_FILE = 0;

	SharedPreferences appPreferences;
	Preference fileChooser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity thisActivity = this;
		this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		addPreferencesFromResource(R.xml.preferences);

		this.fileChooser = findPreference("baseBundlePath");
		this.fileChooser.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {

				// set the FileDisplayFilter
				FilePicker.setFileDisplayFilter(new FileFilter() {
					@Override
					public boolean accept(File file) {
						// accept only readable files
						if (file.canRead()) {
							if (file.isDirectory()) {
								// accept all directories
								return true;
							} else if (file.isFile() && file.getName().endsWith(".xml")) {
								// accept all files with a ".map" extension
								return true;
							}
						}
						return false;
					}
				});

				// start the FilePicker (in directory return mode)
				startActivityForResult(
						new Intent(thisActivity, FilePicker.class).putExtra("directory", true),
						SELECT_MAP_FILE);

				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// check if the full screen mode should be activated
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("fullscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}

		this.fileChooser.setSummary(this.appPreferences.getString("baseBundlePath",
				getString(R.string.preferences_no_path_selected)));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_MAP_FILE && data != null) {
			if (resultCode == RESULT_OK) {
				String filename = data.getStringExtra("selectedFile");
				File file = new File(filename);
				if (file.isFile()) {
					filename = file.getParent();
				}
				Editor editor = this.appPreferences.edit();

				editor.putString("baseBundlePath", filename);
				editor.remove("bundlePath");
				editor.commit();
				// unset in Application
				// TODO: consider using OnSharedPreferenceChangeListener:
				// http://developer.android.com/reference/android/content/SharedPreferences.OnSharedPreferenceChangeListener.html
				AdvancedMapViewerApplication amvapp = (AdvancedMapViewerApplication) getApplication();
				amvapp.resetBaseBundlePath();
				// amvapp.resetCurrentMapBundle();
				this.fileChooser.setSummary(filename);
			}

		}

	}

}