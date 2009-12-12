package org.ihtsdo.db.bdb.concept.component.description;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ManageConflict;
import org.dwfa.ace.api.I_MapNativeToNative;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.TimePathId;
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
	extends ConceptComponent<DescriptionPart> 
	implements I_DescriptionVersioned<DescriptionPart, Description, DescriptionTuple> {

	private static class DescTupleComputer extends
			TupleComputer<DescriptionTuple, Description, DescriptionPart> {

		public DescriptionTuple makeTuple(DescriptionPart part, Description core) {
			return new DescriptionTuple(core, part);
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
	}


	@Override
	public void readPartFromBdb(TupleInput input) {
		versions.add(new DescriptionPart(input));
	}

	@Override
	public void writeComponentToBdb(TupleOutput output) {
		// nothing to do...
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_Position viewPosition,
			List<DescriptionTuple> matchingTuples) {
		computer.addTuples(allowedStatus, viewPosition,
				matchingTuples, versions, this);
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<DescriptionTuple> matchingTuples,
			boolean addUncommitted) {
		computer.addTuples(allowedStatus, allowedTypes, positions,
				matchingTuples, addUncommitted, versions, this);
	}

	@Override
	public void addTuples(I_IntSet allowedStatus, I_IntSet allowedTypes,
			PositionSetReadOnly positions, List<DescriptionTuple> matchingTuples,
			boolean addUncommitted, boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<DescriptionTuple> tuples = new ArrayList<DescriptionTuple>();

		computer.addTuples(allowedStatus, allowedTypes, positions,
				tuples, addUncommitted, versions, this);

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
	public DescriptionTuple getFirstTuple() {
		return new DescriptionTuple(this, versions.get(0));
	}

	@Override
	public DescriptionTuple getLastTuple() {
		return new DescriptionTuple(this, versions.get(versions.size() - 1));
	}

	@Override
	public Set<TimePathId> getTimePathSet() {
		Set<TimePathId> set = new TreeSet<TimePathId>();
		for (DescriptionPart p : versions) {
			set.add(new TimePathId(p.getVersion(), p.getPathId()));
		}
		return set;
	}

	@Override
	public List<DescriptionTuple> getTuples() {
		List<DescriptionTuple> tuples = new ArrayList<DescriptionTuple>();
		for (DescriptionPart p : versions) {
			tuples.add(new DescriptionTuple(this, p));
		}
		return tuples;
	}

	@Override
	public List<DescriptionTuple> getTuples(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		List<DescriptionTuple> tuples = new ArrayList<DescriptionTuple>();
		for (DescriptionPart p : getVersions(returnConflictResolvedLatestState)) {
			tuples.add(new DescriptionTuple(this, p));
		}
		return tuples;
	}

	@Override
	public UniversalAceDescription getUniversal() throws IOException,
			TerminologyException {
		UniversalAceDescription universal = new UniversalAceDescription(
				getUids(nid), getUids(conceptNid), this.versionCount());
		for (DescriptionPart part : versions) {
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
	public List<DescriptionPart> getVersions(
			boolean returnConflictResolvedLatestState)
			throws TerminologyException, IOException {
		return versions;
	}

	@Override
	public boolean matches(Pattern p) {
		String lastText = null;
		for (DescriptionPart desc : versions) {
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
		HashSet<DescriptionPart> versionSet = 
			new HashSet<DescriptionPart>(versions);
		boolean changed = false;
		for (DescriptionPart part : jarDesc.getVersions()) {
			if (!versionSet.contains(part)) {
				changed = true;
				versions.add(part);
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
		   List<DescriptionTuple> matchingTuples = new ArrayList<DescriptionTuple>();
		   addTuples(allowedStatus, viewPosition, matchingTuples);
		   boolean promotedAnything = false;
		   for (I_Path promotionPath: pomotionPaths) {
			   for (DescriptionTuple dt: matchingTuples) {
				   if (dt.getPathId() == viewPathId) {
					   DescriptionPart promotionPart = 
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


}
