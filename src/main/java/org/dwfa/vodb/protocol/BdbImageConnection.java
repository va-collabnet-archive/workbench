package org.dwfa.vodb.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.je.DatabaseException;

public class BdbImageConnection extends URLConnection {

	ThinImageVersioned image;
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
							image = AceConfig.vodb.getImage(UUID.fromString(id));
							if (image != null) {
								return;
							}
						} catch (RuntimeException e) {
							e.printStackTrace();
						} 
					}
				} else {
					image = AceConfig.vodb.getImage(UUID.fromString(queryString));
				}
			} else {
				image = AceConfig.vodb.getImage(UUID.fromString(url.getQuery()));
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
			} catch (IOException e) {
				e.printStackTrace();
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
