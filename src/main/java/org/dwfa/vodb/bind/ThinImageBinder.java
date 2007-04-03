package org.dwfa.vodb.bind;

import java.util.ArrayList;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinImageBinder extends TupleBinding {

	public ThinImageVersioned entryToObject(TupleInput ti) {
		int nativeId = ti.readInt();
		int imageLength = ti.readInt();
		byte[] image = new byte[imageLength];
		ti.readFast(image, 0, imageLength);
		String format = ti.readString();
		int conceptId = ti.readInt();
		int size = ti.readInt();
		ThinImageVersioned versioned = new ThinImageVersioned(nativeId, image, 
				new ArrayList<I_ImagePart>(size), format, conceptId);
		for (int x = 0; x < size; x++) {
			ThinImagePart part = new ThinImagePart();
			part.setPathId(ti.readInt());
			part.setVersion(ti.readInt());
			part.setStatusId(ti.readInt());
			part.setTextDescription(ti.readString());
			part.setTypeId(ti.readInt());
			versioned.addVersion(part);
		}
		return versioned;
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		I_ImageVersioned versioned = (I_ImageVersioned) obj;
		to.writeInt(versioned.getImageId());
		byte[] image = versioned.getImage();
		to.writeInt(image.length);
		to.writeFast(image, 0, image.length);
		to.writeString(versioned.getFormat());
		to.writeInt(versioned.getConceptId());
		to.writeInt(versioned.getVersions().size());
		for (I_ImagePart part: versioned.getVersions()) {
			to.writeInt(part.getPathId());
			to.writeInt(part.getVersion());
			to.writeInt(part.getStatusId());
			to.writeString(part.getTextDescription());
			to.writeInt(part.getTypeId());
		}
	}
}
