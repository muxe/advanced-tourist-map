package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mapsforge.applications.android.advancedmapviewer.BaseActivity;
import org.mapsforge.applications.android.advancedmapviewer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
// TODO: if first start and no baseDirectory is set, what do?
public class FileManagerActivity extends BaseActivity {

	private static final int DIALOG_BUNDLE_INFO = 0;

	private ListView listView;
	private TextView emptyListText;
	private SimpleAdapter mapBundleAdapter;
	ArrayList<MapBundle> installedBundles;
	int currentlySelectedBundlePosition;

	// MapBundle currentlySelectedBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_file_manager);

		this.listView = (ListView) findViewById(R.id.file_manager_list_view);
		this.emptyListText = (TextView) findViewById(R.id.file_manager_empty_text);

		String[] from = new String[] { "name", "description" };
		int[] to = new int[] { R.id.installed_bundle_row_name,
				R.id.installed_bundle_row_description };

		this.installedBundles = this.advancedMapViewer.getFileManager().getInstalledBundles();

		if (this.installedBundles.size() <= 0) {
			this.emptyListText.setVisibility(View.VISIBLE);
			this.listView.setVisibility(View.GONE);
		}

		List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
		for (MapBundle mb : this.installedBundles) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", mb.getName());
			map.put("description", mb.getMapFile().getDescription());
			fillMaps.add(map);
		}

		this.mapBundleAdapter = new SimpleAdapter(this.advancedMapViewer, fillMaps,
				R.layout.installed_bundle_row, from, to);
		this.listView.setAdapter(this.mapBundleAdapter);
		this.listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// FileManagerActivity.this.currentlySelectedBundle =
				// FileManagerActivity.this.installedBundles
				// .get(position);
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

			dialog.setTitle(currentlySelectedBundle.getName());

			TextView routingAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_routing_avail);
			if (currentlySelectedBundle.getRoutingFiles().size() > 0) {
				routingAvail.setText(getString(R.string.routingfile_avaiability) + " "
						+ getString(R.string.enabled));
			} else {
				routingAvail.setText(getString(R.string.routingfile_avaiability) + " "
						+ getString(R.string.disabled));
			}

			TextView addressAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_address_avail);
			if (currentlySelectedBundle.getAddressFile() != null) {
				addressAvail.setText(getString(R.string.addressfile_avaiability) + " "
						+ getString(R.string.enabled));
			} else {
				addressAvail.setText(getString(R.string.addressfile_avaiability) + " "
						+ getString(R.string.disabled));
			}

			TextView poiAvail = (TextView) dialog
					.findViewById(R.id.installed_bundle_info_poi_avail);
			if (currentlySelectedBundle.getPoiFile() != null) {
				poiAvail.setText(getString(R.string.poifile_avaiability) + " "
						+ getString(R.string.enabled));
			} else {
				poiAvail.setText(getString(R.string.poifile_avaiability) + " "
						+ getString(R.string.disabled));
			}
		}
	}

	// TODO: show list with installed bundles, possibilities to select one and make it default,
	// implement the isValid methods, get rid of the old routingmanager, think of way to disable
	// functions if no file is installed

}
