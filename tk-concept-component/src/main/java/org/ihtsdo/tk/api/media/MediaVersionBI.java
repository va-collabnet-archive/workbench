package org.ihtsdo.tk.api.media;

import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.TypedComponentVersionBI;

public interface MediaVersionBI extends TypedComponentVersionBI,
		MediaChronicleBI, AnalogGeneratorBI<MediaAnalogBI> {

    public byte[] getMedia();

    public String getTextDescription();

    public String getFormat();

    public int getConceptNid();

}
