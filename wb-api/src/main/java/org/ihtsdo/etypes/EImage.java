package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;

public class EImage extends TkMedia {

    public static final long serialVersionUID = 1;

    public EImage(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EImage(I_ImageVersioned<?> imageVer) throws TerminologyException, IOException {
        super(imageVer);
    }

    public EImage() {
        super();
    }
}
