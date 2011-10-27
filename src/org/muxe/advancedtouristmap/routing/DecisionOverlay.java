/*
 * Copyright 2011 mapsforge.org
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
package org.muxe.advancedtouristmap.routing;

import org.mapsforge.android.maps.ArrayItemizedOverlay;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Overlay Class to display DecisionPoints of a Route. The overlay items are centered at the
 * center of the bounding Box
 */
public class DecisionOverlay extends ArrayItemizedOverlay {
	
	private final Context context;

	public DecisionOverlay(Context context, Drawable defaultMarker) {
		super(defaultMarker, false);
		this.context = context;
		boundCenter(defaultMarker);
	}

	@Override
	protected boolean onTap(int index) {
		Log.d("RouteCalculator", "Tapped on DecisionPoint: " + index);
		this.context.startActivity(new Intent(this.context, RouteList.class).putExtra("dp_index", index));
		return true;
	}

}
