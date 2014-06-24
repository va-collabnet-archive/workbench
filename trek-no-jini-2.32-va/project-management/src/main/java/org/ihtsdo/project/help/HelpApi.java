/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.help;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * The Class HelpApi.
 */
public class HelpApi {

	/**
	 * Open help for component.
	 *
	 * @param componentId the component id
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static void openHelpForComponent(String componentId) throws IOException, URISyntaxException {
		URI url = getComponentURL(componentId);
		Desktop desktop = java.awt.Desktop.getDesktop();
		try {
			desktop.browse(url);
		} catch (IOException e) {
			desktop.open(new File(url.toString()));
		}
	}

	/**
	 * Gets the component url.
	 *
	 * @param componentId the component id
	 * @return the component url
	 * @throws FileNotFoundException the file not found exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static URI getComponentURL(String componentId) throws FileNotFoundException, IOException, URISyntaxException {
		URI result = null;
		Properties prop = new Properties();
		prop.load(new FileInputStream("config/project-help-config.properties"));

		String strUrl = prop.getProperty(componentId);
		result = new URI(strUrl);

		return result;
	}

}
