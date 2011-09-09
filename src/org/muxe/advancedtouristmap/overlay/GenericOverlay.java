package org.muxe.advancedtouristmap.overlay;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.OverlayItem;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class GenericOverlay extends ArrayItemizedOverlay {

	private final Context context;
	
	public GenericOverlay(Context context, Drawable defaultMarker) {
		super(defaultMarker);
		this.context = context;
	}
	
	public GenericOverlay(Context context, Drawable defaultMarker, boolean alignMarker) {
		super(defaultMarker, alignMarker);
		this.context = context;
	}
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = createItem(index);
		if (item != null) {
			((GenericOverlayItem) item).onTap(this.context);
		}
		return true;
	}

}
