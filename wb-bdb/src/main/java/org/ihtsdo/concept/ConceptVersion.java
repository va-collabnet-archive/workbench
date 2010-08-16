package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public class ConceptVersion implements ConceptVersionBI {
	
	private Concept concept;
	private Coordinate coordinate;

	public ConceptVersion(Concept concept, Coordinate coordinate) {
		super();
		this.concept = concept;
		this.coordinate = coordinate;
	}

	@Override
	public ConAttrVersionBI getConAttrs() throws IOException {
		return concept.getConceptAttributes();
	}

	@Override
	public ConAttrVersionBI getConAttrsActive() throws IOException, ContraditionException {
		return concept.getConceptAttributes().getVersion(coordinate);
	}

	@Override
	public ConceptChronicleBI getConceptChronicle() {
		return concept;
	}

	@Override
	public Coordinate getCoordinate() {
		return coordinate;
	}

	@Override
	public Collection<? extends DescriptionChronicleBI> getDescs()
			throws IOException {
		return concept.getDescriptions();
	}

	@Override
	public Collection<? extends DescriptionVersionBI> getDescsActive()
			throws IOException, ContraditionException {
		Collection<DescriptionVersionBI> returnValues = new ArrayList<DescriptionVersionBI>();
		for (DescriptionChronicleBI desc: getDescs()) {
			returnValues.addAll(desc.getVersions(coordinate));
		}
		return returnValues;
	}

	@Override
	public Collection<? extends MediaChronicleBI> getMedia() throws IOException {
		return concept.getImages();
	}

	@Override
	public Collection<? extends MediaVersionBI> getMediaActive()
			throws IOException, ContraditionException {
		Collection<MediaVersionBI> returnValues = new ArrayList<MediaVersionBI>();
		for (MediaChronicleBI media: getMedia()) {
			returnValues.addAll(media.getVersions(coordinate));
		}
		return returnValues;
	}

	@Override
	public Collection<? extends RelationshipChronicleBI> getRelsIncoming()
			throws IOException {
		return concept.getRelsIncoming();
	}

	@Override
	public Collection<? extends RelationshipVersionBI> getRelsIncomingActive()
			throws IOException, ContraditionException {
		Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			returnValues.addAll(rel.getVersions(coordinate));
		}
		return returnValues;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins()
			throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
				conceptSet.add(cv);
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(
			NidSetBI typeNids) throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				if (typeNids.contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive()
			throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
				conceptSet.add(cv);
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(
			NidSetBI typeNids) throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				if (typeNids.contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActiveIsa()
			throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsIsa()
			throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends RelationshipChronicleBI> getRelsOutgoing()
			throws IOException {
		return concept.getRelsOutgoing();
	}

	@Override
	public Collection<? extends RelationshipVersionBI> getRelsOutgoingActive()
			throws IOException, ContraditionException {
		Collection<RelationshipVersionBI> returnValues = new ArrayList<RelationshipVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			returnValues.addAll(rel.getVersions(coordinate));
		}
		return returnValues;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets()
			throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
				conceptSet.add(cv);
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargets(
			NidSetBI typeNids) throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				if (typeNids.contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive()
			throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
				conceptSet.add(cv);
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActive(
			NidSetBI typeNids) throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				if (typeNids.contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsActiveIsa()
			throws IOException, ContraditionException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsOutgoing()) {
			for (RelationshipVersionBI relv: rel.getVersions(coordinate)) {
				if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingTargetsIsa()
			throws IOException {
		HashSet<ConceptVersionBI> conceptSet = new HashSet<ConceptVersionBI>();
		for (RelationshipChronicleBI rel: getRelsIncoming()) {
			for (RelationshipVersionBI relv: rel.getVersions()) {
				if (coordinate.getIsaTypeNids().contains(relv.getTypeNid())) {
					ConceptVersionBI cv = Ts.get().getConceptVersion(coordinate, relv.getDestinationNid());
					conceptSet.add(cv);
				}
			}
		}
		return conceptSet;
	}

	@Override
	public int getNid() {
		return concept.getNid();
	}

	@Override
	public boolean isKindOf(ConceptVersionBI possibleKind) throws IOException {
		Concept possibleParent = ((ConceptVersion) possibleKind).concept;
		try {
			return possibleParent.isParentOfOrEqualTo(concept, coordinate.getAllowedStatusNids(), 
					coordinate.getIsaTypeNids(), coordinate.getPositionSet(), coordinate.getPrecedence(), coordinate.getContradictionManager());
		} catch (TerminologyException e) {
			throw new IOException(e);
		}
	}

}
