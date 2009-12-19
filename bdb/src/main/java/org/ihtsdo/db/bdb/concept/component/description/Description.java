package org.ihtsdo.db.bdb.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.utypes.UniversalAceDescription;
import org.dwfa.ace.utypes.UniversalAceDescriptionPart;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.conflict.IdentifyAllConflictStrategy;
import org.ihtsdo.db.bdb.concept.component.ConceptComponent;
import org.ihtsdo.db.util.TupleComputer;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class Description 
	extends ConceptComponent<DescriptionVariablePart> 
	implements I_DescriptionVersioned<DescriptionVariablePart, DescriptionVersion> {

	private static class DescTupleComputer extends
			TupleComputer<DescriptionVersion, Description, DescriptionVariablePart> {

		public DescriptionVersion makeTuple(DescriptionVariablePart part, Description core) {
			return new DescriptionVersion(core, part);
		}
	}

	private static DescTupleComputer computer = new DescTupleComputer();
	private int conceptNid;
	
	protected Description(int nid,
			int versionCount, boolean editable) {
		super(nid, versionCount, editable);
	}

	@Override
	public void readComponentFromBdb(TupleInput input, int conceptNid) {
		this.conceptNid = conceptNid;
		short partSize = input.readShort();
		for (int i = 0; i < partSize; i++) {
			variableParts.add(new DescriptionVariablePart(input));
		}
	}

	@Override
	public void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
		List<DescriptionVariablePart> partsToWrite = new ArrayList<DescriptionVariablePart>();
		for (DescriptionVariablePart p: variableParts) {
			if (p.getStatusAtPositionNid() > maxReadOnlyStatusAtPositionNid) {
				partsToWrite.add(p);
			}
		}
		output.writeShort(partsToWrite.size());
		for (DescriptionVariablePart p: partsToWrite) {
			p.writePartToBdb(output);
		}

	}

	public final List<DescriptionVersion> getTuples() {
		List<DescriptionVersion> tuples = new ArrayList<DescriptionVersion>();
		for (DescriptionVariablePart p : variableParts) {
			tuples.add(new DescriptionVersion(this, p));
		}
		return tuples;
	}

	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<DescriptionVersion> matchingTuples) {
		computer.addTuples(allowedStatus, viewPosition,
				matchingTuples, variableParts, this);
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<DescriptionVersion> matchingTuples,
			boolean addUncommitted) {
		computer.addTuples(allowedStatus, allowedTypes, positions,
				matchingTuples, addUncommitted, variableParts, this);
	}

	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<DescriptionVersion> matchingTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<DescriptionVersion> tuples = new ArrayList<DescriptionVersion>();

		computer.addTuples(allowedStatus, allowedTypes, positions,
				tuples, addUncommitted, variableParts, this);

		if (returnConflictResolvedLatestState) {
			I_ConfigAceFrame config = AceConfig.getVodb()
					.getActiveAceFrameConfig();
			I_ManageConflict conflictResolutionStrategy;
			if (config == null) {
				conflictResolutionStrategy = new IdentifyAllConflictStrategy();
			} else {
				conflictResolutionStrategy = config
						.getConflictResolutionStrategy();
			}

			tuples = conflictResolutionStrategy.resolveTuples(tuples);
		}
		matchingTuples.addAll(tuples);
	}

	@Override
	public void convertIds(I_MapNativeToNative jarToDbNativeMap) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getConceptId() {
		return conceptNid;
	}

	@Override
	public int getDescId() {
		return nid;
	}

	@Override
	public DescriptionVersion getFirstTuple() {
		return new DescriptionVersion(this, variableParts.get(0));
	}

	@Override
	public DescriptionVersion getLastTuple() {
		return new DescriptionVersion(this, variableParts.get(variableParts.size() - 1));
	}


	@Override
	public List<DescriptionVersion> getTuples(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<DescriptionVersion> tuples = new ArrayList<DescriptionVersion>();
		for (DescriptionVariablePart p : getVersions(returnConflictResolvedLatestState)) {
			tuples.add(new DescriptionVersion(this, p));
		}
		return tuples;
	}

	@Override
	public UniversalAceDescription getUniversal() throws IOException,
			TerminologyException {
		UniversalAceDescription universal = new UniversalAceDescription(
				getUids(nid), getUids(conceptNid), this.versionCount());
		for (DescriptionVariablePart part : variableParts) {
			UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
			universalPart.setInitialCaseSignificant(part
					.getInitialCaseSignificant());
			universalPart.setLang(part.getLang());
			universalPart.setPathId(getUids(part.getPathId()));
			universalPart.setStatusId(getUids(part.getStatusId()));
			universalPart.setText(part.getText());
			universalPart.setTypeId(getUids(part.getTypeId()));
			universalPart.setTime(ThinVersionHelper.convert(part.getVersion()));
			universal.addVersion(universalPart);
		}
		return universal;
	}

	@Override
	public List<DescriptionVariablePart> getVersions(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return variableParts;
	}

	@Override
	public boolean matches(Pattern p) {
		String lastText = null;
		for (DescriptionVariablePart desc : variableParts) {
			if (desc.getText() != lastText) {
				lastText = desc.getText();
				Matcher m = p.matcher(lastText);
				if (m.find()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean merge(Description jarDesc) {
		HashSet<DescriptionVariablePart> versionSet = 
			new HashSet<DescriptionVariablePart>(variableParts);
		boolean changed = false;
		for (DescriptionVariablePart part : jarDesc.getVersions()) {
			if (!versionSet.contains(part)) {
				changed = true;
				variableParts.add(part);
			}
		}
		return changed;
	}

	@Override
	public I_DescribeConceptLocally toLocalFixedDesc() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean promote(I_Position viewPosition, PathSetReadOnly pomotionPaths,
			I_IntSet allowedStatus) throws IOException, TerminologyException {
		   int viewPathId = viewPosition.getPath().getConceptId();
		   List<DescriptionVersion> matchingTuples = new ArrayList<DescriptionVersion>();
		   addTuples(allowedStatus, viewPosition, matchingTuples);
		   boolean promotedAnything = false;
		   for (I_Path promotionPath: pomotionPaths) {
			   for (DescriptionVersion dt: matchingTuples) {
				   if (dt.getPathId() == viewPathId) {
					   DescriptionVariablePart promotionPart = 
						   dt.getPart().makeAnalog(dt.getStatusId(), 
								   promotionPath.getConceptId(), 
								   Long.MAX_VALUE);
					   // TODO add a check to see if already promoted?
					   dt.getDescVersioned().addVersion(promotionPart);
					   promotedAnything = true;
				   }
			   }
		   }
		   return promotedAnything;
	}

	private static Collection<UUID> getUids(int id) throws IOException,
			TerminologyException {
		return LocalFixedTerminology.getStore().getUids(id);
	}

	@Override
	public boolean addVersion(I_DescriptionPart newPart) {
		return variableParts.add((DescriptionVariablePart) newPart);
	}

	@Override
	public boolean merge(
			I_DescriptionVersioned<DescriptionVariablePart, DescriptionVersion> jarDesc) {
		throw new UnsupportedOperationException();
	}


}
