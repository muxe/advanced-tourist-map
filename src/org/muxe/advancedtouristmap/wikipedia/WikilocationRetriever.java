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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.mapsforge.android.maps.GeoPoint;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.util.Log;

public class WikilocationRetriever implements ArticleRetriever {

	private static final String BASEURL = "http://api.wikilocation.org";
	private String locale;
	private DocumentBuilder dBuilder;

	public WikilocationRetriever(String locale) {
		this.locale = locale;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			this.dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ArrayList<WikiArticleInterface> getArticles(GeoPoint geoPoint, int radius,
			int limit, int offset) {
		ArrayList<WikiArticleInterface> result = new ArrayList<WikiArticleInterface>();

		String url = BASEURL + "/articles?locale=" + this.locale + "&format=xml";
		url += "&lat=" + geoPoint.getLatitude();
		url += "&lng=" + geoPoint.getLongitude();

		if (radius > 0 && radius <= 20000) {
			url += "&radius=" + radius;
		}
		if (limit > 0 && limit <= 50) {
			url += "&limit=" + limit;
		}
		if (offset > 0) {
			url += "&offset=" + offset;
		}
		Log.d("PositionInfo", url);
		try {
			Document doc = this.dBuilder.parse(loadXml(url));
			doc.getDocumentElement().normalize();

			NodeList articleNodes = doc.getElementsByTagName("article");
			for (int i = 0; i < articleNodes.getLength(); i++) {
				OnlineWikiArticle wikiArticle = new OnlineWikiArticle(this.locale);
				Node articleNode = articleNodes.item(i);
				NodeList articleNodeDetails = articleNode.getChildNodes();
				for (int j = 0; j < articleNodeDetails.getLength(); j++) {
					try {
						Node articleProperty = articleNodeDetails.item(j);
						String articlePropertyName = articleProperty.getNodeName();
						if (articlePropertyName.equalsIgnoreCase("lat")) {
							wikiArticle.setLat(Double.parseDouble(articleProperty
									.getFirstChild().getNodeValue()));
						} else if (articlePropertyName.equalsIgnoreCase("lng")) {
							wikiArticle.setLng(Double.parseDouble(articleProperty
									.getFirstChild().getNodeValue()));
						} else if (articlePropertyName.equalsIgnoreCase("title")) {
							wikiArticle
									.setTitle(articleProperty.getFirstChild().getNodeValue());
						} else if (articlePropertyName.equalsIgnoreCase("id")) {
							wikiArticle.setId(articleProperty.getFirstChild().getNodeValue());
						} else if (articlePropertyName.equalsIgnoreCase("type")) {
							wikiArticle.setType(articleProperty.getFirstChild().getNodeValue());
						} else if (articlePropertyName.equalsIgnoreCase("url")) {
							wikiArticle.setUrl(articleProperty.getFirstChild().getNodeValue());
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}

				}
				result.add(wikiArticle);
			}

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Opens a remote/local File and returns a InputStream
	 * 
	 * @param path
	 *            Path of the File as URL (for local files use: file:/path/to/go)
	 * @return InputStream for the File
	 */
	private InputStream loadXml(String path) {
		try {
			URL url = new URL(path);
			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			return is;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
