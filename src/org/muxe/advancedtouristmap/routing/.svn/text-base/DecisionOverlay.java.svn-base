package org.mapsforge.applications.android.advancedmapviewer.routing;

import org.mapsforge.android.maps.ArrayItemizedOverlay;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Overlay Class to display DecisionPoints of a Route. The overlay items are centered at the
 * center of the bounding Box
 */
public class DecisionOverlay extends ArrayItemizedOverlay {

	public DecisionOverlay(Drawable defaultMarker) {
		super(defaultMarker, false);
		boundCenter(defaultMarker);
	}

	@Override
	protected boolean onTap(int index) {
		Log.d("RouteCalculator", "Tapped on DecisionPoint: " + index);
		return true;
	}

}
