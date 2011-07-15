package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.applications.android.advancedmapviewer.routing.Route;
import org.mapsforge.applications.android.advancedmapviewer.routing.RoutingFile;
import org.mapsforge.applications.android.advancedmapviewer.routing.RoutingFileManager;
import org.mapsforge.core.Router;

import android.app.Application;
import android.util.Log;

/**
 * Base Application class to store Objects needed in more than one Activity like the Router
 */
public class AdvancedMapViewerApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	static final String ROUTING_BINARY_FILE = "/sdcard/tourist-map/berlin/berlin.mobileHH";
	// static final String ROUTING_BINARY_FILE = "/sdcard/berlin.mobileHH";
	private Router router;
	private RoutingFileManager routingFileManager;
	private RoutingFile[] routingFiles;
	public Route currentRoute;

	MapView mapView;

	@Override
	public void onCreate() {
		super.onCreate();
		// Log.d("Application", "onCreate()");
		// this.getRoutingFileManager()
		// .store(new RoutingFile("car", "/sdcard/tourist-map/car.HH"));
		// this.getRoutingFileManager().store(
		// new RoutingFile("foot", "/sdcard/tourist-map/foot.HH"));
	}

	/**
	 * Gets a Singleton Router Object with lazy creation.
	 * 
	 * @return the singleton Router Object
	 */
	public synchronized Router getRouter() {
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

	/**
	 * Gets a Singleton RoutingFileManager Object with lazy creation. This is needed to load and
	 * store the installed RoutingFiles
	 * 
	 * @return a RoutingFileManager Object
	 */
	public RoutingFileManager getRoutingFileManager() {
		if (this.routingFileManager == null) {
			Log.d("Application", "initiate rf");
			this.routingFileManager = new RoutingFileManager(this);
		}
		return this.routingFileManager;
	}

	/**
	 * Gets all currently installed RoutingFiles. The Files are only read on the first call and
	 * then cached. If RoutingFiles get changed/deleted/added the cache has to be deleted by
	 * calling {@link #resetRoutingFiles()}
	 * 
	 * @return all installed RoutingFiles
	 */
	public RoutingFile[] getRoutingFiles() {
		if (this.routingFiles == null) {
			this.routingFiles = this.getRoutingFileManager().getAllRoutingFiles();
		}
		return this.routingFiles;
	}

	/**
	 * Resets the Singleton of the installed RoutingFiles. Call this when something changed in
	 * the installed RoutingFiles.
	 */
	public void resetRoutingFiles() {
		this.routingFiles = null;
	}
}
