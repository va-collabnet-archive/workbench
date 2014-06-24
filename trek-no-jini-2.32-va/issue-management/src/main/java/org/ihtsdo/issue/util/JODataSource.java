/**
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
package org.ihtsdo.issue.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.activation.DataSource;


/**
 * The Class JODataSource.
 */
public class JODataSource implements DataSource {

	/** The object. */
	private Object object;
	
	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getContentType()
	 */
	@Override
	public String getContentType() {
		return "multipart/*";
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getName()
	 */
	@Override
	public String getName() {
		return "java.object";
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("Unsupported function");
	}

	/**
	 * Instantiates a new jO data source.
	 * 
	 * @param object the object
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public JODataSource(Object object)throws IOException
	{
	this.object=object;
	}

	/* (non-Javadoc)
	 * @see javax.activation.DataSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException
	{
	ByteArrayOutputStream output=new ByteArrayOutputStream();
	ObjectOutputStream objectStream=new ObjectOutputStream(output);
	objectStream.writeObject(object);
	objectStream.flush();

	ByteArrayInputStream result=new ByteArrayInputStream(output.toByteArray());

	return result;
	}

}
