package org.mapsforge.applications.android.advancedmapviewer;

import android.app.Activity;
import android.os.Bundle;

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

}
