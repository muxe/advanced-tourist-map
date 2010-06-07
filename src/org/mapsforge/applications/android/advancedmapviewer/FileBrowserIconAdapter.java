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

import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

class FileBrowserIconAdapter extends BaseAdapter {
	private AdvancedMapViewer advancedMapViewer;
	private File currentDirectory;
	private File[] files;

	FileBrowserIconAdapter(File[] files, File currentDirectory,
			AdvancedMapViewer advancedMapViewer) {
		this.files = files;
		this.currentDirectory = currentDirectory;
		this.advancedMapViewer = advancedMapViewer;
	}

	@Override
	public int getCount() {
		if (this.files == null) {
			return 0;
		}
		return this.files.length;
	}

	@Override
	public Object getItem(int index) {
		return this.files[index];
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View convertView, ViewGroup parent) {
		File currentFile = this.files[index];
		TextView textView;
		int fileSymbol;
		String fileName;
		if (index == 0
				&& (currentFile.getParentFile() == null || currentFile.getParentFile()
						.getAbsolutePath().compareTo(this.currentDirectory.getAbsolutePath()) != 0)) {
			// the parent directory
			fileSymbol = R.drawable.ic_menu_back;
			fileName = "..";
		} else if (currentFile.isDirectory()) {
			// another directory
			fileSymbol = R.drawable.ic_menu_archive;
			fileName = currentFile.getName();
		} else {
			// a file
			fileSymbol = R.drawable.ic_menu_mapmode;
			fileName = currentFile.getName();
		}

		if (convertView == null || !(convertView instanceof TextView)) {
			// create a new view object
			textView = new TextView(this.advancedMapViewer);
			textView.setEllipsize(TextUtils.TruncateAt.END);
			textView.setSingleLine();
			textView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT));
			textView.setGravity(Gravity.CENTER_HORIZONTAL);
			textView.setPadding(5, 5, 5, 5);
		} else {
			// recycle the old view
			textView = (TextView) convertView;
		}

		// set file icon and name
		textView.setCompoundDrawablesWithIntrinsicBounds(0, fileSymbol, 0, 0);
		textView.setText(fileName);
		return textView;
	}
}