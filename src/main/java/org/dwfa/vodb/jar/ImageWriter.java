package org.dwfa.vodb.jar;

import java.io.OutputStream;

import org.dwfa.ace.config.AceConfig;
import org.dwfa.vodb.types.I_ProcessImages;

import com.sleepycat.je.DatabaseEntry;

public class ImageWriter extends TermWriter implements I_ProcessImages {

	public ImageWriter(OutputStream outStream) {
		super(outStream);
	}

	public void processImages(DatabaseEntry key, DatabaseEntry value) throws Exception {
		if (canceled) {
			throw new InterruptedException();
		}
		int size = value.getSize();
		dos.writeInt(size);
		dos.write(value.getData(), value.getOffset(), size);
		count++;
	}
	public Object call() throws Exception {
		AceConfig.vodb.iterateImages(this);
		dos.close();
		return null;
	}

}
