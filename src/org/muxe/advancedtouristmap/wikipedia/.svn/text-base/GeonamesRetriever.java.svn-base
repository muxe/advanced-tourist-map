package org.mapsforge.applications.android.advancedmapviewer.wikipedia;

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

public class GeonamesRetriever implements ArticleRetriever {

	private static final String USERNAME = "muxe";
	private static final String BASEURL = "http://api.geonames.org";
	private String locale;
	private DocumentBuilder dBuilder;

	public GeonamesRetriever(String locale) {
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

		String url = BASEURL + "/findNearbyWikipedia?username=" + USERNAME + "&lang="
				+ this.locale;

		url += "&lat=" + geoPoint.getLatitude();
		url += "&lng=" + geoPoint.getLongitude();

		// convert meter to km
		int radius_km = radius / 1000;
		if (radius_km > 0 && radius_km <= 20) {
			url += "&radius=" + radius_km;
		}
		if (limit > 0 && limit <= 500) {
			url += "&maxRows=" + limit;
		}
		Log.d("PositionInfo", url);

		try {
			Document doc = this.dBuilder.parse(loadXml(url));
			doc.getDocumentElement().normalize();

			NodeList articleNodes = doc.getElementsByTagName("entry");
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
						} else if (articlePropertyName.equalsIgnoreCase("feature")) {
							wikiArticle.setType(articleProperty.getFirstChild().getNodeValue());
						} else if (articlePropertyName.equalsIgnoreCase("wikipediaUrl")) {
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
