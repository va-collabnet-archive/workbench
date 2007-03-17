package org.dwfa.vodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_ExtendLocally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescVersioned;

import com.sleepycat.je.DatabaseException;

public class VodbFixedServer implements I_StoreLocalFixedTerminology {

	VodbEnv server;

	public VodbFixedServer(VodbEnv server) {
		super();
		this.server = server;
	}

	public Collection<I_ConceptualizeLocally> doConceptSearch(String[] words)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_ConceptualizeLocally> doConceptSearch(List<String> words)
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_DescribeConceptLocally> doDescriptionSearch(
			String[] words) throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_DescribeConceptLocally> doDescriptionSearch(
			List<String> words) throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_RelateConceptsLocally> getDestRels(
			I_ConceptualizeLocally dest) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public I_ExtendLocally getExtension(I_ManifestLocally component,
			I_ConceptualizeLocally extensionType) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_ConceptualizeLocally> getExtensionTypes()
			throws IOException, TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public I_RelateConceptsLocally getRel(int relNid) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_RelateConceptsLocally> getSourceRels(
			I_ConceptualizeLocally source) throws IOException,
			TerminologyException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public Collection<I_ConceptualizeLocally> getRoots() throws IOException,
			TerminologyException {
		throw new UnsupportedOperationException();
	}

	public I_DescribeConceptLocally getDescription(int descriptionNid)
			throws IOException, TerminologyException {
		try {
			ThinDescVersioned vDesc = server.getDescription(descriptionNid);
			return vDesc.toLocalFixedDesc();
		} catch (DatabaseException e) {
			throw new DbToIoException(e);
		}
	}

	public Collection<I_DescribeConceptLocally> getDescriptionsForConcept(
			I_ConceptualizeLocally concept) throws IOException,
			TerminologyException {
		List<I_DescribeConceptLocally> descList = new ArrayList<I_DescribeConceptLocally>();
		try {
			for (ThinDescVersioned desc : server.getDescriptions(concept
					.getNid())) {
				descList.add(desc.toLocalFixedDesc());
			}
		} catch (DatabaseException e) {
			throw new DbToIoException(e);
		}
		return descList;
	}

	public I_ConceptualizeLocally getConcept(int conceptNid)
			throws IOException, TerminologyException {
		try {
			ThinConVersioned vCon = server.getConcept(conceptNid);
			return vCon.getLocalFixedConcept();
		} catch (DatabaseException e) {
			throw new DbToIoException(e);
		}
	}

	public int getNid(UUID uid) throws IOException, TerminologyException {
		return server.uuidToNative(uid);
	}

	public int getNid(Collection<UUID> uids) throws IOException,
			TerminologyException {
		return server.uuidToNative(uids);
	}

	public Collection<UUID> getUids(int nid) throws IOException,
			TerminologyException {
		try {
			return server.nativeToUuid(nid);
		} catch (DatabaseException e) {
			throw new DbToIoException(e);
		}
	}

}
