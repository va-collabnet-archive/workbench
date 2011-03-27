package org.ihtsdo.concept;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.component.ConceptComponent.Version;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
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
import org.ihtsdo.tk.contradiction.ContradictionResult;
import org.ihtsdo.tk.contradiction.PositionForSet;

public class ContradictionIdentifier implements ContradictionIdentifierBI {

     // Class Variables
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

			adjudicatorPathNid = Terms.get().getPath(UUID.fromString(adjudicatorPathUid)).getConceptNid();
			originPathNid = Terms.get().getPath(UUID.fromString(originPathUid)).getConceptNid();
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to get Active Ace Frame", e);
        }
    }

    // For a given concept, look at a set of components at a time.
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type
    @Override
    public ContradictionResult isConceptInConflict(ConceptChronicleBI conceptChronicle) {
        boolean isSingleEdit = false;
        boolean isDuplicateEdit = false;
        Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap = new HashMap<PositionForSet, HashMap<Integer, ComponentVersionBI>>();

		try {
		        ContradictionResult result = analyzeContradictionPerComponent((Concept)conceptChronicle, foundPositionsMap);
	    
	        if (result == ContradictionResult.CONTRADICTION) {
	        	return result;
	        } else {
	            // Ensure new changes are visible to one another.  
	            // Otherwise, is a contradiction or possible duplicate new component
		        ContradictionResult foundPosResult = areFoundPositionsReachable((Concept)conceptChronicle, foundPositionsMap);
		
		        if (foundPosResult.equals(ContradictionResult.CONTRADICTION)) {
		            return ContradictionResult.CONTRADICTION;
		        } else if (isDuplicateEdit || result == ContradictionResult.DUPLICATE_EDIT) {
		            return ContradictionResult.DUPLICATE_EDIT;
		        } else if (foundPosResult.equals(ContradictionResult.DUPLICATE_NEW_COMPONENT)) {
		            return ContradictionResult.DUPLICATE_NEW_COMPONENT;
		        } else if (isSingleEdit || result == ContradictionResult.SINGLE_MODELER_CHANGE) {
		            return ContradictionResult.SINGLE_MODELER_CHANGE;
		        }
		    }
		} catch (Exception e) {
	    	AceLog.getAppLog().log(Level.WARNING, "Failure in detecting conflict on concept: " + conceptChronicle.getPrimUuid());
	    	return ContradictionResult.ERROR;
	    }

		return ContradictionResult.NONE;
    }
    
    private ContradictionResult analyzeContradictionPerComponent(Concept concept, Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap) throws Exception 
    {
    	boolean isDuplicateEdit = false;
		boolean isSingleEdit = false;
		ContradictionResult result = ContradictionResult.NONE;
    	
		for (ComponentType type : ComponentType.values()) {
			if (type != ComponentType.RELATIONSHIP && type != ComponentType.REFSET) 
			{
	        	// Cycle through 4 component types
	            if (type == ComponentType.ATTRIBUTE) {
	            	// Concept Attribute is the only componentType with a Single CompId
	            	result = checkSingleComponentId(concept, foundPositionsMap, concept.getConAttrs(), new ConceptAttributeComparer());
	            } else {
	            	// Descriptions & Relationships componentTypes each have multiple CompIds 
	                result = checkCollectionComponentTypes(concept, foundPositionsMap, type);
	            }
	
	            if (result.equals(ContradictionResult.CONTRADICTION)) {
	                return ContradictionResult.CONTRADICTION;
	            } else if (result.equals(ContradictionResult.DUPLICATE_EDIT)) {
	                isDuplicateEdit = true;
	            } else if (result.equals(ContradictionResult.SINGLE_MODELER_CHANGE)) {
	                isSingleEdit = true;
	            } else if (result.equals(ContradictionResult.ERROR)) {
	            	throw new TerminologyException("Failed processing Concept: " + concept.getInitialText());
	            }
	        }
		}
    	
    	return identifyContradictionResult(false, isSingleEdit, isDuplicateEdit, false);
   	}

	private ContradictionResult identifyContradictionResult(boolean isContradiction, boolean isSingleEdit, boolean isDuplicateEdit, boolean isDuplicateNew) 
	{
		if (isContradiction) {
			return ContradictionResult.CONTRADICTION;
		} else if (isDuplicateEdit) {
			return ContradictionResult.DUPLICATE_EDIT;
	    } else if (isDuplicateNew) {
	        return ContradictionResult.DUPLICATE_NEW_COMPONENT;
	    } else if (isSingleEdit) {
			return ContradictionResult.SINGLE_MODELER_CHANGE;
	    }  else {
	    	return ContradictionResult.NONE;
		}
	}

	// For Multiple Tuples Components (all but concept attributes)
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type	
    public ContradictionResult checkCollectionComponentTypes(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            ComponentType type) throws Exception 
    {
        boolean isSingleEdit = false;
        boolean isDuplicateEdit = false;
        AttributeComparer comparer = null;
        Collection<? extends ComponentChroncileBI<?>> componentTuples = null;

        // Capture collection of Components of this type and create proper comparator 
        if (type == ComponentType.DESCRIPTION) {
            componentTuples = concept.getDescs();
            comparer = new DescriptionAttributeComparer();
        } else {
            comparer = new RelationshipAttributeComparer();

            if (type == ComponentType.SRC_RELATIONSHIP) {
            	((RelationshipAttributeComparer)comparer).setRelationshipType(ComponentType.SRC_RELATIONSHIP);
                componentTuples = concept.getSourceRels();
            } else if (type == ComponentType.DEST_RELATIONSHIP) {
            	((RelationshipAttributeComparer)comparer).setRelationshipType(ComponentType.DEST_RELATIONSHIP);
                componentTuples = concept.getDestRels();
            }
        }

        // Detect for contradictions per componentId in Collection
        for (ComponentChroncileBI<?> tuple : componentTuples) {
            ContradictionResult result = checkSingleComponentId(concept, foundPositionsMap, tuple, comparer);

            if (result.equals(ContradictionResult.CONTRADICTION)) {
                return result;
            } else if (result.equals(ContradictionResult.DUPLICATE_EDIT)) {
                isDuplicateEdit = true;
            } else if (result.equals(ContradictionResult.SINGLE_MODELER_CHANGE)) {
                isSingleEdit = true;
            }

            comparer.clear();
        }

        return identifyContradictionResult(false, isSingleEdit, isDuplicateEdit, false);
    }

    // For a given concept's componentId, see if the component has contradiction
     public ContradictionResult checkSingleComponentId(
            Concept concept, Map<PositionForSet, 
            HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            ComponentChroncileBI<?> comp, 
            AttributeComparer comparer) throws Exception
     {
        boolean isSingleEdit = false;
        boolean isDuplicateEdit = false;
        ContradictionResult result = ContradictionResult.NONE;
		ContradictionResult refsetResult = ContradictionResult.NONE;
	    
	    ComponentVersionBI latestOriginVersion = null;
	    ComponentVersionBI latestAdjudicatedVersion = null; 
		
	    ComponentType compType = comparer.getComponentType();
	    Set<ComponentVersionBI> foundPositionsVersions = new HashSet<ComponentVersionBI>();
        
        try {
			// Examine Refsets.  Only continue to concept's components if refset check doesn't identify contradiction
//			ContradictionResult refsetResult = refsetMembershipConflictFound(concept, foundPositionsMap, comp.getNid(), compType);
			
        	if (refsetResult != ContradictionResult.CONTRADICTION)
			{
				PathBI originPath = Terms.get().getPath(originPathNid);
	
				latestAdjudicatedVersion = identifyLatestAdjudicationVersion(comp);
				latestOriginVersion = identifyLatestOriginVersion(comp, originPath);
			    Map<Integer, ComponentVersionBI> latestDeveloperVersionMap = identifyLatestDeveloperVersions(comp, originPath);
				
				if (!latestDeveloperVersionMap.isEmpty()) {
					if (latestAdjudicatedVersion != null) { 
						// if adjudication has been performed on componentId
						result = handleAdjudication(concept, compType, latestAdjudicatedVersion, latestOriginVersion, latestDeveloperVersionMap, foundPositionsVersions);
					} else {
						// if never had adjudication performed on componentId
						result = handleNonAdjudication(latestDeveloperVersionMap, foundPositionsVersions);
					}
				}
			}
	
        	if (result == ContradictionResult.SINGLE_MODELER_CHANGE) {
				isSingleEdit = true;
        	} else if (result == ContradictionResult.CONTRADICTION)	{
	        	// Have potential contradiction, unless developers made same changes
				ComponentVersionBI compareWithVersion = latestOriginVersion;
				if (latestAdjudicatedVersion != null) {
					compareWithVersion = latestAdjudicatedVersion;
				} 
				
				// Check if for given CompId, have multiple versions with same changes
				if (!isContradictionWithSameValues(comparer, compareWithVersion, foundPositionsVersions)) {
					// Concept contains contradiction.  No need to update positions as check complete for concept
					return ContradictionResult.CONTRADICTION;
				} else {
					isDuplicateEdit = true;
				}
			} 

			// Add Position to List
			updateFoundPositionsForAllVersions(concept, foundPositionsMap, foundPositionsVersions.iterator(), comp.getNid(), compType);
		} catch (ContraditionException ce) {
			return ContradictionResult.CONTRADICTION;
		} 
		
		return identifyContradictionResult(false, isSingleEdit, isDuplicateEdit, false);
	}
	


     // Compare between developer versions to see if two or more developers modified the component
     private ContradictionResult handleNonAdjudication(Map<Integer, ComponentVersionBI> latestDeveloperVersionMap, Set<ComponentVersionBI> foundPositionsVersions) throws TerminologyException, IOException {
		// New or Single change is certain
    	boolean isSingleEdit = true;
    	boolean isContradiction = false;
    	ComponentVersionBI initialVersion = null;
			
		// Check for multiple modifications to single component by multiple developers
		for (ComponentVersionBI version : latestDeveloperVersionMap.values())
		{
			foundPositionsVersions.add(version);

			if (initialVersion == null) {
				initialVersion = version;
			} else {
				RELATIVE_POSITION relPosition = conflictMapper.fastRelativePosition((Version)initialVersion, (Version)version, Terms.get().getActiveAceFrameConfig().getPrecedence());
				if (relPosition == RELATIVE_POSITION.CONTRADICTION) {
					isContradiction = true;
				}
			}
		}
		
		return identifyContradictionResult(isContradiction, isSingleEdit, false, false);
	}

	private ContradictionResult handleAdjudication(Concept concept, ComponentType compType, ComponentVersionBI latestAdjudicatedVersion, 
												  ComponentVersionBI latestOriginVersion, Map<Integer, ComponentVersionBI> latestDeveloperVersionMap, 
												  Set<ComponentVersionBI> foundPositionsVersions) throws TerminologyException, ContraditionException, IOException {
			boolean isContradiction = false;
			boolean isSingleEdit = false;

			// initialize foundPositions with everything.  Will remove from this collection those developer versions that were made before the lastest adjudication 
			foundPositionsVersions.addAll(latestDeveloperVersionMap.values());
			
			// TODO: Do I need this or is it same object??? (latestAdjudicatedVersion & adjudicatorVersion)
			ComponentVersionBI adjudicatorVersion = getAdjudicatorVersion(concept, compType, latestAdjudicatedVersion);

			// Using Adj Versions, run fastRelativeMapper twice to identify changes since
			for (ComponentVersionBI version : latestDeveloperVersionMap.values())
			{
				ComponentVersionBI testingVersion = getCurrentVersion(concept, compType, version);

				RELATIVE_POSITION relPosition = conflictMapper.fastRelativePosition((Version)adjudicatorVersion, (Version)testingVersion, Terms.get().getActiveAceFrameConfig().getPrecedence());

				if (relPosition != RELATIVE_POSITION.EQUAL) 
				{
					if (relPosition == RELATIVE_POSITION.CONTRADICTION) {
						throw new TerminologyException("Can't be contra via adjud path");
					}
				
					if (relPosition == RELATIVE_POSITION.BEFORE)
					{
						// TODO: To remove post testing ... then remove 2 BEFORE check below to only relSecondPosition != RELATIVE_POSITION.BEFORE
						throw new TerminologyException("My first time here");
					}

					if (relPosition == RELATIVE_POSITION.AFTER || relPosition == RELATIVE_POSITION.BEFORE) 
					{
						
						// For Edge Case -- Time-based contradiction Idetifier
						RELATIVE_POSITION relSecondPosition = conflictMapper.fastRelativePosition((Version)adjudicatorVersion, (Version)testingVersion, Precedence.TIME);

						if (relSecondPosition == RELATIVE_POSITION.CONTRADICTION) {
							throw new TerminologyException("Can't be contra via adjud path");
						}				

						if (relSecondPosition != relPosition) 
						{
							if (!isSingleEdit) {
								isSingleEdit = true;
							} else {
								isContradiction = true;
							}
						}

						if (relPosition != RELATIVE_POSITION.BEFORE && relSecondPosition != RELATIVE_POSITION.BEFORE)
						{
							// Remove Pre-Adjudication Versions from Dev list
							foundPositionsVersions.remove(testingVersion);
						}
					}
				} 
			}
			
			return identifyContradictionResult(isContradiction, isSingleEdit, false, false);
     	}

	private ComponentVersionBI identifyLatestAdjudicationVersion(ComponentChroncileBI<?> comp) {
  		ComponentVersionBI latestAdjudicatedVersion = null;
 		
     	for (ComponentVersionBI part : comp.getVersions())
 		{
 			// Identify Adjudication versions and find latest 
 			if (part.getPathNid() == adjudicatorPathNid)
 			
 			{
 				if (latestAdjudicatedVersion  == null || part.getTime() > latestAdjudicatedVersion.getTime())
 				{
 					latestAdjudicatedVersion = part;
 				}
 			}
 		}

 		return latestAdjudicatedVersion;
 	}

     private ComponentVersionBI identifyLatestOriginVersion(ComponentChroncileBI<?> comp, PathBI originPath) throws IOException {
  		ComponentVersionBI latestOriginVersion = null;

     	for (ComponentVersionBI part : comp.getVersions())
 		{
			// Identify Origins versions and find latest
			if ((part.getPathNid() == originPathNid) || (isOriginVersion(originPath, part))) {
 				if (latestOriginVersion  == null || part.getTime() > latestOriginVersion.getTime()) {
 					latestOriginVersion = part;
 				}
 			}
 		}

 		return latestOriginVersion;
 	}

	private Map<Integer, ComponentVersionBI>  identifyLatestDeveloperVersions(ComponentChroncileBI<?> comp, PathBI originPath) throws IOException {
	    Map<Integer, ComponentVersionBI> latestDeveloperVersionMap = new HashMap<Integer, ComponentVersionBI>();

	    for (ComponentVersionBI part : comp.getVersions()) {
	    	boolean putIntoMap = false;
	    	Integer pathNidObj = null;
	    	
	    	if ((part.getPathNid() != adjudicatorPathNid) &&
				(part.getPathNid() != originPathNid) &&
				(!isOriginVersion(originPath, part))) 
			{
				// Identify Developer versions
				putIntoMap = true;
				pathNidObj = new Integer(part.getPathNid());
				ComponentVersionBI latestVersion = latestDeveloperVersionMap.get(pathNidObj);
				
				if (latestVersion != null) {
					if (latestVersion.getTime() > part.getTime()) {
						putIntoMap = false;
					}
				} 
			}
						
			if (putIntoMap) {
				latestDeveloperVersionMap.put(pathNidObj, part);
			}
		}
		
		return latestDeveloperVersionMap;
	}

	private boolean isOriginVersion(PathBI originPath, ComponentVersionBI testingVersion) throws IOException {
		for (PositionBI origin : originPath.getInheritedOrigins())
		{
			if (testingVersion.getPosition().isAntecedentOrEqualTo(origin))
				return true;
		}
		
		return false; 
	}


	private boolean isContradictionWithSameValues(AttributeComparer comparer, ComponentVersionBI baseVersion, Set<ComponentVersionBI> testVersions) throws TerminologyException
	{ 
		try {
			boolean isNewComponent = false;
			AttributeComparer secondaryComparer = comparer.getClass().newInstance();
			
			if (baseVersion != null) {
				comparer.initializeAttributes(baseVersion);
			} else {
				// New Component with multiple versions!
				isNewComponent = true;
				if (testVersions.size() < 2) {
			    	AceLog.getAppLog().log(Level.WARNING, "Shouldn't have single version without latestOriginVersion");
				}
			}
			
			for (ComponentVersionBI version : testVersions)
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
		} catch (Exception e) {
			throw new TerminologyException("Couldn't create Comparer class");
		}
				
		return true;
	}
			
		
			
    // For each version of a given component, ensure that a contradiction doesn't exist with another version of the any other component (view PositionMapper)
    private ContradictionResult areFoundPositionsReachable(
            Concept concept, 
            Map<PositionForSet, 
            HashMap<Integer, ComponentVersionBI>> foundPositionsMap) {
		// Todo: Per Position Found, only store one item 
		// TODO: Also remove CompNid as not needed?

    	ComponentVersionBI currentVersion = null;
        ComponentVersionBI testingVersion = null;

		try {
			for (PositionForSet currentPositionKey : foundPositionsMap.keySet())
			{
				HashMap<Integer, ComponentVersionBI> currentVersionMap = foundPositionsMap.get(currentPositionKey);
				currentVersion = currentVersionMap.get(currentVersionMap.keySet().iterator().next());
	
				for (PositionForSet testingPositionKey : foundPositionsMap.keySet())
				{
					if (!currentPositionKey.equals(testingPositionKey))
					{
						HashMap<Integer, ComponentVersionBI> testingVersionMap = foundPositionsMap.get(testingPositionKey);
						testingVersion = testingVersionMap.get(testingVersionMap.keySet().iterator().next());
	
						RELATIVE_POSITION retPosition = conflictMapper.fastRelativePosition((Version)currentVersion, (Version)testingVersion, Terms.get().getActiveAceFrameConfig().getPrecedence());
						
						if (retPosition == RELATIVE_POSITION.CONTRADICTION)
						{
							if (isSameVersionValues(currentVersion, testingVersion)) {
								return ContradictionResult.DUPLICATE_NEW_COMPONENT;
							} else {
								return ContradictionResult.CONTRADICTION;
							}
						}
					}
				}
			}
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Error in identifying if areFoundPositionsReachable");
		}
		
		return ContradictionResult.NONE;
	}


	private boolean isSameVersionValues(ComponentVersionBI testingVersion, ComponentVersionBI currentVersion)
	{
		Class<? extends ComponentVersionBI> testingClass = testingVersion.getClass();
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
      

    // For a component Type & Nid, get the version corresponding to the View Coordinate 
    private ComponentVersionBI getCurrentVersion(Concept concept, 
            ComponentType compType, ComponentVersionBI version) throws ContraditionException {
        ComponentVersionBI currentVersion = null;
        int componentNid = version.getNid();
		PositionBI developerVersionPosition = getPositionOfVersion(version);

		try
		{
			if (compType == ComponentType.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				Collection<ConceptAttributes.Version> currentAttributesVersions = attr.getVersions();

				for (ConceptAttributes.Version testingVersion : currentAttributesVersions)
				{
					if (testingVersion.getPosition().equals(developerVersionPosition))
					{
						currentVersion = testingVersion;
						break;
					}
				}
			}
			else if (compType == ComponentType.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				Collection<Description.Version> currentDescriptionVersions = desc.getVersions();
				
				for (Description.Version testingVersion : currentDescriptionVersions)
				{
					if (testingVersion.getPosition().equals(developerVersionPosition))
					{
						currentVersion = testingVersion;
						break;
					}
				}
			} 
			else if (compType == ComponentType.SRC_RELATIONSHIP || compType == ComponentType.DEST_RELATIONSHIP)
			{
				// Assume Source Rel first.  If returns null, 
				Relationship rel = concept.getRelationship(componentNid);
				if (rel == null) {
					rel = concept.getDestRel(componentNid);
					
				}
				
				Collection<Relationship.Version> currentRelationshipVersions = rel.getVersions();
				for (Relationship.Version testingVersion : currentRelationshipVersions)
				{
					if (testingVersion.getPosition().equals(developerVersionPosition))
					{
						currentVersion = testingVersion;
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

	private ComponentVersionBI getAdjudicatorVersion(Concept concept, ComponentType compType, ComponentVersionBI latestAdjudicatedVersion) throws ContraditionException 
	{
		int componentNid = latestAdjudicatedVersion.getNid();
		PositionBI adjudicatorVersionPosition = getPositionOfVersion(latestAdjudicatedVersion);

		ComponentVersionBI currentVersion = null;

		try
		{
			ViewCoordinate viewCoord = Terms.get().getActiveAceFrameConfig().getViewCoordinate();

			if (compType == ComponentType.ATTRIBUTE)
			{
				ConceptAttributes attr = concept.getConceptAttributes();
				currentVersion = attr.getVersion(viewCoord);
			}
			else if (compType == ComponentType.DESCRIPTION)
			{
				
				Description desc = concept.getDescription(componentNid);
				currentVersion = identifyDescriptionViewCoord(desc, viewCoord, adjudicatorVersionPosition);
			} 
			else if (compType == ComponentType.SRC_RELATIONSHIP || compType == ComponentType.DEST_RELATIONSHIP)
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
					if (version.getPosition().equals(adjudicatorVersionPosition))
					{
						currentVersion = version;
						break;
					}
				}
			} 			
		} catch (Exception e) {
	    	AceLog.getAppLog().log(Level.WARNING, "Error in identifying version of type: " + compType + "for component: " + componentNid);
	    	return null;
		} 
		
		return currentVersion;
	}


    private ComponentVersionBI identifyDescriptionViewCoord(Description desc, ViewCoordinate viewCoord, PositionBI position) throws IOException {
    	Collection<Description.Version> currentDescriptionVersions = desc.getVersions();
		
		for (Description.Version version : currentDescriptionVersions)
		{
			if (version.getPosition().equals(position))
			{
				 return version;
			}
		}
		
		return null;
	}

	// Get the Position of the current version
    private PositionBI getPositionOfVersion(ComponentVersionBI version) {
        PositionBI coordPosition = null;

		try
		{
			coordPosition = new Position(version.getTime(), Terms.get().getPath(version.getPathNid()));
		} catch (Exception e) { 
        	AceLog.getAppLog().log(Level.WARNING, "Error in accessing path: " + version.getPathNid(), e);
		}
		
		return coordPosition;
	}

    // For all versions of a componentId, add them individually to the "Found Positions" collection 
    private void updateFoundPositionsForAllVersions(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            Iterator<?> versions, 
            int componentNid, 
            ComponentType compType) throws ContraditionException {
        while (versions.hasNext()) {
            ComponentVersionBI v = (ComponentVersionBI) versions.next();

			PositionForSet pfsKey = new PositionForSet(v.getTime(), v.getPathNid());
			ComponentVersionBI developerVersion = getCurrentVersion(concept, compType, v);

			updateFoundPositionForSingleVersion(foundPositionsMap, pfsKey, componentNid, developerVersion);
        }
    }

    // Add version to found positions map
    private void updateFoundPositionForSingleVersion(
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            PositionForSet currentPos, 
            int componentNid, 
            ComponentVersionBI currentVersion) {
        if (!foundPositionsMap.containsKey(currentPos)) {
            // First time position viewed, create new map and add componentId-version map to position
            HashMap<Integer, ComponentVersionBI> newPositionMap = new HashMap<Integer, ComponentVersionBI>();
            newPositionMap.put(componentNid, currentVersion);
            foundPositionsMap.put(currentPos, newPositionMap);
        } else {
            // Position already found, add componentId-version map to position 
            HashMap<Integer, ComponentVersionBI> positionMap = foundPositionsMap.get(currentPos);
            positionMap.put(componentNid, currentVersion);
        }

    }


    // FOR REFSETS
    /*
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


    // FOR REFSETS
    private ComponentVersionBI getCurrentRefsetVersion(ComponentVersionBI version) {
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
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            int componentNid, 
            ComponentType compType) throws TerminologyException, IOException, ParseException {
        List<? extends I_ExtendByRef> members = Terms.get().getAllExtensionsForComponent(componentNid);
        ContradictionResult position = ContradictionResult.NONE;
        boolean isUnreachable = false;
        boolean isSingleEdit = false;

        for (I_ExtendByRef member : members) {
            if (member.getMutableParts().size() > 2) {
                ComponentVersionBI lcaDiscoveredVersion = identifyLeastCommonAncestorVersion(member.getMutableParts().iterator());
                int leastCommonAncestorHashCode = getLeastCommonRefsetAncestorHashCode(lcaDiscoveredVersion, member);
                int secondaryHashCode = -1;

                for (I_ExtendByRefPart v : member.getMutableParts()) {
                    PositionBI p = new Position(v.getTime(), Terms.get().getPath(v.getPathNid()));

                    if ((p.isSubsequentOrEqualTo(leastCommonAncestorForViewPosition)) && leastCommonAncestorHashCode != v.getPartsHashCode()) {
                        if (secondaryHashCode < 0) {
                            position = ContradictionResult.SINGLE_MODELER_CHANGE;
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
                // position = areFoundPositionsReachable(concept, foundPositionsMap, member.getMutableParts(), ComponentType.REFSET, componentNid);
            }

            if (position.equals(ContradictionResult.CONTRADICTION)) {
                return position;
            } else if (position.equals(ContradictionResult.UNREACHABLE)) {
                isUnreachable = true;
            } else if (position.equals(ContradictionResult.SINGLE_MODELER_CHANGE)) {
                isSingleEdit = true;
            }
        }

        if (isUnreachable) {
            return ContradictionResult.UNREACHABLE;
        } else if (isSingleEdit) {
            return ContradictionResult.SINGLE_MODELER_CHANGE;
        } else {
            return ContradictionResult.NONE;
        }
    }

*
*
*    private static PositionBI leastCommonAncestorForViewPosition = null;
    leastCommonAncestorForViewPosition = determineLeastCommonAncestor(sortedOrigins);
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


*/
}


