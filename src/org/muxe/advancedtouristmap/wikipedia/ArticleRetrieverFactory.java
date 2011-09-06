package org.muxe.advancedtouristmap.wikipedia;


public class ArticleRetrieverFactory {
	public static ArticleRetriever getGeonamesReceiver(String locale) {
		return new GeonamesRetriever(locale);
	}

	public static ArticleRetriever getWikilocationReceiver(String locale) {
		return new WikilocationRetriever(locale);
	}
}
