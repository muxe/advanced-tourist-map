package org.mapsforge.applications.android.advancedmapviewer;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

	AdvancedMapViewerApplication advancedMapViewer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.advancedMapViewer = (AdvancedMapViewerApplication) getApplication();
	}

}
