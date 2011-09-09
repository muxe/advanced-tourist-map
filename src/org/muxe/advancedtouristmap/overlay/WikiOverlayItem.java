package org.muxe.advancedtouristmap.overlay;

import org.mapsforge.android.maps.GeoPoint;
import org.muxe.advancedtouristmap.PositionInfo;
import org.muxe.advancedtouristmap.R;
import org.muxe.advancedtouristmap.wikipedia.WikiArticleInterface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WikiOverlayItem extends GenericOverlayItem {
	
	private WikiArticleInterface article;
	
	public WikiOverlayItem(WikiArticleInterface article) {
		this.article = article;
	}
	
	public WikiOverlayItem(WikiArticleInterface article, String title, String snippet) {
		super(article.getGeoPoint(), title, snippet);
		this.article = article;
	}
	
	public WikiOverlayItem(WikiArticleInterface article, String title, String snippet, Drawable marker) {
		super(article.getGeoPoint(), title, snippet, marker);
		this.article = article;
	}
	
	public WikiArticleInterface getArticle() {
		return this.article;
	}

	public void setArticle(WikiArticleInterface article) {
		this.article = article;
	}

	@Override
	public void onTap(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater factory = LayoutInflater
				.from(context);
		final View dialogView = factory.inflate(R.layout.webview_dialog,
				null);
		builder.setTitle(article.getTitle());
		// a progress bar to indicate loading
		final ProgressBar progressBar = (ProgressBar) dialogView
				.findViewById(R.id.webview_dialog_progress);
		WebView webView = (WebView) dialogView
				.findViewById(R.id.webview_dialog_webview);

		// handle link clicks internally (doesn't open new browser window)
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				progressBar.setVisibility(View.VISIBLE);
				view.loadUrl(url);
				return false;
			}
		});

		// show loading progress
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100) {
					progressBar.setVisibility(View.GONE);
				} else {
					progressBar.setProgress(progress);
				}
			}

		});

		// let the article set what to render (load url or local data)
		article.setWebView(webView);

		builder.setView(dialogView);

		builder.setPositiveButton("Info",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						context.startActivity(new Intent(context,
								PositionInfo.class).putExtra(
								PositionInfo.LATITUDE_EXTRA,
								article.getLat()).putExtra(
								PositionInfo.LONGITUDE_EXTRA,
								article.getLng()));
					}
				});

		builder.setNegativeButton("Close", null);

		builder.show();
	}
}
