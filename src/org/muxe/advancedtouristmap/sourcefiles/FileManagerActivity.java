package org.muxe.advancedtouristmap.sourcefiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mapsforge.applications.android.advancedmapviewer.R;
import org.muxe.advancedtouristmap.BaseActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Main Activity to manage the needed source Files. Multiple Files which cover the same area are
 * bundled by an xml file
 * 
 * @author Max Dörfler <doerfler@inf.fu-berlin.de>
 * 
 */
public class FileManagerActivity extends BaseActivity {

	private static final int DIALOG_BUNDLE_INFO = 0;

	private static final String NAMEKEY = "name";
	private static final String DESCKEY = "description";
	private static final String IMAGEKEY = "image";

	private ListView listView;
	private TextView emptyListText;
	private SimpleAdapter mapBundleAdapter;
	ArrayList<MapBundle> installedBundles;
	int currentlySelectedBundlePosition;
	private ImageButton refreshButton;
	ProgressDialog progressDialog;

	// MapBundle currentlySelectedBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.advancedMapViewer.setViewWithHelp(this, R.layout.activity_file_manager);

		this.listView = (ListView) findViewById(R.id.file_manager_list_view);
		this.emptyListText = (TextView) findViewById(R.id.file_manager_empty_text);
		this.refreshButton = (ImageButton) findViewById(R.id.button_file_manager_refresh);
		this.refreshButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// FileManagerActivity.this.advancedMapViewer.resetBaseBundlePath();
				// TODO: async, because this may take a while
				new RefreshListAsync().execute();
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO: what happens if amv gets started, no files are found, files are then added and
		// amv gets resumed?!
		super.onResume();
		this.buildList();
	}

	void buildList() {
		this.installedBundles = this.advancedMapViewer.getFileManager().getInstalledBundles();

		if (this.installedBundles.size() <= 0) {
			this.emptyListText.setVisibility(View.VISIBLE);
			this.listView.setVisibility(View.GONE);
		} else {
			this.emptyListText.setVisibility(View.GONE);
			this.listView.setVisibility(View.VISIBLE);
		}
		List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
		for (MapBundle mb : this.installedBundles) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(NAMEKEY, mb.getName());
			map.put(DESCKEY, mb.getMapFile().getDescription());
			if (!mb.equals(this.advancedMapViewer.getCurrentMapBundle())) {
				map.put(IMAGEKEY, R.drawable.globe);
			} else {
				map.put(IMAGEKEY, R.drawable.globe_checked);
			}
			fillMaps.add(map);
		}
		// just to check layout with a full list
		// for (int i = 0; i <= 50; i++) {
		// HashMap<String, String> map = new HashMap<String, String>();
		// map.put("name", "name " + i);
		// map.put("description", "description: " + i);
		// fillMaps.add(map);
		// }

		String[] from = new String[] { NAMEKEY, DESCKEY, IMAGEKEY };
		int[] to = new int[] { R.id.installed_bundle_row_name,
				R.id.installed_bundle_row_description, R.id.installed_bundle_row_image };

		this.mapBundleAdapter = new SimpleAdapter(this.advancedMapViewer, fillMaps,
				R.layout.installed_bundle_row, from, to);
		this.listView.setAdapter(this.mapBundleAdapter);
		this.listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FileManagerActivity.this.currentlySelectedBundlePosition = position;
				showDialog(DIALOG_BUNDLE_INFO);
			}
		});
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (id == DIALOG_BUNDLE_INFO) {
			LayoutInflater factory = LayoutInflater.from(this);
			final View view = factory.inflate(R.layout.installed_bundle_info_dialog, null);
			builder.setView(view);
			// only called, so it can be set via onPrepareDialog
			builder.setTitle("placeholder");

			builder.setPositiveButton(R.string.yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// make it the new default bundle
					MapBundle currentlySelectedBundle = FileManagerActivity.this.installedBundles
							.get(FileManagerActivity.this.currentlySelectedBundlePosition);

					Editor editor = FileManagerActivity.this.advancedMapViewer.prefs.edit();
					editor.putString("bundlePath", currentlySelectedBundle.getFilepathXml());
					FileManagerActivity.this.advancedMapViewer.resetCurrentMapBundle();
					FileManagerActivity.this.advancedMapViewer.resetRouter();

					editor.commit();
					FileManagerActivity.this.buildList();
				}
			});

			builder.setNeutralButton(R.string.no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// do nothing
				}
			});

			return builder.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		if (id == DIALOG_BUNDLE_INFO) {
			// workaround for deprecated api, instead of bundle param
			MapBundle currentlySelectedBundle = this.installedBundles
					.get(this.currentlySelectedBundlePosition);

			// TODO: externalize
			dialog.setTitle("Use " + currentlySelectedBundle.getName() + "?");

			TextView routingAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_routing_avail);
			if (currentlySelectedBundle.getRoutingFiles().size() > 0) {
				routingAvail.setText(getString(R.string.enabled));
				routingAvail.setTextColor(Color.GREEN);
			} else {
				routingAvail.setText(getString(R.string.disabled));
				routingAvail.setTextColor(Color.RED);
			}

			TextView addressAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_address_avail);
			if (currentlySelectedBundle.getAddressFile() != null) {
				addressAvail.setText(getString(R.string.enabled));
				addressAvail.setTextColor(Color.GREEN);
			} else {
				addressAvail.setText(getString(R.string.disabled));
				addressAvail.setTextColor(Color.RED);
			}

			TextView poiAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_poi_avail);
			if (currentlySelectedBundle.getPoiFile() != null) {
				poiAvail.setText(getString(R.string.enabled));
				poiAvail.setTextColor(Color.GREEN);
			} else {
				poiAvail.setText(getString(R.string.disabled));
				poiAvail.setTextColor(Color.RED);
			}
		}
	}

	private class RefreshListAsync extends AsyncTask<Void, Void, Void> {

		public RefreshListAsync() {
			super();
		}

		@Override
		protected void onPreExecute() {
			FileManagerActivity.this.progressDialog = ProgressDialog.show(
					FileManagerActivity.this, "", "Loading. Please wait...", true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			FileManagerActivity.this.advancedMapViewer.getFileManager().rescan(
					FileManagerActivity.this.advancedMapViewer.getBaseBundlePath());
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// remove progress bar
			FileManagerActivity.this.buildList();
			FileManagerActivity.this.progressDialog.dismiss();
		}
	}
}
