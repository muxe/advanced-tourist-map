package org.mapsforge.applications.android.advancedmapviewer.routing;

import org.mapsforge.applications.android.advancedmapviewer.BaseActivity;
import org.mapsforge.applications.android.advancedmapviewer.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class RoutingFileSettings extends BaseActivity {
	private static final String TAG = RoutingFileSettings.class.getSimpleName();

	protected static final int CONTEXTMENU_MODIFY = 0;
	protected static final int CONTEXTMENU_DELETE = 1;

	protected static final int DIALOG_CONFIRM_DELETE = 0;

	private Button addRoutingFileButton;
	private ListView installedRoutingFilesList;
	private TextView emptyListText;

	private RoutingFile[] routingFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_routing_files);

		this.addRoutingFileButton = (Button) findViewById(R.id.manage_routing_files_button_add);
		this.installedRoutingFilesList = (ListView) findViewById(R.id.manage_routingfiles_list);
		this.emptyListText = (TextView) findViewById(R.id.manage_routing_files_empty_text);

		this.routingFiles = this.advancedMapViewer.getRoutingFiles();
		// this.routingFiles = new RoutingFile[0];

		// if there are no routing files, display help text
		if (this.routingFiles == null || this.routingFiles.length == 0) {
			this.installedRoutingFilesList.setVisibility(View.GONE);
			this.emptyListText.setVisibility(View.VISIBLE);
		}
		ArrayAdapter<RoutingFile> adapter = new ArrayAdapter<RoutingFile>(this,
				R.layout.installed_map_file_row, this.routingFiles);
		this.installedRoutingFilesList.setAdapter(adapter);
		// register a context menu for long click
		registerForContextMenu(this.installedRoutingFilesList);

		this.addRoutingFileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RoutingFileSettings.this.advancedMapViewer.getRoutingFileManager().store(
						new RoutingFile("Car", "/sdcard/car.HH"));
				RoutingFileSettings.this.advancedMapViewer.resetRoutingFiles();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.manage_routingfiles_list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(this.routingFiles[info.position].name);
			menu.add(Menu.NONE, CONTEXTMENU_MODIFY, 0, R.string.context_modify);
			menu.add(Menu.NONE, CONTEXTMENU_DELETE, 1, R.string.context_delete);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		Log.d(TAG, "position: " + info.position);
		if (item.getItemId() == CONTEXTMENU_DELETE) {
			showDialog(DIALOG_CONFIRM_DELETE);
		} else if (item.getItemId() == CONTEXTMENU_MODIFY) {

		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// TODO: what do? 1.6 doesnt support oncreatedialog with bundle param
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// TODO: teste ma mit if true
		if (dialogId == DIALOG_CONFIRM_DELETE) {
			Log.d(TAG, "building confirm delete dialog");
			builder.setMessage("Are you sure you want to delete?").setCancelable(false)
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// TODO: do the delete
							Log.d(TAG, "deleted");
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			return builder.create();
		}
		Log.d(TAG, "didnt create any dialog");
		return null;
	}

}
