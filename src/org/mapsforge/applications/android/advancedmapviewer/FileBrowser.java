/*
 * Copyright 2010, 2011 mapsforge.org
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
 * Simple file browser activity to select a file.
 */
public class FileBrowser extends Activity implements AdapterView.OnItemClickListener {
	private static final String DEFAULT_DIRECTORY = "/";
	private static final int DIALOG_FILE_INVALID = 0;
	private static final int DIALOG_FILE_SELECT = 1;
	private static FileFilter fileDisplayFilter;
	private static FileFilter fileSelectFilter;
	private static final String PREFERENCES_FILE = "FileBrowser";

	/**
	 * Sets the file display filter. This filter is used to determine which files and subfolders
	 * of directories will be displayed. If set to null, all files and subfolders are shown.
	 * 
	 * @param fileDisplayFilter
	 *            the file display filter (may be null).
	 */
	static void setFileDisplayFilter(FileFilter fileDisplayFilter) {
		FileBrowser.fileDisplayFilter = fileDisplayFilter;
	}

	/**
	 * Sets the file select filter. This filter is used when the user selects a file to
	 * determine if it is valid. If set to null, all files are considered as valid.
	 * 
	 * @param fileSelectFilter
	 *            the file selection filter (may be null).
	 */
	static void setFileSelectFilter(FileFilter fileSelectFilter) {
		FileBrowser.fileSelectFilter = fileSelectFilter;
	}

	private File currentDirectory;
	private FileBrowserIconAdapter fileBrowserIconAdapter;
	private Comparator<File> fileComparator;
	private File[] files;
	private File[] filesWithParentFolder;
	private GridView gridView;
	private File selectedFile;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		this.selectedFile = this.files[(int) id];
		if (this.selectedFile.isDirectory()) {
			this.currentDirectory = this.selectedFile;
			browseToCurrentDirectory();
		} else if (fileSelectFilter == null || fileSelectFilter.accept(this.selectedFile)) {
			setResult(RESULT_OK, new Intent().putExtra("selectedFile", this.selectedFile
					.getAbsolutePath()));
			finish();
		} else {
			showDialog(DIALOG_FILE_INVALID);
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

		// read all files and subfolders from the current directory
		if (fileDisplayFilter == null) {
			this.files = this.currentDirectory.listFiles();
		} else {
			this.files = this.currentDirectory.listFiles(fileDisplayFilter);
		}

		if (this.files == null) {
			// no files could be read
			this.files = new File[0];
		} else {
			// order all files
			Arrays.sort(this.files, this.fileComparator);
		}

		// if a parent directory exists, add it at the first position
		if (this.currentDirectory.getParentFile() != null) {
			this.filesWithParentFolder = new File[this.files.length + 1];
			this.filesWithParentFolder[0] = this.currentDirectory.getParentFile();
			System.arraycopy(this.files, 0, this.filesWithParentFolder, 1, this.files.length);
			this.files = this.filesWithParentFolder;
			this.fileBrowserIconAdapter.setFiles(this.files, true);
		} else {
			this.fileBrowserIconAdapter.setFiles(this.files, false);
		}
		this.fileBrowserIconAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_browser);
		this.fileBrowserIconAdapter = new FileBrowserIconAdapter(this);
		this.gridView = (GridView) findViewById(R.id.fileBrowserView);
		this.gridView.setOnItemClickListener(this);
		this.gridView.setAdapter(this.fileBrowserIconAdapter);

		// order all files by type and alphabetically by name
		this.fileComparator = new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				if (file1.isDirectory() && !file2.isDirectory()) {
					return -1;
				} else if (!file1.isDirectory() && file2.isDirectory()) {
					return 1;
				} else {
					return file1.getName().compareToIgnoreCase(file2.getName());
				}
			}
		};

		if (savedInstanceState == null) {
			// first start of this instance
			showDialog(DIALOG_FILE_SELECT);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
			case DIALOG_FILE_INVALID:
				builder.setIcon(android.R.drawable.ic_menu_info_details);
				builder.setTitle(getString(R.string.error));
				builder.setMessage(getString(R.string.map_file_invalid));
				builder.setPositiveButton(getString(R.string.ok), null);
				return builder.create();
			case DIALOG_FILE_SELECT:
				builder.setMessage(getString(R.string.map_file_select));
				builder.setPositiveButton(getString(R.string.ok), null);
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
		if (!this.currentDirectory.exists() || !this.currentDirectory.canRead()) {
			this.currentDirectory = new File(DEFAULT_DIRECTORY);
		}
		browseToCurrentDirectory();
	}
}