package org.dwfa.queue;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public interface I_GetObjectInputStream {

	public abstract ObjectInputStream getObjectInputStream(InputStream is)
			throws IOException;

}