package org.ihtsdo.project.help;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class HelpApi {

	public static void openHelpForComponent(String componentId) throws IOException, URISyntaxException {
		URI url = getComponentURL(componentId);
		Desktop desktop = java.awt.Desktop.getDesktop();
		try {
			desktop.browse(url);
		} catch (IOException e) {
			desktop.open(new File(url.toString()));
		}
	}

	public static URI getComponentURL(String componentId) throws FileNotFoundException, IOException, URISyntaxException {
		URI result = null;
		Properties prop = new Properties();
		prop.load(new FileInputStream("config/project-help-config.properties"));

		String strUrl = prop.getProperty(componentId);
		result = new URI(strUrl);

		return result;
	}

}
