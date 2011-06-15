package org.mapsforge.applications.android.advancedmapviewer.routing;

import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RoutingFileManager {
	private static final String TAG = RoutingFileManager.class.getSimpleName();

	static final int VERSION = 1;
	static final String DATABASE = "routingFiles.db";
	static final String TABLE = "routingfiles";

	public static final String C_NAME = "name";
	public static final String C_PATH = "path";

	class DBHelper extends SQLiteOpenHelper {
		public DBHelper(Context context) {
			super(context, DATABASE, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table " + TABLE + " (" + C_NAME + " text, " + C_PATH
					+ " text UNIQUE)");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO implement upgrades, now just dropping oO
			db.execSQL("drop table " + TABLE);
			this.onCreate(db);
		}
	}

	private DBHelper dbHelper;

	public RoutingFileManager(Context context) {
		this.dbHelper = new DBHelper(context);
	}

	public RoutingFile[] getAllRoutingFiles() {
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, null, null, null, null, null, null);
		LinkedList<RoutingFile> list = new LinkedList<RoutingFile>();
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(cursor
						.getColumnIndexOrThrow(RoutingFileManager.C_NAME));
				String path = cursor.getString(cursor
						.getColumnIndexOrThrow(RoutingFileManager.C_PATH));
				list.add(new RoutingFile(name, path));
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		RoutingFile[] result = list.toArray(new RoutingFile[0]);
		return result;
	}

	public void close() {
		this.dbHelper.close();
	}

	public boolean store(RoutingFile routingFile) {
		ContentValues values = new ContentValues();
		values.put(RoutingFileManager.C_NAME, routingFile.name);
		values.put(RoutingFileManager.C_PATH, routingFile.path);

		// TODO: get reason when fail
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.insertOrThrow(TABLE, null, values);
		} catch (SQLException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			db.close();
		}
		return true;
	}

	public boolean delete(RoutingFile routingfile) {
		Log.d(TAG, "delete routingFile: " + routingfile.name);
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			int deleted = db.delete(TABLE, C_PATH + "=?", new String[] { routingfile.path });
			return deleted > 0;
		} catch (SQLException e) {
			Log.d(TAG, e.getMessage());
		} finally {
			db.close();
		}
		return false;
	}
}