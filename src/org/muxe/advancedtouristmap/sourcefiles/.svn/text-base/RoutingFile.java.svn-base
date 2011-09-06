package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

public class RoutingFile extends SourceFile {

	public enum Algorithm {
		HH, CONTRACTION
	}

	private Algorithm algorithm;

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

	public Algorithm getAlgorithm() {
		return this.algorithm;
	}

	@Override
	public SourceFileType getType() {
		return SourceFileType.ROUTING;
	}

	@Override
	public String toString() {
		return this.getDescription();
	}

}
