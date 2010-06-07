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

import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Simple file browser to select a .map file from the file system.
 */
public class FileBrowser implements AdapterView.OnItemClickListener,
		AdapterView.OnItemSelectedListener {
	private AdvancedMapViewer advancedMapViewer;
	private File currentDirectory;
	private File[] files;
	private File[] filesWithParentFolder;
	private GridView gridView;
	private TextView lastSelected;

	/**
	 * Constructs a new file browser.
	 * 
	 * @param advancedMapViewer
	 *            the AdvancedMapViewer client
	 * @param currentDirectory
	 *            the path of the starting directory
	 * @param gridView
	 *            the grid view for displaying the icons
	 */
	public FileBrowser(AdvancedMapViewer advancedMapViewer, String currentDirectory,
			GridView gridView) {
		this.advancedMapViewer = advancedMapViewer;
		this.currentDirectory = new File(currentDirectory);
		this.gridView = gridView;
		this.gridView.setOnItemClickListener(this);
		this.gridView.setOnItemSelectedListener(this);
		this.gridView.setAdapter(new FileBrowserIconAdapter(this.files, this.currentDirectory,
				this.advancedMapViewer));
	}

	/**
	 * Browse to the current directory.
	 */
	public synchronized void browseToCurrentDirectory() {
		this.advancedMapViewer.setTitle(this.currentDirectory.getAbsolutePath());

		// read files and subfolders in the current folder
		this.files = this.currentDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					// accept all readable folders
					return pathname.canRead();
				}
				// accept all readable files with the correct extension
				return pathname.canRead() && pathname.getName().endsWith(".map");
			}
		});

		// order all files by type and alphabetically by name
		Arrays.sort(this.files, new Comparator<File>() {
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
		});

		// if a parent directory exists, add it at the first position
		if (this.currentDirectory.getParentFile() != null) {
			this.filesWithParentFolder = new File[this.files.length + 1];
			this.filesWithParentFolder[0] = this.currentDirectory.getParentFile();
			System.arraycopy(this.files, 0, this.filesWithParentFolder, 1, this.files.length);
			this.files = this.filesWithParentFolder;
		}

		this.gridView.setAdapter(new FileBrowserIconAdapter(this.files, this.currentDirectory,
				this.advancedMapViewer));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		File file = this.files[(int) id];
		if (file.isDirectory()) {
			browseToDirectory(file);
		} else {
			this.advancedMapViewer.onMapFileSelected(file.getAbsolutePath());
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> grid, View icon, int arg2, long index) {
		if (this.lastSelected != null) {
			this.lastSelected.setEllipsize(TextUtils.TruncateAt.END);
		}
		if (icon != null) {
			if (icon instanceof TextView) {
				this.lastSelected = (TextView) icon;
				this.lastSelected.setEllipsize(TextUtils.TruncateAt.MARQUEE);
			} else {
				this.lastSelected = null;
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> grid) {
		if (this.lastSelected != null) {
			this.lastSelected.setEllipsize(TextUtils.TruncateAt.END);
			this.lastSelected = null;
		}
	}

	/**
	 * Browse to the specified directory.
	 * 
	 * @param directory
	 *            the new directory
	 */
	private synchronized void browseToDirectory(File directory) {
		this.currentDirectory = directory;
		browseToCurrentDirectory();
	}
}