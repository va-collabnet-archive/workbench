package org.dwfa.ace.task.search.refset;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;

@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class RefsetContainsConcept extends AbstractSearchTest {
    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    /**
     * refset concept the component must be a member of.
     */
    private TermEntry conceptInRefset = new TermEntry(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.conceptInRefset);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.conceptInRefset = (TermEntry) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

	@Override
	public boolean test(I_AmTermComponent component,
			I_ConfigAceFrame frameConfig) throws TaskFailedException {
		try {
			if (I_ExtendByRefVersion.class.isAssignableFrom(component.getClass())) {
				I_ExtendByRefVersion extV = (I_ExtendByRefVersion) component;
				I_ExtendByRefPart part = extV.getMutablePart();
				boolean found = false;
				int matchNid = Ts.get().getConcept(conceptInRefset.ids).getNid();
				if (I_ExtendByRefPartCid.class.isAssignableFrom(part.getClass())) {
					found = ((I_ExtendByRefPartCid) part).getC1id() == matchNid;
					if (found) {
						return true;
					}
				}
				if (I_ExtendByRefPartCidCid.class.isAssignableFrom(part.getClass())) {
					found = ((I_ExtendByRefPartCidCid) part).getC2id() == matchNid;
					if (found) {
						return true;
					}
				}
				if (I_ExtendByRefPartCidCidCid.class.isAssignableFrom(part.getClass())) {
					found = ((I_ExtendByRefPartCidCidCid) part).getC3id() == matchNid;
					if (found) {
						return true;
					}
				}
			}
			return false;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		}
	}

	public TermEntry getConceptInRefset() {
		return conceptInRefset;
	}

	public void setConceptInRefset(TermEntry conceptInRefset) {
		this.conceptInRefset = conceptInRefset;
	}

}
