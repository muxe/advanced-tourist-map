package org.mapsforge.applications.android.advancedmapviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.applications.android.advancedmapviewer.routing.Route;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.FileManager;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.MapBundle;
import org.mapsforge.applications.android.advancedmapviewer.sourcefiles.RoutingFile;
import org.mapsforge.applications.android.advancedmapviewer.wikipedia.WikiArticleInterface;
import org.mapsforge.core.Router;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.persistence.IPersistenceManager;
import org.mapsforge.poi.persistence.PersistenceManagerFactory;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;

/**
 * Base Application class to store Objects needed in more than one Activity like the Router
 */
public class AdvancedMapViewerApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	private Router router;
	public Route currentRoute;

	/** The currently displayed pois on map */
	private ArrayList<PointOfInterest> currentPois;
	private ArrayList<WikiArticleInterface> currentWikiArticles;
	private FileManager fileManager;
	private String baseBundlePath;
	private String currentUsedBundlePath;
	private MapBundle currentMapBundle;
	private String currentRoutingFile;
	public SharedPreferences prefs;
	private IPersistenceManager perstManager;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs
				.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
					@Override
					public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
							String key) {
						// TODO: use this to react to changes in e.g. mapfilepath instead of
						// EditPreferences
					}
				});
	}

	public MapBundle getCurrentMapBundle() {
		if (this.currentMapBundle == null) {
			this.currentUsedBundlePath = this.prefs.getString("bundlePath", null);
			if (this.currentUsedBundlePath != null) {
				this.currentMapBundle = this.getFileManager().getSingleBundle(
						this.currentUsedBundlePath);
				Log.d("FileManager", "got current bundle: " + this.currentUsedBundlePath);
			}
		}
		return this.currentMapBundle;
	}

	public void resetCurrentMapBundle() {
		this.currentMapBundle = null;
		this.resetRouter();
		this.perstManager = null;
	}

	public synchronized FileManager getFileManager() {
		TimingLogger timings = new TimingLogger("timing", "getFileManager");
		Log.d("FileManager", "get file manager");
		String directory = this.getBaseBundlePath();
		if (this.fileManager == null) {
			this.fileManager = new FileManager(directory);
			timings.addSplit("got new file manager");
			Log.d("FileManager", "got new file manager");
		} else if (!this.fileManager.getBaseDirectory().equals(directory)) {
			this.fileManager.rescan(directory);
			timings.addSplit("rescanned file manager");
			Log.d("FileManager", "rescanned: " + directory);
		}
		timings.dumpToLog();
		return this.fileManager;
	}

	/**
	 * Gets a Router Object based on the path to a Routing Binary. If there already is a Router
	 * object it is checked whether the existent Router is based on the same Routing Binary.
	 * Only if they differ, a new Router gets created and returned.
	 * 
	 * @param file
	 *            Absolute path to a Routing Binary
	 * @return Router Object
	 */
	public synchronized Router getRouter(String file) {
		if (this.router == null || !this.currentRoutingFile.equals(file)) {
			try {
				this.router = new HHRouter(new File(file), ROUTING_MAIN_MEMORY_CACHE_SIZE);
				this.currentRoutingFile = file;
				Log.d("Application", "new Router created: " + file);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return this.router;
	}

	/**
	 * Gets a Singleton Router Object with lazy creation. If there already is a Router, this one
	 * is returned. If there is none but the path to the last used binary is stored, a new
	 * Router for this binary gets created. If no info is stored or passed at all, the first
	 * Routing Binary from the current Map Bundle is used.
	 * 
	 * @return the singleton Router Object
	 */
	public synchronized Router getRouter() {
		if (this.router == null) {
			if (this.currentRoutingFile != null) {
				return this.getRouter(this.currentRoutingFile);
			}
			// no router yet, and no filepath given: take first one
			ArrayList<RoutingFile> rfs = this.getCurrentMapBundle().getRoutingFiles();
			if (rfs.size() > 0) {
				String path = this.getBaseBundlePath() + File.separator
						+ rfs.get(0).getRelativePath();
				return this.getRouter(path);
			}
		}
		return this.router;
	}

	public void resetRouter() {
		this.router = null;
		this.currentRoutingFile = null;
	}

	public String getBaseBundlePath() {
		if (this.baseBundlePath == null) {
			this.baseBundlePath = this.prefs.getString("baseBundlePath", "/sdcard");
		}
		return this.baseBundlePath;
	}

	public void resetBaseBundlePath() {
		this.baseBundlePath = null;
		// if the base path is changed, the current bundles are useless
		this.resetCurrentMapBundle();
	}

	public IPersistenceManager getPerstManager() {
		if (this.perstManager == null) {
			if (getCurrentMapBundle().isPoiable()) {
				this.perstManager = PersistenceManagerFactory
						.getPerstPersistenceManager(getBaseBundlePath() + File.separator
								+ getCurrentMapBundle().getPoiFile().getRelativePath());
			}
		}
		return this.perstManager;
	}

	public ArrayList<PointOfInterest> getCurrentPois() {
		if (this.currentPois == null) {
			this.currentPois = new ArrayList<PointOfInterest>();
		}
		return this.currentPois;
	}

	public ArrayList<WikiArticleInterface> getCurrentWikiArticles() {
		if (this.currentWikiArticles == null) {
			this.currentWikiArticles = new ArrayList<WikiArticleInterface>();
		}
		return this.currentWikiArticles;
	}

	public String getWikiLocale() {
		String savedLocale = this.prefs.getString("wikiLang", "default");
		if (savedLocale.equals("default")) {
			savedLocale = Locale.getDefault().getLanguage();
		}
		// TODO: check if wikipedia avaiable in this locale oO
		return savedLocale;
	}
}
