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