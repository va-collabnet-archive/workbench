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
import java.util.logging.Level;

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
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;


public class ContradictionIdentifier {
    public enum CONTRADICTION_INVESTIGATION_TYPE {
        ATTRIBUTE, DESCRIPTION, RELATIONSHIP, REFSET;
    };

    public enum COMPONENT_TYPE {
    	ATTRIBUTE, DESCRIPTION, SOURCE_RELATIONSHIP, DESTINATION_RELATIONSHIP;
    }

    public enum CONTRADICTION_RESULT {
        NONE, SINGLE, UNREACHABLE, CONTRADICTION, ERROR;
    };

	// Simple inner-class to store the position for the "Found Positions" test
	private class PositionForSet 
	{
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
            	AceLog.getAppLog().log(Level.WARNING, "Error in accessing view path: " + pathNid, e);
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

	// Class Variables
	private static PositionBI leastCommonAncestorForViewPosition = null;
	private static PositionMapper conflictMapper = null;
		
    public ContradictionIdentifier() 
    {
		try 
		{
			// Initialize View Coordinate with RETIRED status
			NidSetBI allowedStatusNids = Terms.get().getActiveAceFrameConfig().getViewCoordinate().getAllowedStatusNids();
			allowedStatusNids.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getPrimoridalUid()));
		
			// Initialize the PositionMapper used for identifying contradictions
	    	conflictMapper = Bdb.getSapDb().getMapper(Terms.get().getActiveAceFrameConfig().getViewPositionSet().iterator().next());
		    conflictMapper.queueForSetup();
			
			// Identify leastCommonAncestor of View Position from Origins of view position 
			Set<HashSet<PositionBI>> sortedOrigins = new HashSet<HashSet<PositionBI>>();
			for (PositionBI viewPosParent : Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly()) 
			{
				// Ignoring actual viewPos as that will always be leastCommonAncestor by definition
				for (PositionBI viewPos : viewPosParent.getPath().getOrigins())
				{
					HashSet<PositionBI> s = new HashSet<PositionBI>();
					s.add(viewPos);
					s.addAll(viewPos.getPath().getInheritedOrigins());
					sortedOrigins.add(s);
				}
			}
	
			leastCommonAncestorForViewPosition = determineLeastCommonAncestor(sortedOrigins);
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Failure to get Active Ace Frame", e);
        	return;
		}
	}
    
    // For a given concept, look at a set of components at a time.
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type
	public CONTRADICTION_RESULT inConflict(Concept concept) throws TerminologyException, IOException, ParseException 
	{
		boolean isSingle = false;
		CONTRADICTION_RESULT result = CONTRADICTION_RESULT.NONE;
		Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap = new HashMap<PositionForSet, HashMap<Integer,Version>>();
		
		for (COMPONENT_TYPE type : COMPONENT_TYPE.values()) 
		{
			if (type == COMPONENT_TYPE.ATTRIBUTE)
				result = checkForComponentConflicts(concept, foundPositionsMap, concept.getConAttrs(), new ConceptAttributeComparer());
			else
				result = checkMultipleComponentsForConflicts(concept, foundPositionsMap, type);

			if (result.equals(CONTRADICTION_RESULT.CONTRADICTION))
				break;
			else if (result.equals(CONTRADICTION_RESULT.SINGLE))
				isSingle = true;
		}
		
		if (result.equals(CONTRADICTION_RESULT.CONTRADICTION))
			return result;
		else if (isSingle || result.equals(CONTRADICTION_RESULT.SINGLE))
			return CONTRADICTION_RESULT.SINGLE;
		else
			return CONTRADICTION_RESULT.NONE;
	}
	
	// For Multiple Tuples Components (all but concept attributes)
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type	
	public CONTRADICTION_RESULT checkMultipleComponentsForConflicts(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, COMPONENT_TYPE type) throws IOException
	{
		boolean isSingle = false;
		AttributeComparer comparer = null;
		Collection<? extends ComponentChroncileBI<?>> componentTuples = null;
		 
		// Capture collection of tuples and create the comparator type
		if (type == COMPONENT_TYPE.DESCRIPTION)
		{
			componentTuples = concept.getDescs();
			comparer = new DescriptionAttributeComparer();
		} 
		else 
		{
			comparer = new RelationshipAttributeComparer();
			
			if (type == COMPONENT_TYPE.SOURCE_RELATIONSHIP) 
			{
				componentTuples = concept.getSourceRels();
			} 
			else if (type == COMPONENT_TYPE.DESTINATION_RELATIONSHIP) 
			{
				componentTuples = concept.getDestRels();
			}
		}

		// Search for conflict or single change on each tuple
		for (ComponentChroncileBI<?> tuple : componentTuples)
		{
			CONTRADICTION_RESULT pos = checkForComponentConflicts(concept, foundPositionsMap, tuple, comparer);

			if (pos.equals(CONTRADICTION_RESULT.CONTRADICTION))
				return pos;
			else if (pos.equals(CONTRADICTION_RESULT.SINGLE))
				isSingle = true;
				
			comparer.clear();
		}
		
		if (isSingle)
			return CONTRADICTION_RESULT.SINGLE;
		else
			return CONTRADICTION_RESULT.NONE;
	}

	// For a given concept's componentId, see if the component has contradiction
	public CONTRADICTION_RESULT checkForComponentConflicts(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, ComponentChroncileBI<?> comp, AttributeComparer comparer)
	{
		boolean isSingle = false;
		boolean isContradiction = false;
		CONTRADICTION_INVESTIGATION_TYPE compType = comparer.getComponentType();

		try
		{
			// Examine Refsets.  Only continue to concept's components if refset check doesn't identify contradiction
//			CONTRADICTION_RESULT refsetResult = refsetMembershipConflictFound(concept, foundPositionsMap, comp.getNid(), compType);
			CONTRADICTION_RESULT refsetResult = CONTRADICTION_RESULT.NONE;
			
			// If there are multiple versions in addition to the common ancestor, compare versions
			if (refsetResult != CONTRADICTION_RESULT.CONTRADICTION && comp.getVersions().size() > 2)
			{
				// Identify the version that is the least common ancestor to all versions
				ComponentVersionBI commonAncestorVersion = identifyLeastCommonAncestorVersion(comp.getVersions().iterator());
				
				// Initialize comparers
				AttributeComparer secondaryComparer = comparer.getClass().newInstance();
				comparer.initializeAttributes(commonAncestorVersion);

				// Iterate through all versions
				Iterator<?> versions = comp.getVersions().iterator();
				while(versions.hasNext())
				{
					ComponentVersionBI v = (ComponentVersionBI)versions.next();
					PositionBI p = Terms.get().newPosition(Terms.get().getPath(v.getPathNid()), ThinVersionHelper.convert(v.getTime()));

					// Only compare if 
					// a) the version's position is subsequent or equal to the lca of the View Position and
					// b) the values between the current version is different than the least common ancestor's version
					if ((p.isSubsequentOrEqualTo(leastCommonAncestorForViewPosition)) && (!comparer.hasSameAttributes(v)))
					{
						// If first time here, isSingle is true, so setup comparer to check for contradiction with rest of versions
						if (!secondaryComparer.isInitialized()) {
							isSingle = true;
							secondaryComparer.initializeAttributes(v);
						}
						else
						{
							// Check if there exists a third unique version of this component, if so, contradiction found
							if (!secondaryComparer.hasSameAttributes(v))
							{
								isContradiction = true;
								break;
							}
						}
					}
				} 
			} 
			
			
			if (isContradiction)
			{
				// Concept contains contradiction.  No need to update positions as check complete for concept
				return CONTRADICTION_RESULT.CONTRADICTION;
			}
			else
			{
				// Either a single change or no contradiction found, check that all positions are reachable
				CONTRADICTION_RESULT retVal = areFoundPositionsReachable(concept, foundPositionsMap, comp.getVersions(), compType, comp.getNid());
				
				if (retVal == CONTRADICTION_RESULT.CONTRADICTION)
					return retVal;
				else if (retVal == CONTRADICTION_RESULT.SINGLE || isSingle)
					return CONTRADICTION_RESULT.SINGLE;
			}
		} 
		catch (Exception e) 
		{
        	AceLog.getAppLog().log(Level.WARNING, "Error in detecting contradictions for component: " + comp.toString(), e);
        	return CONTRADICTION_RESULT.ERROR;
		}

		return CONTRADICTION_RESULT.NONE;
	}
	
	// For each version of a given component, ensure that a contradiction doesn't exist with another version of the same component (view PositionMapper)
	private  CONTRADICTION_RESULT areFoundPositionsReachable(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, Collection<? extends ComponentVersionBI> versionsOfComponent, CONTRADICTION_INVESTIGATION_TYPE compType, int componentNid) 
	{
		RELATIVE_POSITION retPosition = RELATIVE_POSITION.EQUAL;
		Version currentVersion = null;
		PositionForSet currentPos = null;
		boolean isUnreachable = false;
		boolean singleChangeFound = false;

		// For each version of the component
		for (ComponentVersionBI version : versionsOfComponent)
		{
			boolean isLCAPath = false;
			
			// Identify PositionForSet of version
			PositionBI versionPosition = getVersionPosition(version);
			currentPos = normalizeLeastCommonAncestors(version, versionPosition);
			
			// If used LCA's path, save information
			if (currentPos.getPathNid() != version.getPathNid())
				isLCAPath = true;
			
			try 
			{
				// get ConceptComponent.Version object of componentNid's version
				if (compType == CONTRADICTION_INVESTIGATION_TYPE.REFSET)
					currentVersion = getCurrentRefsetVersion(version);
				else
					currentVersion = getCurrentVersionByType(concept, compType, componentNid);
			} catch (ContraditionException ce) {
				// Multiple versions on versionCoord found where should exist one.  
				// Therefore, exception indicates a contradiction
				return CONTRADICTION_RESULT.CONTRADICTION;
			}

			// If first time seeing position and position is not based on the LCAPath, 
			// compare version's PositionForSet to all previously Found Positions via PositionMapper to identify contradiction's
			if (!foundPositionsMap.containsKey(currentPos) && (!isLCAPath))
			{
				// See if version is visible by all view Paths
				for (PositionForSet viewPos : foundPositionsMap.keySet())
				{ 
					// TODO: Why test for null?  Necessary?
					Version testVersion = foundPositionsMap.get(viewPos).get(componentNid);

					if (testVersion == null) {
						testVersion = foundPositionsMap.get(viewPos).values().iterator().next();
					}
					
					try 
					{
						// Test between two versions with this basic (path/time) precedence check
						retPosition = conflictMapper.fastRelativePosition(currentVersion, testVersion, Terms.get().getActiveAceFrameConfig().getPrecedence());
					} catch (Exception e) {
		            	AceLog.getAppLog().log(Level.WARNING, "Error in calling position mapper method", e);
					}
					
					if (retPosition.equals(RELATIVE_POSITION.CONTRADICTION))
					{ 
						return CONTRADICTION_RESULT.CONTRADICTION;
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

			// Add Version to Found Positions
			updateFoundPositionWithVersion(foundPositionsMap, currentPos, componentNid, currentVersion);
		}
		
		if (isUnreachable)
			return CONTRADICTION_RESULT.UNREACHABLE;
		else if (singleChangeFound)
			return CONTRADICTION_RESULT.SINGLE;
		else 
			return CONTRADICTION_RESULT.NONE;
	}
	
	// For set of origins, identify origin (by position) that is the least common ancestor of the other origins
	// ie, the version by which to identify original version of concept
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

	// identify the version of a component that is the least common ancestor of all versions
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

	// For a component Type & Nid, get the version corresponding to the View Coordinate 
	private Version getCurrentVersionByType(Concept concept, CONTRADICTION_INVESTIGATION_TYPE compType, int componentNid) throws ContraditionException
	{
		Version currentVersion = null;
		
		try
		{
			ViewCoordinate viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
		
			if (compType == CONTRADICTION_INVESTIGATION_TYPE.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				ConceptAttributes.Version currentAttributesVersion = attr.getVersion(viewCoord);
				currentVersion = (Version)currentAttributesVersion;
			}
			else if (compType == CONTRADICTION_INVESTIGATION_TYPE.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				Description.Version currentDescriptionVersion = desc.getVersion(viewCoord);
				currentVersion = (Version)currentDescriptionVersion;
			} 
			else if (compType == CONTRADICTION_INVESTIGATION_TYPE.RELATIONSHIP)
			{
				// Assume Source Rel first.  If returns null, 
				Relationship.Version currentRelationshipVersion = null;
				Relationship rel = concept.getRelationship(componentNid);
	
				if (rel == null)
					currentRelationshipVersion = concept.getDestRel(componentNid).getVersion(viewCoord);
				else
					currentRelationshipVersion = rel.getVersion(viewCoord);
				
				currentVersion = (Version)currentRelationshipVersion;
			
				if (currentVersion == null)
				{
					 rel = concept.getRelationship(componentNid);
					currentRelationshipVersion = concept.getDestRel(componentNid).getVersion(viewCoord);
	
				}
			} 			
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in identifying version of type: " + compType + "for component: " + componentNid);
        	return null;
		}
		
		return currentVersion;
	}

	// Get the Position of the current version
	private PositionBI getVersionPosition(ComponentVersionBI version) {
		PositionBI coordPosition = null;
		
		try
		{
			coordPosition = Terms.get().newPosition(Terms.get().getPath(version.getPathNid()), ThinVersionHelper.convert(version.getTime()));
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid(), e);
		}
		
		return coordPosition;
	}
	
	// For all versions of a componentId, add them individually to the "Found Positions" collection 
	private void updateFoundPositionsForAllVersions(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, Iterator<?> versions, int componentNid, CONTRADICTION_INVESTIGATION_TYPE compType) throws ContraditionException 
	{
		while(versions.hasNext())
		{
			ComponentVersionBI v = (ComponentVersionBI)versions.next();
			
			PositionBI versionPosition = getVersionPosition(v);
			PositionForSet currentPos = normalizeLeastCommonAncestors(v, versionPosition);
			Version currentVersion = getCurrentVersionByType(concept, compType, componentNid);

			updateFoundPositionWithVersion(foundPositionsMap, currentPos, componentNid, currentVersion);
		}
	}

	// Of all versions, identify least common ancestor
	private ComponentVersionBI identifyLeastCommonAncestorVersion(Iterator<?> versions) 
	{
		ComponentVersionBI v = null;
		Map<PositionBI, ComponentVersionBI> possibleVersions = new HashMap<PositionBI,ComponentVersionBI>();
			
		try 
		{
			while(versions.hasNext())
			{
				v = (ComponentVersionBI) versions.next();
				PositionBI p = Terms.get().newPosition(Terms.get().getPath(v.getPathNid()), ThinVersionHelper.convert(v.getTime()));
		
				if (leastCommonAncestorForViewPosition.isSubsequentOrEqualTo(p))
					possibleVersions.put(p, v);
			}
			
			return getLeastCommonAncestorVersion(possibleVersions);
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + v.getPathNid());
		}
		
		return null;
	}
	
	// Add version to found positions map
	private void updateFoundPositionWithVersion(Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, PositionForSet currentPos, int componentNid, Version currentVersion) 
	{
		if (currentVersion == null)
			currentVersion = null;
			
		if (!foundPositionsMap.containsKey(currentPos))
		{
			// First time position viewed, create new map and add componentId-version map to position
			HashMap<Integer, Version> newPositionMap = new HashMap<Integer, Version>();
			newPositionMap.put(componentNid, currentVersion);
			foundPositionsMap.put(currentPos, newPositionMap);
		}
		else
		{
			// Position already found, add componentId-version map to position 
			HashMap<Integer, Version> positionMap = foundPositionsMap.get(currentPos);
			positionMap.put(componentNid, currentVersion);					
		}

	}

	// If version is antecedent to lcaForViewPosition, create a PositionForSet out of the version's timestamp and lcaForViewPosition's path
	// Otherwise, simply create a PositionForSet from current version
	private PositionForSet normalizeLeastCommonAncestors(ComponentVersionBI version, PositionBI versionPosition)
	{		
		PositionForSet currentPos = null;
		
		if (versionPosition.isAntecedentOrEqualTo(leastCommonAncestorForViewPosition))
			currentPos = new PositionForSet(version.getTime(), leastCommonAncestorForViewPosition.getPath().getConceptNid());
		else
			currentPos = new PositionForSet(version.getTime(), version.getPathNid());
		
		return currentPos;
	}

	// FOR REFSETS
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
	
	// FOR REFSETS
	private Version getCurrentRefsetVersion(ComponentVersionBI version)
	{
		// TODO: Haven't done this yet, but need viewCoord
		try 
		{
			ViewCoordinate versionCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid());
		}

		return null;		
	}

	
	// FOR REFSETS
	private CONTRADICTION_RESULT refsetMembershipConflictFound(Concept concept, Map<PositionForSet, HashMap<Integer,Version>> foundPositionsMap, int componentNid, CONTRADICTION_INVESTIGATION_TYPE compType) throws TerminologyException, IOException, ParseException 
	{
		List<? extends I_ExtendByRef> members = Terms.get().getAllExtensionsForComponent(componentNid);
		CONTRADICTION_RESULT position = CONTRADICTION_RESULT.NONE;
		boolean isUnreachable = false;
		boolean isSingle = false;

		for (I_ExtendByRef member : members)
		{
			if (member.getMutableParts().size() > 2) 
			{
				ComponentVersionBI lcaDiscoveredVersion = identifyLeastCommonAncestorVersion(member.getMutableParts().iterator());
				int leastCommonAncestorHashCode = getLeastCommonRefsetAncestorHashCode(lcaDiscoveredVersion, member);
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

				try {
					updateFoundPositionsForAllVersions(concept, foundPositionsMap, member.getMutableParts().iterator(), componentNid, compType);
				} catch (ContraditionException ce) {
					return CONTRADICTION_RESULT.CONTRADICTION;
				}
			} 
			else 
			{ 
				ConceptDataSimpleReference data = new ConceptDataSimpleReference(concept);
int ss = data.getRefsetMembers().size();
				int size = member.getMutableParts().size();
				RefsetMember a = concept.getRefsetMember(member.getMemberId());
				position = areFoundPositionsReachable(concept, foundPositionsMap, member.getMutableParts(), CONTRADICTION_INVESTIGATION_TYPE.REFSET, componentNid);
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
}


