package org.dwfa.queue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class DefaultObjectInputStreamCreator implements I_GetObjectInputStream {

	public DefaultObjectInputStreamCreator() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.queue.I_GetObjectInputStream#getObjectInputStream(java.io.InputStream)
	 */
	public ObjectInputStream getObjectInputStream(InputStream is) throws IOException {
		return new ObjectInputStream(is);
	}
}
