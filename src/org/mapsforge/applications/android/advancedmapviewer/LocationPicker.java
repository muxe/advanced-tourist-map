package org.mapsforge.applications.android.advancedmapviewer;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
