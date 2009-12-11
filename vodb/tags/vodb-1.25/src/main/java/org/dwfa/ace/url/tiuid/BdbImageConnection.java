/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.url.tiuid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.je.DatabaseException;

public class BdbImageConnection extends URLConnection {

	I_ImageVersioned image;
	protected BdbImageConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		try {
			String queryString = url.getQuery();
			if (queryString.startsWith("[")) {
				queryString = queryString.substring(1, queryString.length()-2);
				if (queryString.contains(",")) {
					String[] ids = queryString.split(",");
					for (String id: ids) {
						try {
							image = AceConfig.getVodb().getImage(UUID.fromString(id));
							if (image != null) {
								return;
							}
						} catch (RuntimeException ex) {
							AceLog.getAppLog().alertAndLogException(ex);
						} 
					}
				} else {
					image = AceConfig.getVodb().getImage(UUID.fromString(queryString));
				}
			} else {
                String id = url.getQuery();
                if (id.length() == 36) {
                    image = AceConfig.getVodb().getImage(UUID.fromString(url.getQuery()));
                } else {
                    image = AceConfig.getVodb().getImage(Integer.parseInt(id));
                }
			}
		} catch (DatabaseException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		} catch (TerminologyException e) {
			IOException ex = new IOException();
			ex.initCause(e);
			throw ex;
		}
	}

	@Override
	public String getContentType() {
		if (image == null) {
			try {
				connect();
			} catch (IOException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
				return null;
			}
		}
		
		return "image/" + image.getFormat(); //jpg png tiff ...
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (image == null) {
			connect();
		}
		ByteArrayInputStream stream = new ByteArrayInputStream(image.getImage());
		return stream;
	}

}
