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

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;
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
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.contradiction.ComponentType;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.contradiction.ContradictionInvestigationType;
import org.ihtsdo.tk.contradiction.ContradictionResult;
import org.ihtsdo.tk.contradiction.PositionForSet;

public class ContradictionIdentifier implements ContradictionIdentifierBI {

     // Class Variables
    private static PositionBI leastCommonAncestorForViewPosition = null;
    private static PositionMapper conflictMapper = null;

	// TODO: Assign values via building of wf metadata but in cr metadata
	private String adjudicatorPathUid = "a55ae9d9-2e8a-5d04-a3b3-9c8a87ba34d1"; 
	private String originPathUid = "4906ace4-537f-5ea9-9575-c5ce4182f292"; 
	private int adjudicatorPathNid = 0;
	private int originPathNid = 0;
	
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
			adjudicatorPathNid = Terms.get().getPath(UUID.fromString(adjudicatorPathUid)).getConceptNid();
			originPathNid = Terms.get().getPath(UUID.fromString(originPathUid)).getConceptNid();
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to get Active Ace Frame", e);
            return;
        }
    }

    // For a given concept, look at a set of components at a time.
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type
    @Override
    public ContradictionResult inConflict(ConceptChronicleBI conceptChronicle) throws IOException, ParseException {
        Concept concept = (Concept) conceptChronicle;
        boolean isSingle = false;
        boolean isDuplicateSingleComp = false;
        ContradictionResult result = ContradictionResult.NONE;
        Map<PositionForSet, HashMap<Integer, Version>> foundPositionsMap = new HashMap<PositionForSet, HashMap<Integer, Version>>();

        for (ComponentType type : ComponentType.values()) {
            if (type == ComponentType.ATTRIBUTE) {
                result = checkForComponentConflicts(concept, foundPositionsMap, concept.getConAttrs(), new ConceptAttributeComparer());
            } else {
                result = checkMultipleComponentsForConflicts(concept, foundPositionsMap, type);
            }

            if (result.equals(ContradictionResult.CONTRADICTION)) {
                return ContradictionResult.CONTRADICTION;
            } else if (result.equals(ContradictionResult.CONTRADICTON_SAME_VALUES_SINGLE_COMPID)) {
                isDuplicateSingleComp = true;
            } else if (result.equals(ContradictionResult.SINGLE)) {
                isSingle = true;
            }
        }
        
        ContradictionResult foundPosResult = areFoundPositionsReachable(concept, foundPositionsMap);

        if (foundPosResult.equals(ContradictionResult.CONTRADICTION)) {
            return ContradictionResult.CONTRADICTION;
        } else if (isDuplicateSingleComp) {
            return ContradictionResult.CONTRADICTON_SAME_VALUES_SINGLE_COMPID;
        } else if (foundPosResult.equals(ContradictionResult.CONTRADICTON_SAME_VALUES_DIFFERENT_COMPID)) {
            return ContradictionResult.CONTRADICTON_SAME_VALUES_DIFFERENT_COMPID;
        } else if (isSingle) {
            return ContradictionResult.SINGLE;
        } else {
            return ContradictionResult.NONE;
        }
    }

    // For Multiple Tuples Components (all but concept attributes)
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type	
    public ContradictionResult checkMultipleComponentsForConflicts(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, Version>> foundPositionsMap, 
            ComponentType type) throws IOException 
           {
        boolean isSingle = false;
        boolean isDuplicate = false;
        AttributeComparer comparer = null;
        Collection<? extends ComponentChroncileBI<?>> componentTuples = null;

        // Capture collection of tuples and create the comparator type
        if (type == ComponentType.DESCRIPTION) {
            componentTuples = concept.getDescs();
            comparer = new DescriptionAttributeComparer();
        } else {
            comparer = new RelationshipAttributeComparer();

            if (type == ComponentType.SOURCE_RELATIONSHIP) {
                componentTuples = concept.getSourceRels();
            } else if (type == ComponentType.DESTINATION_RELATIONSHIP) {
                componentTuples = concept.getDestRels();
            }
        }

        // Search for conflict or single change on each tuple
        for (ComponentChroncileBI<?> tuple : componentTuples) {
            ContradictionResult pos = checkForComponentConflicts(concept, foundPositionsMap, tuple, comparer);

            if (pos.equals(ContradictionResult.CONTRADICTION)) {
                return pos;
            } else if (pos.equals(ContradictionResult.CONTRADICTON_SAME_VALUES_SINGLE_COMPID)) {
                isDuplicate = true;
            } else if (pos.equals(ContradictionResult.SINGLE)) {
                isSingle = true;
            }

            comparer.clear();
        }

        if (isDuplicate) {
        	return ContradictionResult.CONTRADICTON_SAME_VALUES_SINGLE_COMPID;
        }  else if (isSingle) {
            return ContradictionResult.SINGLE;
        } else {
            return ContradictionResult.NONE;
        }
    }

    // For a given concept's componentId, see if the component has contradiction
     public ContradictionResult checkForComponentConflicts(
            Concept concept, Map<PositionForSet, 
            HashMap<Integer, Version>> foundPositionsMap, 
            ComponentChroncileBI<?> comp, 
            AttributeComparer comparer) {
        boolean isSingle = false;
        boolean isDuplicate = false;
        boolean isContradiction = false;
		ContradictionResult componentResult = ContradictionResult.NONE;
        ContradictionInvestigationType compType = comparer.getComponentType();

        try {
            // Examine Refsets.  Only continue to concept's components if refset check doesn't identify contradiction
//			ContradictionResult refsetResult = refsetMembershipConflictFound(concept, foundPositionsMap, comp.getNid(), compType);
            ContradictionResult refsetResult = ContradictionResult.NONE;

			ComponentVersionBI latestOriginVersion = null;
			Set<ComponentVersionBI> developerVersions = new HashSet<ComponentVersionBI>();
			
			if (refsetResult != ContradictionResult.CONTRADICTION)
			{
				ComponentVersionBI earliestVersion = null;
				ComponentVersionBI latestAdjudicatedVersion = null;
				Set<ComponentVersionBI> originVersions = new HashSet<ComponentVersionBI>();
				
				PathBI originPath = Terms.get().getPath(originPathNid);
				
//				Set<ComponentVersionBI> mutableParts = identifyMutableParts(compType, comp);
//				
//				for (ComponentVersionBI part : mutableParts)
				for (ComponentVersionBI part : comp.getVersions())
				{
					// Identify Adjudication versions and find latest 
					if (part.getPathNid() == adjudicatorPathNid)
					{
						if (latestAdjudicatedVersion == null || part.getTime() > latestAdjudicatedVersion.getTime())
						{
							latestAdjudicatedVersion = part;
						}
					} else if ((part.getPathNid() == originPathNid) || (isOriginVersion(originPath, part))) {
						// Identify Origins versions and find latest
						originVersions.add(part);
						if (latestOriginVersion == null || part.getTime() > latestOriginVersion.getTime())
						{
							// For comparer if necessary
							latestOriginVersion = part;
						}							
					} else {
						// Identify Developer versions
						developerVersions.add(part);
					}
				}
				
				if (developerVersions.isEmpty())
				{
					// Avoid investigation & adding to foundPositions
					return ContradictionResult.NONE;
				}
				
				if (latestAdjudicatedVersion != null)
				{
					PositionBI adjudicatorVersionPosition = getVersionPosition(latestAdjudicatedVersion);
					Version adjudicatorVersion = getAdjudicatorVersionByType(concept, compType, latestAdjudicatedVersion.getNid(), adjudicatorVersionPosition);

					// Using Adj Versions, run fastRelativeMapper twice to identify changes since
					for (ComponentVersionBI version : developerVersions)
					{
						PositionBI developerVersionPosition = getVersionPosition(version);
						Version testingVersion = getCurrentVersionByType(concept, compType, version.getNid(), developerVersionPosition);

						RELATIVE_POSITION relPosition = conflictMapper.fastRelativePosition(adjudicatorVersion, testingVersion, Terms.get().getActiveAceFrameConfig().getPrecedence());
						
						int a = 0;

						if (relPosition != RELATIVE_POSITION.EQUAL) 
						{
							if (relPosition == RELATIVE_POSITION.CONTRADICTION)
								throw new Exception("Can't be contra via adjud path");

							if (relPosition == RELATIVE_POSITION.AFTER || relPosition == RELATIVE_POSITION.BEFORE) 
							{
								// For Edge Case
								RELATIVE_POSITION relSecondPosition = conflictMapper.fastRelativePosition(adjudicatorVersion, testingVersion, Precedence.TIME);
								if (relSecondPosition != relPosition) 
								{
									if (!isContradictionWithSameValues(comparer, adjudicatorVersion, comp)) {
										// Concept contains contradiction.  No need to update positions as check complete for concept
										return ContradictionResult.CONTRADICTION;
									} else {
										isDuplicate = true;
									}
								}
							}
						} 
					}
				}
				else
				{
					// New or Single change is certain
					isSingle = true;
					int developerPathNid = 0;
						
					// Check for STRICTLY multiple modifications to single component by Single developer, else Contradiction
					for (ComponentVersionBI version : developerVersions)
					{
						if (developerPathNid == 0) {
							developerPathNid = version.getPathNid();
						} else if (developerPathNid != version.getPathNid()) {
							isContradiction = true;
							break;
						}
					}
				}
			}
	
			if (isContradiction && !isDuplicate)
			{
				// Check if for given CompId, have multiple versions with same changes
				if (!isContradictionWithSameValues(comparer, latestOriginVersion, comp)) {
					// Concept contains contradiction.  No need to update positions as check complete for concept
					return ContradictionResult.CONTRADICTION;
				} else {
					isDuplicate = true;
				}
			}

			// Add Position to List
			updateFoundPositionsForAllVersions(concept, foundPositionsMap, developerVersions.iterator(), comp.getNid(), compType);
		} catch (ContraditionException ce) {
			return ContradictionResult.CONTRADICTION;
		} catch (Exception e) 
		{
        	AceLog.getAppLog().log(Level.WARNING, "Error in detecting contradictions for component: " + comp.toString(), e);
        	return ContradictionResult.ERROR;
		}
		
		if (isDuplicate) {
			return ContradictionResult.CONTRADICTON_SAME_VALUES_SINGLE_COMPID;
		} else if (isSingle) {
			return ContradictionResult.SINGLE;
		}
		
		return ContradictionResult.NONE;
	}
	


	private boolean isOriginVersion(PathBI originPath, ComponentVersionBI version) throws IOException {
		for (PositionBI origin : originPath.getInheritedOrigins())
		{
			if (version.getPosition().isAntecedentOrEqualTo(origin))
				return true;
		}
		
		return false;
	}


	private boolean isContradictionWithSameValues(AttributeComparer comparer, ComponentVersionBI latestOriginVersion, ComponentChroncileBI<?> comp) throws Exception
	{ 
		boolean isNewComponent = false;
		AttributeComparer secondaryComparer = comparer.getClass().newInstance();
		
		if (latestOriginVersion != null) {
			comparer.initializeAttributes(latestOriginVersion);
		} else {
			// New Component with multiple versions!
			isNewComponent = true;
			if (comp.getVersions().size() < 2) {
				throw new Exception("Shouldn't have single version without latestOriginVersion");
			}
		}
		
		for (ComponentVersionBI version : comp.getVersions())
		{
			if (!version.getPosition().isAntecedentOrEqualTo(leastCommonAncestorForViewPosition))
			{
				if (isNewComponent) {
					comparer.initializeAttributes(version);
				} else {
					if (!comparer.hasSameAttributes(version))
					{
						if (!secondaryComparer.isInitialized())
						{
							secondaryComparer.initializeAttributes(version);
						}
						else if (!secondaryComparer.hasSameAttributes(version))
						{
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}
			
		
			
			
	private ComponentVersionBI identifyViewPositonVersion(Iterator<?> versions) 
	{
		ComponentVersionBI v = null;
		Map<PositionBI, ComponentVersionBI> possibleVersions = new HashMap<PositionBI,ComponentVersionBI>();
			
		try 
		{
			while(versions.hasNext())
			{
				v = (ComponentVersionBI) versions.next();
				PositionBI p = new Position(v.getTime(), Terms.get().getPath(v.getPathNid()));
		
				if (conflictMapper.getDestination().getPath().equals(p.getPath()) &&
					conflictMapper.getDestination().getTime() > p.getTime())
					return v;
			}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + v.getPathNid());
		}
		
		return null;
	}
	
    // For each version of a given component, ensure that a contradiction doesn't exist with another version of the any other component (view PositionMapper)
    private ContradictionResult areFoundPositionsReachable(
            Concept concept, 
            Map<PositionForSet, 
            HashMap<Integer, Version>> foundPositionsMap) {
		// Todo: Per Position Found, only store one item 
		// TODO: Also remove CompNid as not needed?

        Version currentVersion = null;
		Version testingVersion = null;

		try {
			for (PositionForSet currentPositionKey : foundPositionsMap.keySet())
			{
				HashMap<Integer, Version> currentVersionMap = foundPositionsMap.get(currentPositionKey);
				currentVersion = currentVersionMap.get(currentVersionMap.keySet().iterator().next());
	
				for (PositionForSet testingPositionKey : foundPositionsMap.keySet())
				{
					if (!currentPositionKey.equals(testingPositionKey))
					{
						HashMap<Integer, Version> testingVersionMap = foundPositionsMap.get(testingPositionKey);
						testingVersion = testingVersionMap.get(testingVersionMap.keySet().iterator().next());
	
						RELATIVE_POSITION retPosition = conflictMapper.fastRelativePosition(currentVersion, testingVersion, Terms.get().getActiveAceFrameConfig().getPrecedence());
						
						if (retPosition == RELATIVE_POSITION.CONTRADICTION)
						{
							if (isSameVersionValues(currentVersion, testingVersion))
								return ContradictionResult.CONTRADICTON_SAME_VALUES_DIFFERENT_COMPID;
							else
								return ContradictionResult.CONTRADICTION;
						}
					}
				}
			}
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in identifying if areFoundPositionsReachable");
		}
		
		return ContradictionResult.NONE;
	}


	private boolean isSameVersionValues(Version testingVersion, Version currentVersion)
	{
		Class testingClass = testingVersion.getClass();
		AttributeComparer comparer = null;
		boolean result = false;
		
		if (!testingClass.equals(currentVersion.getClass()))
			return false;
		
		if (testingClass.equals(ConceptAttributes.Version.class))
		{
			comparer = new ConceptAttributeComparer();
			comparer.initializeAttributes((ConceptAttributes.Version)currentVersion);
			result = comparer.hasSameAttributes((ConceptAttributes.Version)testingVersion);
		}
		else if (testingClass.equals(Description.Version.class))
		{
			comparer = new DescriptionAttributeComparer();
			comparer.initializeAttributes((Description.Version)currentVersion);
			result = comparer.hasSameAttributes((Description.Version)testingVersion);
		}
		else if (testingClass.equals(Relationship.Version.class))
		{
			comparer = new RelationshipAttributeComparer();
			comparer.initializeAttributes((Relationship.Version)currentVersion);
			result = comparer.hasSameAttributes((Relationship.Version)testingVersion);
		}
			
		return result;
	}
      

    // For set of origins, identify origin (by position) that is the least common ancestor of the other origins
    // ie, the version by which to identify original version of concept
    private PositionBI determineLeastCommonAncestor(
            Set<HashSet<PositionBI>> originsByVersion) {
        int smallestSize = -1;
        Set<PositionBI> smallestSet = new HashSet<PositionBI>();

        for (HashSet<PositionBI> origins : originsByVersion) {
            // Identify smallest set
            if ((origins.size() < smallestSize) || (smallestSize < 0)) {
                smallestSize = origins.size();
                smallestSet = origins;
            }
        }

        originsByVersion.remove(smallestSet);
        Set<PositionBI> testingAncestors = new HashSet<PositionBI>();

        for (PositionBI testingPos : smallestSet) {
            boolean success = true;
            for (HashSet<PositionBI> originSet : originsByVersion) {
                if (!originSet.contains(testingPos)) {
                    success = false;
                }
            }

            if (success) {
                testingAncestors.add(testingPos);
            }
        }

        PositionBI leastCommonAncestor = null;
        for (PositionBI ancestor : testingAncestors) {
            if (leastCommonAncestor == null || leastCommonAncestor.isAntecedentOrEqualTo(ancestor)) {
                leastCommonAncestor = ancestor;
            }
        }

        return leastCommonAncestor;
    }

    // identify the version of a component that is the least common ancestor of all versions
    private ComponentVersionBI getLeastCommonAncestorVersion(
            Map<PositionBI, ComponentVersionBI> versions) {
        Set<PositionBI> keys = versions.keySet();
        PositionBI leastCommonAncestorPositionBI = null;

        // Find the lowest possible Path
        for (PositionBI key : keys) {
            if (leastCommonAncestorPositionBI == null) {
                leastCommonAncestorPositionBI = key;
            } else {
                if (key.isSubsequentOrEqualTo(leastCommonAncestorPositionBI)) {
                    leastCommonAncestorPositionBI = key;
                }
            }
        }

        return versions.get(leastCommonAncestorPositionBI);
    }

    // For a component Type & Nid, get the version corresponding to the View Coordinate 
    private Version getCurrentVersionByType(Concept concept, 
            ContradictionInvestigationType compType, 
            int componentNid, PositionBI position) throws ContraditionException {
        Version currentVersion = null;

		try
		{
			if (compType == ContradictionInvestigationType.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				Collection<ConceptAttributes.Version> currentAttributesVersions = attr.getVersions();

				for (ConceptAttributes.Version version : currentAttributesVersions)
				{
					if (version.getPosition().equals(position))
					{
						currentVersion = version;
						break;
					}
				}
			}
			else if (compType == ContradictionInvestigationType.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				Collection<Description.Version> currentDescriptionVersions = desc.getVersions();
				
				for (Description.Version version : currentDescriptionVersions)
				{
					if (version.getPosition().equals(position))
					{
						currentVersion = version;
						break;
					}
				}
			} 
			else if (compType == ContradictionInvestigationType.RELATIONSHIP)
			{
				// Assume Source Rel first.  If returns null, 
				Collection<org.ihtsdo.concept.component.relationship.Relationship.Version> currentRelationshipVersions = null;
				Relationship rel = concept.getRelationship(componentNid);
	
				if (rel == null)
				{
					rel = concept.getDestRel(componentNid);
				}
				
				currentRelationshipVersions =  rel.getVersions();
				
				for (Relationship.Version version : currentRelationshipVersions)
				{
					if (version.getPosition().equals(position))
					{
						currentVersion = version;
						break;
					}
				}
			} 			
		} catch (IOException e) {
	    	AceLog.getAppLog().log(Level.WARNING, "Error in identifying version of type: " + compType + "for component: " + componentNid);
	    	return null;
		} 
		
		return currentVersion;
	}

	private Version getAdjudicatorVersionByType(Concept concept, ContradictionInvestigationType compType, int componentNid, PositionBI position) throws ContraditionException 
	{
		Version currentVersion = null;

		try
		{
			ViewCoordinate viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();

			if (compType == ContradictionInvestigationType.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				currentVersion = attr.getVersion(viewCoord);
			}
			else if (compType == ContradictionInvestigationType.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				Collection<Description.Version> currentDescriptionVersions = desc.getVersions();
				
				for (Description.Version version : currentDescriptionVersions)
				{
					if (version.getPosition().equals(position))
					{
						currentVersion = version;
						break;
					}
				}
			} 
			else if (compType == ContradictionInvestigationType.RELATIONSHIP)
			{
				// Assume Source Rel first.  If returns null, 
				Collection<org.ihtsdo.concept.component.relationship.Relationship.Version> currentRelationshipVersions = null;
				Relationship rel = concept.getRelationship(componentNid);
	
				if (rel == null)
				{
					rel = concept.getDestRel(componentNid);
				}
				
				currentRelationshipVersions =  rel.getVersions();
				
				for (Relationship.Version version : currentRelationshipVersions)
				{
					if (version.getPosition().equals(position))
					{
						currentVersion = version;
						break;
					}
				}
			} 			
		} catch (IOException e) {
	    	AceLog.getAppLog().log(Level.WARNING, "Error in identifying version of type: " + compType + "for component: " + componentNid);
	    	return null;
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return currentVersion;
	}


    // Get the Position of the current version
    private PositionBI getVersionPosition(ComponentVersionBI version) {
        PositionBI coordPosition = null;

		try
		{
			coordPosition = new Position(version.getTime(), Terms.get().getPath(version.getPathNid()));
		} catch (Exception e) { 
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid(), e);
		}
		
		return coordPosition;
	}

	private PositionBI getAdjudicatorPosition(ComponentVersionBI version) {
		PositionBI coordPosition = null;
		
		try
		{
			coordPosition = new Position(Long.MAX_VALUE, Terms.get().getPath(version.getPathNid()));
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid(), e);
        }

        return coordPosition;
    }

    // For all versions of a componentId, add them individually to the "Found Positions" collection 
    private void updateFoundPositionsForAllVersions(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, Version>> foundPositionsMap, 
            Iterator<?> versions, 
            int componentNid, 
            ContradictionInvestigationType compType) throws ContraditionException {
        while (versions.hasNext()) {
            ComponentVersionBI v = (ComponentVersionBI) versions.next();

			PositionForSet pfsKey = new PositionForSet(v.getTime(), v.getPathNid());
			
			PositionBI developerVersionPosition = getVersionPosition(v);
			Version developerVersion = getCurrentVersionByType(concept, compType, componentNid, developerVersionPosition);

			updateFoundPositionWithVersion(foundPositionsMap, pfsKey, componentNid, developerVersion);
        }
    }

    // Of all versions, identify least common ancestor
    private ComponentVersionBI identifyLeastCommonAncestorVersion(
            Iterator<?> versions) {
        ComponentVersionBI v = null;
        Map<PositionBI, ComponentVersionBI> possibleVersions = new HashMap<PositionBI, ComponentVersionBI>();

        try {
            while (versions.hasNext()) {
                v = (ComponentVersionBI) versions.next();
				PositionBI p = new Position(v.getTime(), Terms.get().getPath(v.getPathNid()));

                if (leastCommonAncestorForViewPosition.isSubsequentOrEqualTo(p)) {
                    possibleVersions.put(p, v);
                }
            }

            return getLeastCommonAncestorVersion(possibleVersions);
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + v.getPathNid());
        }

        return null;
    }

    // Add version to found positions map
    private void updateFoundPositionWithVersion(
            Map<PositionForSet, HashMap<Integer, Version>> foundPositionsMap, 
            PositionForSet currentPos, 
            int componentNid, 
            Version currentVersion) {
        if (currentVersion == null) {
            currentVersion = null;
        }

        if (!foundPositionsMap.containsKey(currentPos)) {
            // First time position viewed, create new map and add componentId-version map to position
            HashMap<Integer, Version> newPositionMap = new HashMap<Integer, Version>();
            newPositionMap.put(componentNid, currentVersion);
            foundPositionsMap.put(currentPos, newPositionMap);
        } else {
            // Position already found, add componentId-version map to position 
            HashMap<Integer, Version> positionMap = foundPositionsMap.get(currentPos);
            positionMap.put(componentNid, currentVersion);
        }

    }

    // If version is antecedent to lcaForViewPosition, create a PositionForSet out of the version's timestamp and lcaForViewPosition's path
    // Otherwise, simply create a PositionForSet from current version
    private PositionForSet normalizeLeastCommonAncestors(
            ComponentVersionBI version, 
            PositionBI versionPosition) {
        PositionForSet currentPos = null;

		//	*** Unnec b/c only devVersions analyzed> 
//		if (versionPosition.isAntecedentOrEqualTo(leastCommonAncestorForViewPosition))
//			currentPos = new PositionForSet(version.getTime(), leastCommonAncestorForViewPosition.getPath().getConceptNid());
//		else
//			currentPos = new PositionForSet(version.getTime(), version.getPathNid());
		
		

        return currentPos;
    }

    // FOR REFSETS
    private int getLeastCommonRefsetAncestorHashCode(
            ComponentVersionBI leastCommonAncestorVersion, 
            I_ExtendByRef member) {
        for (I_ExtendByRefPart version : member.getMutableParts()) {
            if (leastCommonAncestorVersion.getTime() == version.getTime()
                    && leastCommonAncestorVersion.getPathNid() == version.getPathNid()) {
                return version.getPartsHashCode();
            }
        }

        return -1;
    }

    // FOR REFSETS
    private Version getCurrentRefsetVersion(ComponentVersionBI version) {
        // TODO: Haven't done this yet, but need viewCoord
        try {
            ViewCoordinate versionCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid());
        }

        return null;
    }

    // FOR REFSETS
    private ContradictionResult refsetMembershipConflictFound(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, Version>> foundPositionsMap, 
            int componentNid, 
            ContradictionInvestigationType compType) throws TerminologyException, IOException, ParseException {
        List<? extends I_ExtendByRef> members = Terms.get().getAllExtensionsForComponent(componentNid);
        ContradictionResult position = ContradictionResult.NONE;
        boolean isUnreachable = false;
        boolean isSingle = false;

        for (I_ExtendByRef member : members) {
            if (member.getMutableParts().size() > 2) {
                ComponentVersionBI lcaDiscoveredVersion = identifyLeastCommonAncestorVersion(member.getMutableParts().iterator());
                int leastCommonAncestorHashCode = getLeastCommonRefsetAncestorHashCode(lcaDiscoveredVersion, member);
                int secondaryHashCode = -1;

                for (I_ExtendByRefPart v : member.getMutableParts()) {
                    PositionBI p = new Position(v.getTime(), Terms.get().getPath(v.getPathNid()));

                    if ((p.isSubsequentOrEqualTo(leastCommonAncestorForViewPosition)) && leastCommonAncestorHashCode != v.getPartsHashCode()) {
                        if (secondaryHashCode < 0) {
                            position = ContradictionResult.SINGLE;
                            secondaryHashCode = v.getPartsHashCode();
                        } else {
                            if (secondaryHashCode != v.getPartsHashCode()) {
                                position = ContradictionResult.CONTRADICTION;
                                break;
                            }
                        }
                    }
                }

                try {
                    updateFoundPositionsForAllVersions(concept, foundPositionsMap, member.getMutableParts().iterator(), componentNid, compType);
                } catch (ContraditionException ce) {
                    return ContradictionResult.CONTRADICTION;
                }
            } else {
                ConceptDataSimpleReference data = new ConceptDataSimpleReference(concept);
                int ss = data.getRefsetMembers().size();
                int size = member.getMutableParts().size();
                RefsetMember a = concept.getRefsetMember(member.getMemberId());
                // position = areFoundPositionsReachable(concept, foundPositionsMap, member.getMutableParts(), ContradictionInvestigationType.REFSET, componentNid);
            }

            if (position.equals(ContradictionResult.CONTRADICTION)) {
                return position;
            } else if (position.equals(ContradictionResult.UNREACHABLE)) {
                isUnreachable = true;
            } else if (position.equals(ContradictionResult.SINGLE)) {
                isSingle = true;
            }
        }

        if (isUnreachable) {
            return ContradictionResult.UNREACHABLE;
        } else if (isSingle) {
            return ContradictionResult.SINGLE;
        } else {
            return ContradictionResult.NONE;
        }
    }
}


