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
package org.muxe.advancedtouristmap.wikipedia;

import org.mapsforge.android.maps.GeoPoint;

import android.webkit.WebView;

public interface WikiArticleInterface {

	/**
	 * Sets all params for the given webView so that the WebView can displayed. Can either call
	 * loadUrl or loadData depending on whether it's an online or offline article.
	 * 
	 * @param webView
	 *            WebView to set
	 */
	public void setWebView(WebView webView);

	public String getLocale();

	public String getId();

	public void setId(String id);

	public double getLat();

	public void setLat(double lat);

	public double getLng();

	public void setLng(double lng);

	public String getTitle();

	public void setTitle(String title);

	public String getUrl();

	public void setUrl(String url);

	public String getType();

	public void setType(String type);

	public GeoPoint getGeoPoint();

}