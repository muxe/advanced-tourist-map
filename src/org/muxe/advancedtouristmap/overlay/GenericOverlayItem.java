package org.muxe.advancedtouristmap.overlay;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;

public abstract class GenericOverlayItem extends OverlayItem {
	
	public GenericOverlayItem() {
		super();
	}
	public GenericOverlayItem(GeoPoint geoPoint, String title, String snippet) {
		super(geoPoint, title, snippet);
	}
	
	public GenericOverlayItem(GeoPoint point, String title, String snippet, Drawable marker) {
		super(point, title, snippet, marker);
	}

	public abstract void onTap(Context context);
}
