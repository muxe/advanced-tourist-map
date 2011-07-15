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
import android.widget.Toast;

public class RoutingFileSettings extends BaseActivity {
	static final String TAG = RoutingFileSettings.class.getSimpleName();

	protected static final int CONTEXTMENU_MODIFY = 0;
	protected static final int CONTEXTMENU_DELETE = 1;

	protected static final int DIALOG_CONFIRM_DELETE = 0;

	private Button addRoutingFileButton;
	private ListView installedRoutingFilesList;
	private TextView emptyListText;

	private ArrayAdapter<RoutingFile> routingFileAdapter;

	int storedRoutingFilePosition = -1;

	RoutingFile[] routingFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_routing_files);

		this.addRoutingFileButton = (Button) findViewById(R.id.manage_routing_files_button_add);
		this.installedRoutingFilesList = (ListView) findViewById(R.id.manage_routingfiles_list);
		this.emptyListText = (TextView) findViewById(R.id.manage_routing_files_empty_text);

		this.addRoutingFileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RoutingFileSettings.this.advancedMapViewer.getRoutingFileManager().store(
						new RoutingFile("car", "/sdcard/muh.HH"));
				RoutingFileSettings.this.refreshListView();
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
			// set the info about the calling RoutingFileRow (since bundle param just exists
			// since api 8)
			this.storedRoutingFilePosition = info.position;
			showDialog(DIALOG_CONFIRM_DELETE);
		} else if (item.getItemId() == CONTEXTMENU_MODIFY) {

		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");

		this.setListView();
		// register a context menu for long click
		registerForContextMenu(this.installedRoutingFilesList);
	}

	// onCreateDialog(int, bundle) is only supported since api 8, so we have to store the
	// calling view
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (dialogId == DIALOG_CONFIRM_DELETE) {
			if (this.storedRoutingFilePosition < 0) {
				return null;
			}
			builder.setMessage(getString(R.string.confirm_delete_qst))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									// TODO: catch exception
									RoutingFile toDelete = RoutingFileSettings.this.routingFiles[RoutingFileSettings.this.storedRoutingFilePosition];
									// do the delete
									if (RoutingFileSettings.this.advancedMapViewer
											.getRoutingFileManager().delete(toDelete)) {
										RoutingFileSettings.this.refreshListView();
									}
									// reset the position
									RoutingFileSettings.this.storedRoutingFilePosition = -1;
									Log.d(TAG, "deleted");
									Toast.makeText(
											RoutingFileSettings.this,
											String.format(
													getString(R.string.routing_routingfile_uninstalled_formatted),
													toDelete.name), Toast.LENGTH_LONG).show();
								}
							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
			return builder.create();
		}
		return null;
	}

	private void setListView() {
		this.routingFiles = this.advancedMapViewer.getRoutingFiles();

		if (this.routingFiles == null || this.routingFiles.length == 0) {
			this.installedRoutingFilesList.setVisibility(View.GONE);
			this.emptyListText.setVisibility(View.VISIBLE);
		} else {
			this.installedRoutingFilesList.setVisibility(View.VISIBLE);
			this.emptyListText.setVisibility(View.GONE);
		}

		this.routingFileAdapter = new ArrayAdapter<RoutingFile>(this,
				R.layout.installed_map_file_row, this.routingFiles);
		this.installedRoutingFilesList.setAdapter(this.routingFileAdapter);
	}

	void refreshListView() {
		// unset the global list
		this.advancedMapViewer.resetRoutingFiles();
		this.setListView();
	}
}
