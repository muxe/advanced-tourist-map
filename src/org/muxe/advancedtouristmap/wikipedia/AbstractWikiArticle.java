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
