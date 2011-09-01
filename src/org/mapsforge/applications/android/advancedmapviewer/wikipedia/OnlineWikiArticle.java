package org.mapsforge.applications.android.advancedmapviewer.wikipedia;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.webkit.WebView;

public class OnlineWikiArticle extends AbstractWikiArticle {

	public OnlineWikiArticle(String locale) {
		super(locale);
	}

	@Override
	public void setWebView(WebView webView) {
		// needed to expand collapsed categories
		webView.getSettings().setJavaScriptEnabled(true);

		if (this.getUrl() != null) {
			// webView.loadUrl(this.getUrl());
			// webView.loadUrl("http://www.hashemian.com/whoami/");
			try {
				String encodedTitle = URLEncoder.encode(this.getTitle(), "UTF-8");
				webView.loadUrl("http://" + this.getLocale() + ".m.wikipedia.org/wiki?search="
						+ encodedTitle);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}
}
