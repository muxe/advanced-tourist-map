package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;

import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.core.Router;

import android.app.Application;
import android.util.Log;

public class AdvancedMapViewerApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	static final String ROUTING_BINARY_FILE = "/sdcard/berlin.mobileHH";
	private Router router;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Application", "onCreate()");
	}

	public Router getRouter() {
		Log.d("Application", "getRouter");
		if (this.router == null) {
			try {
				this.router = new HHRouter(new File(ROUTING_BINARY_FILE),
						ROUTING_MAIN_MEMORY_CACHE_SIZE);
				Log.d("Application", "initialized");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.router;
	}

}
