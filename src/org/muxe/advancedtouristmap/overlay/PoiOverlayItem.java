package org.muxe.advancedtouristmap.overlay;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.poi.PointOfInterest;
import org.muxe.advancedtouristmap.PositionInfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public class PoiOverlayItem extends GenericOverlayItem {

	private PointOfInterest poi;

	public PoiOverlayItem(PointOfInterest poi) {
		super();
		this.setPoi(poi);
	}

	public PoiOverlayItem(PointOfInterest poi, String title, String snippet,
			Drawable marker) {
		super(new GeoPoint(poi.getLatitude(), poi.getLongitude()), title,
				snippet, marker);
		this.setPoi(poi);
	}

	public PoiOverlayItem(PointOfInterest poi, String title, String snippet) {
		super(new GeoPoint(poi.getLatitude(), poi.getLongitude()), title,
				snippet);
		this.setPoi(poi);
	}

	public void setPoi(PointOfInterest poi) {
		this.poi = poi;
	}

	public PointOfInterest getPoi() {
		return this.poi;
	}

	@Override
	public void onTap(final Context context) {
		Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setTitle(this.getTitle());
		builder.setMessage(this.getSnippet());
		builder.setPositiveButton("OK", null);
		builder.setNeutralButton("Info", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				context.startActivity(new Intent(context, PositionInfo.class)
						.putExtra(PositionInfo.LATITUDE_EXTRA,
								poi.getLatitude()).putExtra(
								PositionInfo.LONGITUDE_EXTRA,
								poi.getLongitude()));
			}
		});
		builder.show();
	}
}
