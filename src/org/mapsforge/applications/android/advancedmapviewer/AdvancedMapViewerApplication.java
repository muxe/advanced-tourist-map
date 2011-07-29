package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.applications.android.advancedmapviewer.routing.Route;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.FileManager;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.MapBundle;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.RoutingFile;
import org.mapsforge.core.Router;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;

/**
 * Base Application class to store Objects needed in more than one Activity like the Router
 */
public class AdvancedMapViewerApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	static final String ROUTING_BINARY_FILE = "/sdcard/tourist-map/berlin/berlin.mobileHH";
	// static final String ROUTING_BINARY_FILE = "/sdcard/berlin.mobileHH";
	private Router router;
	public Route currentRoute;
	private FileManager fileManager;
	private String baseBundlePath;
	private String currentUsedBundlePath;
	private MapBundle currentMapBundle;
	public SharedPreferences prefs;

	MapView mapView;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	public MapBundle getCurrentMapBundle() {
		if (this.currentMapBundle == null) {
			this.currentUsedBundlePath = this.prefs.getString("bundlePath", null);
			if (this.currentUsedBundlePath != null) {
				this.currentMapBundle = this.getFileManager().getSingleBundle(
						this.currentUsedBundlePath);
			}
		}
		return this.currentMapBundle;
	}

	public void resetCurrentMapBundle() {
		this.currentMapBundle = null;
		this.router = null;
	}

	public synchronized FileManager getFileManager() {
		TimingLogger timings = new TimingLogger("timing", "getFileManager");
		String directory = this.getBaseBundlePath();
		if (this.fileManager == null) {
			this.fileManager = new FileManager(directory);
			timings.addSplit("got new file manager");
		} else if (!this.fileManager.getBaseDirectory().equals(directory)) {
			this.fileManager.rescan(directory);
			timings.addSplit("rescanned file manager");
		}
		timings.dumpToLog();
		return this.fileManager;
	}

	/**
	 * Gets a Singleton Router Object with lazy creation.
	 * 
	 * @return the singleton Router Object
	 */
	public synchronized Router getRouter() {
		if (this.router == null) {
			try {
				// TODO: dirty! just takes the first one
				ArrayList<RoutingFile> rfs = this.getCurrentMapBundle().getRoutingFiles();
				if (rfs.size() > 0) {
					String path = this.getBaseBundlePath() + File.separator
							+ rfs.get(0).getRelativePath();
					Log.d("Application", "new Routing file path: " + path);
					this.router = new HHRouter(new File(path), ROUTING_MAIN_MEMORY_CACHE_SIZE);
					Log.d("Application", "initialized");
				} else {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		Log.d("Application",
				this.router.getBoundingBox().maxLatitudeE6 + ", "
						+ this.router.getBoundingBox().maxLongitudeE6 + " / "
						+ this.router.getBoundingBox().minLatitudeE6 + ", "
						+ this.router.getBoundingBox().minLongitudeE6);
		return this.router;
	}

	/**
	 * Gets all currently installed RoutingFiles. The Files are only read on the first call and
	 * then cached. If RoutingFiles get changed/deleted/added the cache has to be deleted by
	 * calling {@link #resetRoutingFiles()}
	 * 
	 * @return all installed RoutingFiles
	 */
	// public RoutingFile[] getRoutingFiles() {
	// // TODO:
	// return new RoutingFile[0];
	// }

	/**
	 * Resets the Singleton of the installed RoutingFiles. Call this when something changed in
	 * the installed RoutingFiles.
	 */
	// public void resetRoutingFiles() {
	// this.routingFiles = null;
	// }

	public void resetRouter() {
		this.router = null;
	}

	public String getBaseBundlePath() {
		if (this.baseBundlePath == null) {
			this.baseBundlePath = this.prefs.getString("baseBundlePath", "/sdcard");
		}
		return this.baseBundlePath;
	}

	public void resetBaseBundlePath() {
		this.baseBundlePath = null;
	}
}
