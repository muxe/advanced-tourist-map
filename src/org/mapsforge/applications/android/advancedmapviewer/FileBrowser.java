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

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.mapsforge.android.maps.MapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * Simple file browser activity to select a map file from the file system.
 */
public class FileBrowser extends Activity implements AdapterView.OnItemClickListener {
	private static final String DEFAULT_DIRECTORY = "/";
	private static final int DIALOG_MAP_FILE_INVALID = 0;
	private static final int DIALOG_MAP_FILE_SELECT = 1;
	private static final String PREFERENCES_FILE = "FileBrowser";
	private File currentDirectory;
	private FileBrowserIconAdapter fileBrowserIconAdapter;
	private Comparator<File> fileComparator;
	private FileFilter fileFilter;
	private File[] files;
	private File[] filesWithParentFolder;
	private GridView gridView;
	private File selectedFile;

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		this.selectedFile = this.files[(int) id];
		if (this.selectedFile.isDirectory()) {
			this.currentDirectory = this.selectedFile;
			browseToCurrentDirectory();
		} else if (MapView.isValidMapFile(this.selectedFile.getAbsolutePath())) {
			setResult(RESULT_OK, new Intent().putExtra("mapFile", this.selectedFile
					.getAbsolutePath()));
			finish();
		} else {
			showDialog(DIALOG_MAP_FILE_INVALID);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// quit the application
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}
		return false;
	}

	/**
	 * Browse to the current directory.
	 */
	private void browseToCurrentDirectory() {
		setTitle(this.currentDirectory.getAbsolutePath());

		// read and filter files and subfolders in the current folder
		this.files = this.currentDirectory.listFiles(this.fileFilter);

		// order all files by type and alphabetically by name
		Arrays.sort(this.files, this.fileComparator);

		// if a parent directory exists, add it at the first position
		if (this.currentDirectory.getParentFile() != null) {
			this.filesWithParentFolder = new File[this.files.length + 1];
			this.filesWithParentFolder[0] = this.currentDirectory.getParentFile();
			System.arraycopy(this.files, 0, this.filesWithParentFolder, 1, this.files.length);
			this.files = this.filesWithParentFolder;
			this.fileBrowserIconAdapter.updateFiles(this.files, true);
		} else {
			this.fileBrowserIconAdapter.updateFiles(this.files, false);
		}
		this.fileBrowserIconAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser);
		this.fileBrowserIconAdapter = new FileBrowserIconAdapter(this);
		this.gridView = (GridView) findViewById(R.id.fileBrowserView);
		this.gridView.setOnItemClickListener(this);
		this.gridView.setAdapter(this.fileBrowserIconAdapter);

		this.fileFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					// accept all readable folders
					return pathname.canRead();
				}
				// accept all readable files with the correct extension
				return pathname.canRead() && pathname.getName().endsWith(".map");
			}
		};

		this.fileComparator = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && !o2.isDirectory()) {
					return -1;
				} else if (!o1.isDirectory() && o2.isDirectory()) {
					return 1;
				} else {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		};

		if (savedInstanceState == null) {
			// first start of this instance
			showDialog(DIALOG_MAP_FILE_SELECT);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_MAP_FILE_INVALID:
				builder.setMessage(getString(R.string.map_file_invalid)).setTitle(
						getString(R.string.error)).setPositiveButton(getString(R.string.ok),
						null);
				return builder.create();
			case DIALOG_MAP_FILE_SELECT:
				builder.setMessage(getString(R.string.map_file_select)).setPositiveButton(
						getString(R.string.ok), null);
				return builder.create();
			default:
				return null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// save the current directory
		Editor editor = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
		editor.clear();
		if (this.currentDirectory != null) {
			editor.putString("currentDirectory", this.currentDirectory.getAbsolutePath());
		}
		editor.commit();
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

		// restore the current directory
		SharedPreferences preferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
		this.currentDirectory = new File(preferences.getString("currentDirectory",
				DEFAULT_DIRECTORY));
		if (!this.currentDirectory.exists()) {
			this.currentDirectory = new File(DEFAULT_DIRECTORY);
		}
		browseToCurrentDirectory();
	}
}