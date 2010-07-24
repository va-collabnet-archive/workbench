package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.concept.component.media.TkMedia;
import org.ihtsdo.tk.concept.component.media.TkMediaRevision;

public class EImage extends TkMedia {

    public static final long serialVersionUID = 1;

    public EImage(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EImage(I_ImageVersioned imageVer) throws TerminologyException, IOException {
        EConcept.convertId(Terms.get().getId(imageVer.getNid()), this);
        int partCount = imageVer.getMutableParts().size();
        I_ImagePart part = imageVer.getMutableParts().get(0);
        conceptUuid = nidToUuid(imageVer.getConceptId());
        format = imageVer.getFormat();
        image = imageVer.getImage();
        textDescription = part.getTextDescription();
        typeUuid = nidToUuid(part.getTypeId());
        pathUuid = nidToUuid(part.getPathId());
        statusUuid = nidToUuid(part.getStatusId());
        time = part.getTime();
        if (partCount > 1) {
            revisions = new ArrayList<TkMediaRevision>(partCount - 1);
            for (int i = 1; i < partCount; i++) {
                revisions.add(new EImageRevision(imageVer.getMutableParts().get(i)));
            }
        }
    }

    public EImage() {
        super();
    }
}
