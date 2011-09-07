/*
 * Copyright 2010, 2011 mapsforge.org
 *
 *	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.muxe.advancedtouristmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Overlay;
import org.mapsforge.android.maps.Projection;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;

public class LocationPicker extends MapActivity {

	private MapView mapView;

	// AdvancedMapViewerApplication advancedMapViewerApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// this.advancedMapViewerApplication = (AdvancedMapViewerApplication) getApplication();

		this.mapView = new MapView(this);
		this.mapView.setClickable(true);
		this.mapView.setBuiltInZoomControls(true);
		// mapView.setMapFile("/sdcard/tourist-map/berlin/berlin.map");
		setContentView(this.mapView);

		this.mapView.getOverlays().add(new Overlay() {

			@Override
			protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
					Projection projection, byte drawZoomLevel) {
				// do nothing
			}

			@Override
			public boolean onLongPress(GeoPoint geoPoint, MapView mv) {
				LocationPicker.this.setResult(
						RESULT_OK,
						new Intent().putExtra("LONGITUDE", geoPoint.getLongitude()).putExtra(
								"LATITUDE", geoPoint.getLatitude()));
				LocationPicker.this.finish();
				return true;
			}
		});
	}
}
