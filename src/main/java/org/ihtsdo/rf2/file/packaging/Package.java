package org.ihtsdo.rf2.file.packaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.file.packaging.mojo.RF2FilePackagingMojo;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Package {

	private static Logger logger = Logger.getLogger(Package.class.getName());
	
	public Package() {
		super();
	}
	
	public Document parseXmlFile(String path) {
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			InputStream is = Package.class.getResourceAsStream(path);
			
			// parse using builder to get DOM representation of the XML file
			dom = db.parse(is);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return dom;
	}

	public void parseDocument(Document dom, String source, String target) {
		try {

			Node node = dom.getFirstChild();
			processNode(node, source, target);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
	}

	private void processNode(Node node, String source, String path) throws Exception {
		NodeList childNodeList = node.getChildNodes();

		if (childNodeList != null) {
			for (int i = 0; i < childNodeList.getLength(); i++) {
				Node n = (Node) childNodeList.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					if (n.getNodeName().equals("folder")) {
						String folderName = getAttribute(n, "Name", "unknown");
						System.out.println("found folder: " + folderName);
						// logger.info("found folder: " + folderName);
						// create directory
						String newPath = path + "/" + folderName;
						createFolder(newPath);
						// make recursive call
						processNode(n, source, newPath);
					} else if (n.getNodeName().equals("file")) {
						String fileName = getAttribute(n, "Name", "unknown");
						System.out.println("found file: " + fileName);
						logger.info("found file: " + fileName);
						String src = source + "/" + fileName;
						String dst = path + "/" + fileName;
						copy(src, dst);
					}
				}
			}
		} else {
			System.out.println("child is not present");
			// logger.info("child is not present");
		}
	}

	private String getAttribute(Node node, String name, String defaultValue) {
		Node n = node.getAttributes().getNamedItem(name);
		return (n != null) ? (n.getNodeValue()) : (defaultValue);
	}

	private void createFolder(String targetPath) {
		boolean flag = false;
		// if ParentDir may not exist yet, you can use
		// mkDirs() instead and all directories will be created
		File targetDir = new File(targetPath);
		if (!targetDir.exists())
			flag = targetDir.mkdirs();
		if (flag) {
			logger.info("folder created succssfully: " + targetDir);
		}
	}

	// If the dst file does not exist, it is created
	public void copy(String src, String dst) throws IOException {
		InputStream in = new FileInputStream(new File(src));
		OutputStream out = new FileOutputStream(new File(dst));
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

}
