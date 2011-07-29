package org.mapsforge.applications.android.advancedmapviewer.sourcefiles;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class for managing the source files needed by the different mapsforge components used in this
 * app. Handles reading of XML files and checking the correctness.
 * 
 * @author Max Dörfler
 * 
 */
public class FileManager {

	private String baseDirectory;
	private ArrayList<MapBundle> installedBundles;

	private DocumentBuilder dBuilder;

	// some pre compiled xpath expressions to find data in the XML
	private XPathExpression xpathGetName;
	private XPathExpression xpathGetMap;
	private XPathExpression xpathGetRoutingFiles;
	private XPathExpression xpathGetAddress;
	private XPathExpression xpathGetPoi;
	private XPathExpression xpathGetFilename;
	private XPathExpression xpathGetPath;
	private XPathExpression xpathGetMD5;
	private XPathExpression xpathGetFileSize;
	private XPathExpression xpathGetDate;
	private XPathExpression xpathGetDesc;
	private XPathExpression xpathGetBoundingBox;

	public FileManager(String baseDirectory) {
		this.baseDirectory = baseDirectory;

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			this.dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		try {
			this.xpathGetName = xpath.compile("/mapbundle/name/text()");
			this.xpathGetMap = xpath.compile("/mapbundle/map[1]");
			this.xpathGetRoutingFiles = xpath.compile("/mapbundle/routingfiles/routingfile");
			this.xpathGetAddress = xpath.compile("/mapbundle/addressfile[1]");
			this.xpathGetPoi = xpath.compile("/mapbundle/poifile[1]");
			this.xpathGetFilename = xpath.compile("child::filename/text()");
			this.xpathGetPath = xpath.compile("child::path/text()");
			this.xpathGetDate = xpath.compile("child::date_of_creation/text()");
			this.xpathGetMD5 = xpath.compile("child::md5/text()");
			this.xpathGetFileSize = xpath.compile("child::file_size/text()");
			this.xpathGetDesc = xpath.compile("child::desc/text()");
			this.xpathGetBoundingBox = xpath.compile("child::bounding_box/@*");

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public void rescan(String directory) {
		if (directory != null) {
			this.baseDirectory = directory;
		}
		if (this.installedBundles != null) {
			this.installedBundles.clear();
		}
		this.findBundleXML(this.baseDirectory);
	}

	private void findBundleXML(String directory) {
		File[] xmlFiles = new File(directory).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".xml");
			}
		});
		if (xmlFiles != null) {
			for (File f : xmlFiles) {
				MapBundle bundle = buildMapBundle(loadXml("file:" + f.getAbsolutePath()),
						f.getAbsolutePath());
				if (bundle != null) {
					this.installedBundles.add(bundle);
				}
			}
		}
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

	/**
	 * Parses a XML-File (as InputStream) and builds a MapBundle
	 * 
	 * @param xmlStream
	 *            the XML file as InputStream
	 * @param filePath
	 *            the full path of the XML-File
	 * @return MapBundle representation of the XML
	 */
	private MapBundle buildMapBundle(InputStream xmlStream, String filePath) {
		MapBundle mapBundle = new MapBundle();
		mapBundle.setFilepathXml(filePath);
		if (xmlStream == null) {
			return null;
		}
		try {
			Document doc = this.dBuilder.parse(xmlStream);

			doc.getDocumentElement().normalize();
			// getName
			String name = (String) this.xpathGetName.evaluate(doc, XPathConstants.STRING);
			mapBundle.setName(name);

			// get Mapfile
			Node mapNode = (Node) this.xpathGetMap.evaluate(doc, XPathConstants.NODE);
			MapFile mapFile = new MapFile();
			this.parseBasicFileInfo(mapFile, mapNode);

			// get bounding box (as attributes)
			NodeList bounding_box = (NodeList) this.xpathGetBoundingBox.evaluate(mapNode,
					XPathConstants.NODESET);
			for (int i = 0; i < bounding_box.getLength(); i++) {
				String nodeName = bounding_box.item(i).getNodeName();
				double nodeValue = Double.parseDouble(bounding_box.item(i).getNodeValue());
				if (nodeName.equals("min_lat")) {
					mapFile.setMin_lat(nodeValue);
				} else if (nodeName.equals("max_lat")) {
					mapFile.setMax_lat(nodeValue);
				} else if (nodeName.equals("min_lon")) {
					mapFile.setMin_lon(nodeValue);
				} else if (nodeName.equals("max_lon")) {
					mapFile.setMax_lon(nodeValue);
				}
			}
			mapBundle.setMapFile(mapFile);

			// get RoutingFiles
			NodeList routingNodes = (NodeList) this.xpathGetRoutingFiles.evaluate(doc,
					XPathConstants.NODESET);
			// loop routing files
			for (int i = 0; i < routingNodes.getLength(); i++) {
				RoutingFile rf = new RoutingFile();
				this.parseBasicFileInfo(rf, routingNodes.item(i));
				mapBundle.addRoutingFile(rf);
			}

			// get Address File
			Node addressNode = (Node) this.xpathGetAddress.evaluate(doc, XPathConstants.NODE);
			if (addressNode != null) {
				AddressFile addressFile = new AddressFile();
				this.parseBasicFileInfo(addressFile, addressNode);
				mapBundle.setAddressFile(addressFile);
			}

			// get POI File
			Node poiNode = (Node) this.xpathGetPoi.evaluate(doc, XPathConstants.NODE);
			if (poiNode != null) {
				PoiFile poiFile = new PoiFile();
				this.parseBasicFileInfo(poiFile, poiNode);
				mapBundle.setPoiFile(poiFile);
			}

			return mapBundle;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Reads all the default SourceFile attributes from a given node and stores them in a given
	 * SourceFile Object
	 * 
	 * @param sf
	 *            The SourceFile to store the attributes in
	 * @param node
	 *            The DOM-Node in which the attributes are stored
	 * @throws XPathExpressionException
	 *             if the xpath expression is malformed
	 * @throws NumberFormatException
	 *             if there are non-numeric values stored in numeric fields
	 */
	private void parseBasicFileInfo(SourceFile sf, Node node) throws XPathExpressionException,
			NumberFormatException {
		sf.setFilename((String) this.xpathGetFilename.evaluate(node, XPathConstants.STRING));
		sf.setFilesize(Long.parseLong((String) this.xpathGetFileSize.evaluate(node,
				XPathConstants.STRING)));
		sf.setCreated(new Date(Long.parseLong((String) this.xpathGetDate.evaluate(node,
				XPathConstants.STRING))));
		sf.setDescription((String) this.xpathGetDesc.evaluate(node, XPathConstants.STRING));
		sf.setMd5((String) this.xpathGetMD5.evaluate(node, XPathConstants.STRING));
		sf.setPath((String) this.xpathGetPath.evaluate(node, XPathConstants.STRING));
	}

	public ArrayList<MapBundle> getInstalledBundles() {
		if (this.installedBundles == null) {
			this.installedBundles = new ArrayList<MapBundle>();
			findBundleXML(this.baseDirectory);
		}
		return this.installedBundles;
	}

	public String getBaseDirectory() {
		return this.baseDirectory;
	}

	public MapBundle getSingleBundle(String path) {
		MapBundle bundle = buildMapBundle(loadXml("file:" + path), path);
		return bundle;
	}
}
