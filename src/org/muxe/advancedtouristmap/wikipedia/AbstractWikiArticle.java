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

public abstract class AbstractWikiArticle implements WikiArticleInterface {

	private String id;
	private double lat;
	private double lng;
	private String title;
	private String url;
	private String type;
	private String locale;

	public AbstractWikiArticle(String locale) {
		this.locale = locale;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public double getLat() {
		return this.lat;
	}

	@Override
	public void setLat(double lat) {
		this.lat = lat;
	}

	@Override
	public double getLng() {
		return this.lng;
	}

	@Override
	public void setLng(double lng) {
		this.lng = lng;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getUrl() {
		return this.url;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public GeoPoint getGeoPoint() {
		return new GeoPoint(this.lat, this.lng);
	}

	@Override
	public abstract void setWebView(WebView webView);

	@Override
	public String getLocale() {
		return this.locale;
	}

}
