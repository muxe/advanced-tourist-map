package org.muxe.advancedtouristmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.core.GeoCoordinate;

import android.location.Location;

public class Utility {
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	/**
	 * Converts a GeoCoordinate into a GeoPoint
	 * @param gc the GeoCoordinate
	 * @return a GeoPoint representing the GeoCoordinate
	 */
	public static GeoPoint geoCoordinateToGeoPoint(GeoCoordinate gc) {
		return new GeoPoint(gc.getLatitude(), gc.getLongitude());
	}
	
	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation1
	 *            The current Location fix, to which you want to compare the new one
	 * @return boolean whether the location is better than the current best location
	 */
	public static boolean isBetterLocation(Location location, Location currentBestLocation1) {
		if (currentBestLocation1 == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation1.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation1.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation1.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks whether two providers are the same
	 * 
	 * @param provider1
	 *            First Provider Name
	 * @param provider2
	 *            Second Provider Name
	 * @return boolean if the two providers are the same
	 */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
