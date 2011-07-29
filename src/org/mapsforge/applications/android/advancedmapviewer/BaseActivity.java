package org.mapsforge.applications.android.advancedmapviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Base Activity every Activity in the App can subclass. Contains methods and variables (almost)
 * every activity needs
 */
public class BaseActivity extends Activity {

	/**
	 * A reference to the App's Application
	 */
	protected AdvancedMapViewerApplication advancedMapViewer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.advancedMapViewer = (AdvancedMapViewerApplication) getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.basic_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_preferences:
				startActivity(new Intent(this, EditPreferences.class));
				return true;

			default:
				return false;
		}
	}

}
