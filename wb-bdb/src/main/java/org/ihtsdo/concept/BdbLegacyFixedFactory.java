package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.exceptions.ToIoException;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ComponentChroncileBI;

import com.sleepycat.je.DatabaseException;

public class BdbLegacyFixedFactory implements I_StoreLocalFixedTerminology {


    public BdbLegacyFixedFactory() {
        super();
    }

    public Collection<I_ConceptualizeLocally> doConceptSearch(String[] words) throws IOException, TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_ConceptualizeLocally> doConceptSearch(List<String> words) throws IOException,
            TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_DescribeConceptLocally> doDescriptionSearch(String[] words) throws IOException,
            TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_DescribeConceptLocally> doDescriptionSearch(List<String> words) throws IOException,
            TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_RelateConceptsLocally> getDestRels(I_ConceptualizeLocally dest) throws IOException,
            TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public I_ExtendLocally getExtension(I_ManifestLocally component, I_ConceptualizeLocally extensionType)
            throws IOException, TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_ConceptualizeLocally> getExtensionTypes() throws IOException, TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public I_RelateConceptsLocally getRel(int relNid) throws IOException, TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_RelateConceptsLocally> getSourceRels(I_ConceptualizeLocally source) throws IOException,
            TerminologyException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public Collection<I_ConceptualizeLocally> getRoots() throws IOException, TerminologyException {
        throw new UnsupportedOperationException();
    }

    public I_DescribeConceptLocally getDescription(int descriptionNid, int concNid) throws IOException,
            TerminologyException {
        I_DescriptionVersioned vDesc = (I_DescriptionVersioned) Bdb.getComponent(descriptionNid);
        return vDesc.toLocalFixedDesc();
    }

    public Collection<I_DescribeConceptLocally> getDescriptionsForConcept(I_ConceptualizeLocally concept)
            throws IOException, TerminologyException {
        List<I_DescribeConceptLocally> descList = new ArrayList<I_DescribeConceptLocally>();
        Concept c = Bdb.getConceptDb().getConcept(concept.getNid());
        try {
            for (I_DescriptionVersioned desc : c.getDescriptions()) {
                descList.add(desc.toLocalFixedDesc());
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
        return descList;
    }

    public I_ConceptualizeLocally getConcept(int cNid) throws IOException, TerminologyException {
        I_ConceptAttributeVersioned vCon = Bdb.getConceptDb().getConcept(cNid).getConceptAttributes();
        return vCon.getLocalFixedConcept();
    }

    public int getNid(UUID uid) throws IOException, TerminologyException {
        return Bdb.uuidToNid(uid);
    }

    public int getNid(Collection<UUID> uids) throws IOException, TerminologyException {
        return Bdb.uuidsToNid(uids);
    }

    public Collection<UUID> getUids(int nid) throws IOException, TerminologyException {
        try {
        	int cNid = Bdb.getConceptNid(nid);
        	Concept c = Bdb.getConceptDb().getConcept(cNid);
        	if (cNid == nid) {
        		return c.getUUIDs();
        	}
        	ComponentChroncileBI<?> cc = c.getComponent(nid);
            return cc.getUUIDs();
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        }
    }

	@Override
	public Collection<I_ConceptualizeLocally> getConcepts() throws IOException,
			TerminologyException {
		throw new UnsupportedOperationException();
	}

}
