package org.mapsforge.applications.android.advancedmapviewer;

import org.mapsforge.android.maps.GeoPoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class RouteCalculator extends BaseActivity {
	private static final String TAG = RouteCalculator.class.getSimpleName();

	protected static final int INTENT_SEARCH = 0;

	private GeoPoint startPoint;
	private GeoPoint destPoint;

	private Button chooseStartButton;
	private Button chooseDestButton;

	private EditText startEditText;
	private EditText destEditText;
	int viewToSet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculate_route);

		Intent startingIntent = getIntent();
		if (startingIntent.hasExtra("lat") && startingIntent.hasExtra("lon")) {
			this.destPoint = new GeoPoint(startingIntent.getDoubleExtra("lat", 0.0),
					startingIntent.getDoubleExtra("lon", 0.0));
		}

		this.startEditText = (EditText) findViewById(R.id.calculate_route_edittext_start);
		this.destEditText = (EditText) findViewById(R.id.calculate_route_edittext_dest);

		this.chooseStartButton = (Button) findViewById(R.id.calculate_route_button_choose_start);
		this.chooseDestButton = (Button) findViewById(R.id.calculate_route_button_choose_dest);

		OnClickListener startDestChooserListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				RouteCalculator.this.viewToSet = v.getId();
				startActivityForResult(new Intent(RouteCalculator.this, Search.class),
						INTENT_SEARCH);
			}
		};

		this.chooseStartButton.setOnClickListener(startDestChooserListener);
		this.chooseDestButton.setOnClickListener(startDestChooserListener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_SEARCH) {
			if (resultCode == RESULT_OK) {
				if (data != null && data.hasExtra("lon") && data.hasExtra("lat")) {
					double lon = data.getDoubleExtra("lon", 0.0);
					double lat = data.getDoubleExtra("lat", 0.0);
					GeoPoint point = new GeoPoint(lat, lon);
					if (this.viewToSet == this.chooseStartButton.getId()) {
						this.startPoint = point;
					} else {
						this.destPoint = point;
					}
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (this.startPoint != null) {
			this.startEditText.setText(this.startPoint.getLatitude() + " "
					+ this.startPoint.getLongitude());
		}
		if (this.destPoint != null) {
			this.destEditText.setText(this.destPoint.getLatitude() + " "
					+ this.destPoint.getLongitude());
		}
	}

}
