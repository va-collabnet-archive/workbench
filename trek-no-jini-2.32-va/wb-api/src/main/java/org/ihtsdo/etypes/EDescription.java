package org.ihtsdo.etypes;

import java.io.DataInput;
import java.io.IOException;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;

public class EDescription extends TkDescription {
    public static final long serialVersionUID = 1;


    public EDescription(DataInput in, int dataVersion) throws IOException, ClassNotFoundException {
        super(in, dataVersion);
    }

    public EDescription(I_DescriptionVersioned<?> desc) throws TerminologyException, IOException {
        super(desc);        
    }

    public EDescription() {
        super();
    }
}
