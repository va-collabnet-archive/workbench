package org.ihtsdo.concept;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.cern.colt.map.HashFunctions;
import org.ihtsdo.concept.component.ConceptComponent.Version;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


public class ConflictIdentifier {
    public enum CONTRADICTION_INVESTIGATION_TYPE {
        ATTRIBUTE, DESCRIPTION, RELATIONSHIP, REFSET;
    };

    public enum COMPONENT_TYPE {
    	ATTRIBUTE, DESCRIPTION, SOURCE_RELATIONSHIP, DESTINATION_RELATIONSHIP;
    }

    public enum CONTRADICTION_RESULT {
        NONE, SINGLE, UNREACHABLE, CONTRADICTION;
    };

	private static PositionBI leastCommonAncestorForViewPosition = null;
	private static PositionMapper conflictMapper = null;
	private ViewCoordinate coord = null;
	private I_ConfigAceFrame config;
		
    public ConflictIdentifier() {
		// Initialize the positions found for the concept
		Set<HashSet<PositionBI>> sortedOrigins = new HashSet<HashSet<PositionBI>>();
		Set<PositionBI> allOrigins = new HashSet<PositionBI>();
		PositionSetBI positions = null;

		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Failure to get Active Ace Frame", e);
        	return;
		}
			
		coord = config.getViewCoordinate();
		positions = config.getViewPositionSetReadOnly();
			
			for (PositionBI viewPosParent : positions) 
			{
				// Ignoring actual viewPos as that will always be leastCommonAncestor by definition
				for (PositionBI viewPos : viewPosParent.getPath().getOrigins())
				{
					HashSet<PositionBI> s = new HashSet<PositionBI>();
					s.add(viewPos);
					s.addAll(viewPos.getPath().getInheritedOrigins());
					sortedOrigins.add(s);
					allOrigins.addAll(s);
				}
			}
			// Filter out anything origin of LCA
			leastCommonAncestorForViewPosition = determineLeastCommonAncestor(sortedOrigins);
			
	    	Set<PositionBI> newSet = new HashSet<PositionBI>();
	    	
	    	for (PositionBI vPos : allOrigins) 
	    		if (!leastCommonAncestorForViewPosition.isSubsequentOrEqualTo(vPos))
	    			newSet.add(vPos);
	    	
    	newSet.addAll(config.getViewPositionSetReadOnly());
	    	newSet.add(leastCommonAncestorForViewPosition);
	    	
    	conflictMapper = Bdb.getSapDb().getMapper(config.getViewPositionSet().iterator().next());
	    	conflictMapper.queueForSetup();
	}
    
	public CONTRADICTION_RESULT inConflict(Concept concept) throws TerminologyException, IOException, ParseException 
	{
		boolean isUnreachable = false;
		boolean isSingle = false;
		Map<PositionForSet, HashMap<Integer,Version>> foundPositions = new HashMap<PositionForSet, HashMap<Integer,Version>>();
		CONTRADICTION_RESULT result = CONTRADICTION_RESULT.NONE;
		
		if (concept.getPrimUuid().equals(UUID.fromString("1ec915b4-8d80-3125-a711-c61e9cf7eb53")))
			isSingle = false;
		
		for (COMPONENT_TYPE type : COMPONENT_TYPE.values()) {
			if (result.equals(CONTRADICTION_RESULT.UNREACHABLE))
				isUnreachable = true;
			else if (result.equals(CONTRADICTION_RESULT.SINGLE) && !isUnreachable)
				isSingle = true;

			if (type == COMPONENT_TYPE.ATTRIBUTE)
				result = checkForComponentConflicts(concept, foundPositions, concept.getConAttrs(), new ConceptAttributeComparer());
			else
				result = checkMultipleComponentsForConflicts(concept, foundPositions, type);

			if (result.equals(CONTRADICTION_RESULT.CONTRADICTION))
				break;
		}
		
		if (result.equals(CONTRADICTION_RESULT.CONTRADICTION))
			return result;
		else if (isUnreachable)
			return CONTRADICTION_RESULT.UNREACHABLE;
		else if (isSingle)
			return CONTRADICTION_RESULT.SINGLE;
		else
			return CONTRADICTION_RESULT.NONE;
	}

	public CONTRADICTION_RESULT checkForComponentConflicts(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositions, ComponentChroncileBI<?> comp, AttributeComparer comparer)
	{
		CONTRADICTION_RESULT position = CONTRADICTION_RESULT.NONE;

		try
		{
			// REFSETS
			 position = refsetMembershipConflictFound(concept, foundPositions, comp.getNid(), comparer.getComponentType());

			if (position != CONTRADICTION_RESULT.CONTRADICTION && comp.getVersions().size() > 2)
			{
				CONTRADICTION_RESULT retVal = CONTRADICTION_RESULT.NONE;
				AttributeComparer secondaryComparer = comparer.getClass().newInstance();

				ComponentVersionBI leastCommonAncestorVersion = identifyLeastCommonAncestorVersion(comp.getVersions().iterator());
				comparer.initializeAttributes(leastCommonAncestorVersion);

				Iterator<?> versions = comp.getVersions().iterator();
				while(versions.hasNext())
				{
					ComponentVersionBI v = (ComponentVersionBI)versions.next();
					PositionBI p = Terms.get().newPosition(Terms.get().getPath(v.getPathNid()), ThinVersionHelper.convert(v.getTime()));

					if ((p.isSubsequentOrEqualTo(leastCommonAncestorForViewPosition)) && (!comparer.hasSameAttributes(v)))
					{
						if (!secondaryComparer.isInitialized()) {
							retVal = CONTRADICTION_RESULT.SINGLE;
							secondaryComparer.initializeAttributes(v);
						}
						else
						{
							if (!secondaryComparer.hasSameAttributes(v))
							{
								retVal = CONTRADICTION_RESULT.CONTRADICTION;
								break;
							}
						}
					}
				}

				updateFoundPositions(concept, foundPositions, comp.getVersions().iterator(), comp.getNid(), comparer.getComponentType());

				return retVal;
			} 
			else
			{
				CONTRADICTION_INVESTIGATION_TYPE compType = comparer.getComponentType();
				comparer.clear();
				return foundPositionsReachable(concept, foundPositions, comp.getVersions(), compType, comp.getNid(), 0);
			}
			
		} 
		catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in detecting contradictions for component: " + comp.toString(), e);
		}

		return CONTRADICTION_RESULT.CONTRADICTION;
	}
	
	@SuppressWarnings("unchecked")
	private  CONTRADICTION_RESULT foundPositionsReachable(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositions, Collection<? extends ComponentVersionBI> versionsOfComponent, CONTRADICTION_INVESTIGATION_TYPE compType, int componentNid, int memberNid) 
	{
		RELATIVE_POSITION retPosition = RELATIVE_POSITION.EQUAL;
		Version currentVersion = null;
		PositionForSet currentPos = null;
		boolean isUnreachable = false;
		boolean singleChangeFound = false;
		
		for (ComponentVersionBI version : versionsOfComponent)
		{
			boolean isSubstituted = false;
			PositionBI versionPosition = getVersionPosition(version);
			ViewCoordinate versionCoord = getVersionCoordinate(versionPosition);
			currentPos = normalizeLeastCommonAncestors(version, versionPosition);
			
			if (!(currentPos.getTime() == version.getTime() && currentPos.getPathNid() == version.getPathNid()))
				isSubstituted = true;

			String s = currentPos.toString();
			if (compType != CONTRADICTION_INVESTIGATION_TYPE.REFSET)
				currentVersion = getCurrentVersionByType(concept, compType, versionCoord, componentNid, memberNid);
			else
				currentVersion = getCurrentRefsetVersion(version, versionCoord);

			if (!foundPositions.containsKey(currentPos)) 
			{
					
				if (!isSubstituted)
				{
					// See if version is visible by all view Paths
					for (PositionForSet viewPos : foundPositions.keySet())
					{ 
						boolean matchingComponentNid = true;
						Version testVersion = foundPositions.get(viewPos).get(componentNid);
						if (testVersion == null) {
							testVersion = foundPositions.get(viewPos).values().iterator().next();
							matchingComponentNid = false;
						}
						
						try {
							retPosition = conflictMapper.relativePosition(currentVersion, testVersion);
						} catch (Exception e) {
			            	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in calling position mapper method", e);
						}
						
						if (retPosition.equals(RELATIVE_POSITION.CONTRADICTION))
						{ 
							if (matchingComponentNid)
							{
								return CONTRADICTION_RESULT.CONTRADICTION;
							}
							else
							{
								isUnreachable = true;
							}
						}
						else if (retPosition.equals(RELATIVE_POSITION.UNREACHABLE))
						{
							isUnreachable = true;
						}
						else
						{
							singleChangeFound = true;
						}
					}
				}
			}

			updateSingleFoundPosition(foundPositions, currentPos, componentNid, currentVersion);
		}
		
		if (isUnreachable)
			return CONTRADICTION_RESULT.UNREACHABLE;
		else if (singleChangeFound)
			return CONTRADICTION_RESULT.SINGLE;
		else 
			return CONTRADICTION_RESULT.NONE;
	}
	
	private CONTRADICTION_RESULT refsetMembershipConflictFound(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositions, int componentNid, CONTRADICTION_INVESTIGATION_TYPE compType) throws TerminologyException, IOException, ParseException 
	{
		List<? extends I_ExtendByRef> members = Terms.get().getAllExtensionsForComponent(componentNid);
		CONTRADICTION_RESULT position = CONTRADICTION_RESULT.NONE;
		boolean isUnreachable = false;
		boolean isSingle = false;

		for (I_ExtendByRef member : members)
		{
			if (member.getMutableParts().size() > 2) 
			{
				ComponentVersionBI leastCommonAncestorVersion = identifyLeastCommonAncestorVersion(member.getMutableParts().iterator());
				int leastCommonAncestorHashCode = getLeastCommonRefsetAncestorHashCode(leastCommonAncestorVersion, member);
				int secondaryHashCode = -1;
				
				for (I_ExtendByRefPart v : member.getMutableParts())
				{
					PositionBI p = Terms.get().newPosition(Terms.get().getPath(v.getPathNid()), ThinVersionHelper.convert(v.getTime()));
				
					if ((p.isSubsequentOrEqualTo(leastCommonAncestorForViewPosition)) && leastCommonAncestorHashCode != v.getPartsHashCode())
					{
						if (secondaryHashCode < 0)
						{
							position = CONTRADICTION_RESULT.SINGLE;
							secondaryHashCode = v.getPartsHashCode();
						}
						else
						{
							if (secondaryHashCode != v.getPartsHashCode())
							{
								position = CONTRADICTION_RESULT.CONTRADICTION;
								break;
							}
						}
					}
				}

				updateFoundPositions(concept, foundPositions, member.getMutableParts().iterator(), componentNid, compType);		
			} 
			else 
			{ 
				ConceptDataSimpleReference data = new ConceptDataSimpleReference(concept);
int ss = data.getRefsetMembers().size();
				int size = member.getMutableParts().size();
				RefsetMember a = concept.getRefsetMember(member.getMemberId());
				position = foundPositionsReachable(concept, foundPositions, member.getMutableParts(), CONTRADICTION_INVESTIGATION_TYPE.REFSET, componentNid, member.getMemberId());
			}

			if (position.equals(CONTRADICTION_RESULT.CONTRADICTION))
				return position;
			else if (position.equals(CONTRADICTION_RESULT.UNREACHABLE))
				isUnreachable = true;
			else if (position.equals(CONTRADICTION_RESULT.SINGLE))
				isSingle = true;
		}
		
		if (isUnreachable)
			return CONTRADICTION_RESULT.UNREACHABLE;
		else if (isSingle)
			return CONTRADICTION_RESULT.SINGLE;
		else
			return CONTRADICTION_RESULT.NONE;
	}

	private PositionBI determineLeastCommonAncestor(Set<HashSet<PositionBI>> originsByVersion)
	{
		int smallestSize = -1;
		Set<PositionBI> smallestSet = new HashSet<PositionBI>();
		
		for (HashSet<PositionBI> origins : originsByVersion)
		{
			// Identify smallest set
			if ((origins.size() < smallestSize) || (smallestSize < 0))
			{
				smallestSize = origins.size();
				smallestSet = origins;
			}
		}

		originsByVersion.remove(smallestSet);
		Set<PositionBI> testingAncestors = new HashSet<PositionBI>(); 

		for (PositionBI testingPos : smallestSet)
		{ 
			boolean success = true;
			for (HashSet<PositionBI> originSet : originsByVersion)
			{
				if (!originSet.contains(testingPos))
					success = false;
			}
			
			if (success)
				testingAncestors.add(testingPos);
		}

		PositionBI leastCommonAncestor = null;
		for (PositionBI ancestor : testingAncestors)
		{
			if (leastCommonAncestor == null || leastCommonAncestor.isAntecedentOrEqualTo(ancestor))
				leastCommonAncestor = ancestor;
		}
		
		return leastCommonAncestor;
	}


	public CONTRADICTION_RESULT checkMultipleComponentsForConflicts(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositions, COMPONENT_TYPE type) throws IOException
	{
		boolean isUnreachable = false;
		boolean isSingle = false;
		Collection<? extends ComponentChroncileBI<?>> collection = null;
		AttributeComparer comparer = null;
		 
		if (type == COMPONENT_TYPE.DESCRIPTION) {
			collection = concept.getDescs();
			comparer = new DescriptionAttributeComparer();
		} else {
			comparer = new RelationshipAttributeComparer();
			if (type == COMPONENT_TYPE.SOURCE_RELATIONSHIP) 
				collection = concept.getSourceRels();
			else if (type == COMPONENT_TYPE.DESTINATION_RELATIONSHIP)
				collection = concept.getDestRels();
		}

		for (ComponentChroncileBI<?> comp : collection)
		{
			CONTRADICTION_RESULT pos = checkForComponentConflicts(concept, foundPositions, comp, comparer);

			if (pos.equals(CONTRADICTION_RESULT.CONTRADICTION))
				return pos;
			else if (pos.equals(CONTRADICTION_RESULT.UNREACHABLE))
				isUnreachable = true;
			else if (pos.equals(CONTRADICTION_RESULT.SINGLE))
				isSingle = true;
		}
		
		if (isUnreachable)
			return CONTRADICTION_RESULT.UNREACHABLE;
		else if (isSingle)
			return CONTRADICTION_RESULT.SINGLE;
		else
			return CONTRADICTION_RESULT.NONE;
	}

	private ComponentVersionBI getLeastCommonAncestorVersion(Map<PositionBI, ComponentVersionBI> versions)
	{
		Set <PositionBI> keys = versions.keySet();
		PositionBI leastCommonAncestorPositionBI = null;

		// Find the lowest possible Path
		for (PositionBI key : keys)
		{
			if (leastCommonAncestorPositionBI == null)
				leastCommonAncestorPositionBI = key;
			else
			{
				if (key.isSubsequentOrEqualTo(leastCommonAncestorPositionBI))
					leastCommonAncestorPositionBI = key;
			}
		}
		
		return versions.get(leastCommonAncestorPositionBI);
	}

	private class PositionForSet {
		long time;
		int pathNid;

		public PositionForSet(long time, int pathNid) {
			super();
			this.time = time;
			this.pathNid = pathNid;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (PositionForSet.class.isAssignableFrom(obj.getClass())) {
				PositionForSet another = (PositionForSet) obj;
				return another.time == time && another.pathNid == pathNid;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return HashFunctions.hash(time + pathNid);
		}
		
		public String toString() {
			try {
				return "\nRunning contradiction detection on view path: " + Terms.get().getPath(pathNid).toString() + "With Time: " + time + "\n";
			} catch (Exception e) {
            	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in accessing view path: " + pathNid, e);
			}
			
			return "";
		}
		
		public long getTime() {
			return time;
		}
		
		public int getPathNid() {
			return pathNid;
		}
	}

	private Version getCurrentRefsetVersion(ComponentVersionBI version, ViewCoordinate versionCoord)
	{
		return null;		
	}

	@SuppressWarnings("unchecked")
	private Version getCurrentVersionByType(Concept concept, CONTRADICTION_INVESTIGATION_TYPE compType, ViewCoordinate versionCoord, int componentNid, int memberNid)
	{
		Version currentVersion = null;
		try
		{
			if (compType == CONTRADICTION_INVESTIGATION_TYPE.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				ConceptAttributes.Version currentAttributesVersion = attr.getVersion(versionCoord);
				currentVersion = (Version)currentAttributesVersion;
			}
			else if (compType == CONTRADICTION_INVESTIGATION_TYPE.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				Description.Version currentDescriptionVersion = desc.getVersion(versionCoord);
				currentVersion = (Version)currentDescriptionVersion;
			} 
			else if (compType == CONTRADICTION_INVESTIGATION_TYPE.DESCRIPTION)
			{
				Relationship.Version currentRelationshipVersion = null;
				Relationship rel = concept.getRelationship(componentNid);
	
				if (rel == null)
					currentRelationshipVersion = concept.getDestRel(componentNid).getVersion(versionCoord);
				else
					currentRelationshipVersion = rel.getVersion(versionCoord);
				
				currentVersion = (Version)currentRelationshipVersion;
			
				if (currentVersion == null)
				{
					 rel = concept.getRelationship(componentNid);
					currentRelationshipVersion = concept.getDestRel(componentNid).getVersion(versionCoord);
	
				}
			} 			
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in identifying version of type: " + compType + "for component: " + componentNid, e);
		}
		
		return currentVersion;
	}

	private PositionBI getVersionPosition(ComponentVersionBI version) {
		PositionBI coordPosition = null;
		
		try
		{
			coordPosition = Terms.get().newPosition(Terms.get().getPath(version.getPathNid()), ThinVersionHelper.convert(version.getTime()));
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in accessing path: " + version.getPathNid(), e);
		}
		
		return coordPosition;
	}
	
	private ViewCoordinate getVersionCoordinate(PositionBI versionPosition) {
		ViewCoordinate versionCoord = null;
		
		try {
			NidSetBI allowedStatusNids = coord.getAllowedStatusNids();
			
			// @TODO RETIRED?
			allowedStatusNids.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getPrimoridalUid()));
			versionCoord = config.getViewCoordinate();
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in accessing RETIRED concept", e);
		} 

		return versionCoord;
	}

	private void updateFoundPositions(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositions, Iterator<?> versions, int componentNid, CONTRADICTION_INVESTIGATION_TYPE compType) {
		
		while(versions.hasNext())
		{
			ComponentVersionBI v = (ComponentVersionBI)versions.next();
			
			PositionBI versionPosition = getVersionPosition(v);
			PositionForSet currentPos = normalizeLeastCommonAncestors(v, versionPosition);
			ViewCoordinate versionCoord = getVersionCoordinate(versionPosition);
			Version currentVersion = getCurrentVersionByType(concept, compType, versionCoord, componentNid, 0);

			updateSingleFoundPosition(foundPositions, currentPos, componentNid, currentVersion);
		}
	}

	private ComponentVersionBI identifyLeastCommonAncestorVersion(Iterator<?> versions) {
			Map<PositionBI, ComponentVersionBI> possibleVersions = new HashMap<PositionBI,ComponentVersionBI>();
		ComponentVersionBI v = null;
			
		try {
			while(versions.hasNext())
			{
				v = (ComponentVersionBI) versions.next();
				PositionBI p = Terms.get().newPosition(Terms.get().getPath(v.getPathNid()), ThinVersionHelper.convert(v.getTime()));
		
				if (leastCommonAncestorForViewPosition.isSubsequentOrEqualTo(p))
					possibleVersions.put(p, v);
			}
			
			return getLeastCommonAncestorVersion(possibleVersions);
		} catch (Exception e) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Error in accessing path: " + v.getPathNid(), e);
		}
		
		return null;
	}
	
	private void updateSingleFoundPosition(Map<PositionForSet, HashMap<Integer,Version>> foundPositions, PositionForSet currentPos, int componentNid, Version currentVersion) 
	{
		if (!foundPositions.containsKey(currentPos))
		{
			HashMap<Integer, Version> newPositionMap = new HashMap<Integer, Version>();
			newPositionMap.put(componentNid, currentVersion);
			foundPositions.put(currentPos, newPositionMap);
		}
		else
		{
			HashMap<Integer, Version> positionMap = foundPositions.get(currentPos);
			positionMap.put(componentNid, currentVersion);					
		}

	}

	private PositionForSet normalizeLeastCommonAncestors(ComponentVersionBI version, PositionBI versionPosition)
	{			
		PositionForSet currentPos = new PositionForSet(version.getTime(), version.getPathNid());
	
		if (versionPosition.isAntecedentOrEqualTo(leastCommonAncestorForViewPosition))
		{
			currentPos = new PositionForSet(version.getTime(), leastCommonAncestorForViewPosition.getPath().getConceptNid());
		}
		
		return currentPos;
	}

	private int getLeastCommonRefsetAncestorHashCode(ComponentVersionBI leastCommonAncestorVersion, I_ExtendByRef member)
	{
			for (I_ExtendByRefPart version: member.getMutableParts())
			{
				if (leastCommonAncestorVersion.getTime() == version.getTime() &&
					leastCommonAncestorVersion.getPathNid() == version.getPathNid())
				return version.getPartsHashCode();
		}
		
		return -1;
	}

}


