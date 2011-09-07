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
