package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;
import org.ihtsdo.tk.api.constraint.DescriptionConstraint;
import org.ihtsdo.tk.api.constraint.RelConstraint;
import org.ihtsdo.tk.api.constraint.RelConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelConstraintOutgoing;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations()
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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(
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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive()
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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(
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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActiveIsa()
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
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsIsa()
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

	@Override
	public String toString() {
		return "ConceptVersion coordinate: " + coordinate + 
			"\nConcept: " + concept.toString();
	}

	@Override
	public boolean satisfies(ConstraintBI constraint,
			ConstraintCheckType subjectCheck,
			ConstraintCheckType propertyCheck, 
			ConstraintCheckType valueCheck)
			throws IOException, ContraditionException {
		if (RelConstraintOutgoing.class.isAssignableFrom(constraint.getClass())) {
			return testRels(constraint, subjectCheck, propertyCheck,
					valueCheck, getRelsOutgoingActive());
		} else if (RelConstraintIncoming.class.isAssignableFrom(constraint.getClass())) {
			return testRels(constraint, subjectCheck, propertyCheck,
					valueCheck, getRelsIncomingActive());
		} else if (DescriptionConstraint.class.isAssignableFrom(constraint.getClass())) {
			DescriptionConstraint dc = (DescriptionConstraint) constraint;
			for (DescriptionVersionBI desc: getDescsActive()) {
				if (checkConceptVersionConstraint(desc.getConceptNid(), 
						dc.getConceptSpec(), subjectCheck) && 
					checkConceptVersionConstraint(desc.getTypeNid(), 
								dc.getDescTypeSpec(), propertyCheck) && 
								checkTextConstraint(desc.getText(), 
										dc.getText(), valueCheck)) {
					return true;
				}
			}
			return false;
		}
		throw new UnsupportedOperationException("Can't handle constraint of type: " + constraint);
	}

	private boolean testRels(ConstraintBI constraint,
			ConstraintCheckType subjectCheck,
			ConstraintCheckType propertyCheck, ConstraintCheckType valueCheck,
			Collection<? extends RelationshipVersionBI> rels)
			throws IOException {
		RelConstraint rc = (RelConstraint) constraint;
		for (RelationshipVersionBI rel: rels) {
			if (checkConceptVersionConstraint(rel.getOriginNid(), 
					rc.getOriginSpec(), subjectCheck) && 
				checkConceptVersionConstraint(rel.getTypeNid(), 
							rc.getRelTypeSpec(), propertyCheck) && 
							checkConceptVersionConstraint(rel.getDestinationNid(), 
									rc.getDestinationSpec(), valueCheck)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkConceptVersionConstraint(
			int cNid, ConceptSpec constraint,
			ConstraintCheckType checkType) throws IOException {
		
		switch (checkType) {
		case EQUALS:
			return Ts.get().getConceptVersion(coordinate, cNid).getNid() == constraint.get(coordinate).getNid();
		case IGNORE:
			return true;
		case KIND_OF:
			return Ts.get().getConceptVersion(coordinate, cNid).isKindOf(constraint.get(coordinate));
		default:
			throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
		}
	}

	private boolean checkTextConstraint(
			String text, String constraint,
			ConstraintCheckType checkType) {
		switch (checkType) {
		case EQUALS:
			return text.equals(constraint);
		case IGNORE:
			return true;
		case REGEX:
			Pattern pattern = 
	            Pattern.compile(constraint);
			Matcher matcher = 
	            pattern.matcher(text);
			return matcher.find();
		default:
			throw new UnsupportedOperationException("Illegal ConstraintCheckType: " + checkType);
		}
	}

	@Override
	public Collection<? extends DescriptionVersionBI> getDescsActive(int typeNid)
			throws IOException, ContraditionException {
		return getDescsActive(new IntSet(new int[] { typeNid }));
	}

	@Override
	public Collection<? extends DescriptionVersionBI> getDescsActive(
			NidSetBI typeNids) throws IOException, ContraditionException {
		Collection<DescriptionVersionBI> results = new ArrayList<DescriptionVersionBI>();
		for (DescriptionVersionBI d: getDescsActive()) {
			if (typeNids.contains(d.getTypeNid())) {
				results.add(d);
			}
		}
		return results;
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOrigins(
			int typeNid) throws IOException {
		return getRelsIncomingOrigins(new IntSet(new int[] { typeNid }));
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsIncomingOriginsActive(
			int typeNid) throws IOException, ContraditionException {
		return getRelsIncomingOriginsActive(new IntSet(new int[] { typeNid }));
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinations(
			int typeNid) throws IOException {
		return getRelsOutgoingDestinations(new IntSet(new int[] { typeNid }));
	}

	@Override
	public Collection<? extends ConceptVersionBI> getRelsOutgoingDestinationsActive(
			int typeNid) throws IOException, ContraditionException {
		return getRelsOutgoingDestinationsActive(new IntSet(new int[] { typeNid }));
	}

}
