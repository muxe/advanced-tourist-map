package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;

import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.applications.android.advancedmapviewer.routing.Route;
import org.mapsforge.applications.android.advancedmapviewer.routing.RoutingFile;
import org.mapsforge.applications.android.advancedmapviewer.routing.RoutingFileManager;
import org.mapsforge.core.Router;

import android.app.Application;
import android.util.Log;

/**
 * Base Application class
 * 
 * @author Max Dörfler
 */
public class AdvancedMapViewerApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	static final String ROUTING_BINARY_FILE = "/sdcard/tourist-map/berlin/berlin.mobileHH";
	// static final String ROUTING_BINARY_FILE = "/sdcard/berlin.mobileHH";
	private Router router;
	private RoutingFileManager routingFileManager;
	private RoutingFile[] routingFiles;
	public Route currentRoute;

	@Override
	public void onCreate() {
		super.onCreate();
		// Log.d("Application", "onCreate()");
		// this.getRoutingFileManager()
		// .store(new RoutingFile("car", "/sdcard/tourist-map/car.HH"));
		// this.getRoutingFileManager().store(
		// new RoutingFile("foot", "/sdcard/tourist-map/foot.HH"));
	}

	public Router getRouter() {
		if (this.router == null) {
			try {
				this.router = new HHRouter(new File(ROUTING_BINARY_FILE),
						ROUTING_MAIN_MEMORY_CACHE_SIZE);
				Log.d("Application", "initialized");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d("Application",
				this.router.getBoundingBox().maxLatitudeE6 + ", "
						+ this.router.getBoundingBox().maxLongitudeE6 + " / "
						+ this.router.getBoundingBox().minLatitudeE6 + ", "
						+ this.router.getBoundingBox().minLongitudeE6);
		return this.router;
	}

	public Router getRouter(RoutingFile rf) {
		// TODO: how to check if other routingfile is needed?
		if (this.router == null) {
			try {
				this.router = new HHRouter(new File(rf.path), ROUTING_MAIN_MEMORY_CACHE_SIZE);
				Log.d("Application", "initialized");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.router;
	}

	public RoutingFileManager getRoutingFileManager() {
		if (this.routingFileManager == null) {
			Log.d("Application", "initiate rf");
			this.routingFileManager = new RoutingFileManager(this);
		}
		return this.routingFileManager;
	}

	public RoutingFile[] getRoutingFiles() {
		if (this.routingFiles == null) {
			this.routingFiles = this.getRoutingFileManager().getAllRoutingFiles();
		}
		return this.routingFiles;
	}

	public void resetRoutingFiles() {
		this.routingFiles = null;
	}
}
