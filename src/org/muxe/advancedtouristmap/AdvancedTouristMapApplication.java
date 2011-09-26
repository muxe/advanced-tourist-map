package org.muxe.advancedtouristmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.mapsforge.android.mobileHighwayHierarchies.HHRouter;
import org.mapsforge.core.Router;
import org.mapsforge.poi.PointOfInterest;
import org.mapsforge.poi.persistence.IPersistenceManager;
import org.mapsforge.poi.persistence.PersistenceManagerFactory;
import org.muxe.advancedtouristmap.routing.Route;
import org.muxe.advancedtouristmap.sourcefiles.FileManager;
import org.muxe.advancedtouristmap.sourcefiles.MapBundle;
import org.muxe.advancedtouristmap.sourcefiles.RoutingFile;
import org.muxe.advancedtouristmap.wikipedia.WikiArticleInterface;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Base Application class to store Objects needed in more than one Activity like the Router
 */
public class AdvancedTouristMapApplication extends Application {

	static final int ROUTING_MAIN_MEMORY_CACHE_SIZE = 1024 * 2048;
	public static final String HELP_PREFERENCES_NAME = "helpModulePrefs";
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
	public boolean positioningEnabled;

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

	public void setViewWithHelp(Activity activity, int layoutResource) {
		final String helpResource = getHelpResource(activity);
		// check if really needs help
		if (helpResource == null || hasSeenHelp(helpResource)) {
			activity.setContentView(layoutResource);
		} else {
			// create a new framelayout as new root element
			FrameLayout frameLayout = new FrameLayout(activity);
			// add the actual view to the new root
			activity.getLayoutInflater().inflate(layoutResource, frameLayout);
			// also add the help layout (will overlay the actual layout)
			activity.getLayoutInflater().inflate(R.layout.help_layout, frameLayout);
			// set the newly created view as content view
			activity.setContentView(frameLayout);

			final LinearLayout helpLayoutRoot = (LinearLayout) activity
					.findViewById(R.id.help_linear_layout);
			// webview to display help text
			WebView webView = (WebView) activity.findViewById(R.id.help_web_view);
			// button to close the help view
			ImageView closeButton = (ImageView) activity.findViewById(R.id.help_close_button);

			// close the help view on button click
			closeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					helpLayoutRoot.setVisibility(View.GONE);
					// mark this help article read
					setHelpSeen(helpResource);
				}
			});
			// handle links internally
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return false;
				}
			});
			// load the html formatted help resource
			webView.loadUrl(helpResource);
			// make the background transparent (needs to be called *after* loadUrl)
			webView.setBackgroundColor(0x00000000);
		}
	}

	/**
	 * Gets the URI to a help file in the assets folder to display in a WebView. The name of the
	 * xml file is the name of the activity in lowercase. It is then checked if there is a file
	 * for the default locale (which needs to be in folder help-<locale>/) if there is none, it
	 * falls back to the default en-locale. So if the activity is "PositionInfo" it is first
	 * checked if "/assets/help-<locale>/positioninfo.xml" exists, and the URI
	 * "file:///android_asset/help-<locale>/positioninfo.xml" is returned.
	 * 
	 * If it doesn't exist, "/assets/help-en/positioninfo.xml" is checked.
	 * 
	 * If this doesn't exist as well, null is returned
	 * 
	 * @param activity
	 *            Activity for which to create the help resource
	 * 
	 * @return a file URI to a found xml resource or null if no matching resource could be found
	 */
	private String getHelpResource(Activity activity) {
		String languageLocale = Locale.getDefault().getLanguage();
		String activityNamePck = activity.getClass().getName().toLowerCase(Locale.getDefault());
		// cut out the package information
		int mid = activityNamePck.lastIndexOf('.') + 1;
		String activityName = activityNamePck.substring(mid);
		AssetManager assetManager = getResources().getAssets();

		String result = "help-" + languageLocale + "/" + activityName + ".xml";
		try {
			assetManager.open(result);
			return "file:///android_asset/" + result;
		} catch (IOException e) {
		}
		// file didn't exist, check again with en locale
		result = "help-en/" + activityName + ".xml";
		try {
			assetManager.open(result);
			return "file:///android_asset/" + result;
		} catch (IOException e) {
		}
		// didn't exist as well, return null :(
		return null;
	}

	private boolean hasSeenHelp(String key) {
		SharedPreferences helpPrefs = getSharedPreferences(HELP_PREFERENCES_NAME, MODE_PRIVATE);
		return helpPrefs.getBoolean(key, false);
	}

	void setHelpSeen(String key) {
		Editor editor = getSharedPreferences(HELP_PREFERENCES_NAME, MODE_PRIVATE).edit();
		editor.putBoolean(key, true);
		editor.commit();
	}
}
