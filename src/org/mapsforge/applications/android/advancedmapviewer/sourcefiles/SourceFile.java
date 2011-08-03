package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

import java.io.File;
import java.util.Date;

import android.util.Log;

/**
 * Abstract class to model a SourceFile
 * 
 * @author Max Dörfler
 */
public abstract class SourceFile {

	// TODO: needed?
	public enum SourceFileType {
		MAP, ROUTING, ADDRESS, POI
	}

	private String filename;
	private String path;
	private String md5;
	private String description;
	private long filesize;
	private Date created;

	public SourceFile() {
		this.description = "";
	}

	public abstract SourceFileType getType();

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMd5() {
		return this.md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public long getFilesize() {
		return this.filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public Date getCreated() {
		return this.created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public String getRelativePath() {
		return this.path + File.separator + this.filename;
	}

	public boolean isValid(String basepath, boolean checkMD5) {
		// check if all fields are set and not null
		if (this.filename == null || this.created == null || this.description == null
				|| this.filesize <= 0 || this.md5 == null || this.path == null) {
			Log.d("FileManager", "Some field(s) wasn't/weren't set");
			return false;
		}

		// check if file exists
		String fullpath = basepath + File.separator + this.getRelativePath();
		Log.d("FileManager", "Check if " + fullpath + " is valid");
		try {
			File file = new File(fullpath);
			if (!file.isFile()) {
				Log.d("FileManager", "File " + fullpath + " is no file");
				return false;
			}
		} catch (Exception e) {
			Log.d("FileManager", "File " + fullpath + " doesn't exist");
			return false;
		}

		if (checkMD5) {
			return checkMD5();
		}
		return true;
	}

	private boolean checkMD5() {
		// TODO: implement
		return true;
	}

}
