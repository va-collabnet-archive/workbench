package org.ihtsdo.concept;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.component.ConceptComponent.Version;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.refsetmember.str.StrMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.computer.version.PositionMapper;
import org.ihtsdo.db.bdb.computer.version.PositionMapper.RELATIVE_POSITION;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.contradiction.ComponentType;
import org.ihtsdo.tk.contradiction.ContradictionIdentifierBI;
import org.ihtsdo.tk.contradiction.ContradictionResult;
import org.ihtsdo.tk.contradiction.PositionForSet;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class ContradictionIdentifier implements ContradictionIdentifierBI {

     // Class Variables
    private PositionMapper conflictMapper;

	private AtomicInteger viewPathNid = null;
	private AtomicInteger commonOriginPathNid;
	private AtomicInteger workflowRefsetNid;
	private AtomicBoolean isReturnVersionsUseCase;
	private ViewCoordinate viewCoord;
	private Set<ComponentVersionBI> returnVersionCollection;

	private ComponentVersionBI singleVersion;
	
	public ContradictionIdentifier(ViewCoordinate vc, boolean useCase) {
		viewCoord = vc;
		isReturnVersionsUseCase = new AtomicBoolean(useCase);
	}
	
	// For a given concept, look at a set of components at a time.
    // If contradiction found, return immediatly.  
    // If single change found, save state and only return state if no contradiction found in each concept's component type
    @Override
    public ContradictionResult isConceptInConflict(ConceptChronicleBI conceptChronicle) {
        boolean isSingleEdit = false;
        boolean isDuplicateEdit = false;
        boolean isDuplicateNew = false;
        
        Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap = new HashMap<PositionForSet, HashMap<Integer, ComponentVersionBI>>();

        for (PositionBI pos : viewCoord.getPositionSet())
        {
        	try {
            	initializeViewPos(pos);
            
		        ContradictionResult result = analyzeContradictionPerComponent((Concept)conceptChronicle, foundPositionsMap);
	    
		        if (result == ContradictionResult.CONTRADICTION) {
		        	return result;
		        } else {
		            // Ensure new changes are visible to one another.  
		            // Otherwise, is a contradiction or possible duplicate new component
			        ContradictionResult foundPosResult = areFoundPositionsReachable((Concept)conceptChronicle, foundPositionsMap);
			
			        if (foundPosResult.equals(ContradictionResult.CONTRADICTION)) {
			            return ContradictionResult.CONTRADICTION;
				    } else if (result == ContradictionResult.DUPLICATE_EDIT) {
				            isDuplicateEdit = true;
			        } else if (foundPosResult.equals(ContradictionResult.DUPLICATE_NEW)) {
				    	isDuplicateNew = true;
				    } else if (result == ContradictionResult.SINGLE_MODELER_CHANGE) {
				        isSingleEdit = true;
			        }
			    }
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.WARNING, "Failure in detecting conflict on concept: " + conceptChronicle.getPrimUuid());
		    	return ContradictionResult.ERROR;
		    }
        }

        if (isDuplicateEdit) {
            return ContradictionResult.DUPLICATE_EDIT;
        } else if (isDuplicateNew) {
            return ContradictionResult.DUPLICATE_NEW;
        } else if (isSingleEdit) {
			if (isReturnVersionsUseCase.get()) {
				returnVersionCollection.add(singleVersion);
			}
            return ContradictionResult.SINGLE_MODELER_CHANGE;
        } else {
			return ContradictionResult.NONE;
	    }
    }
    
    private ContradictionResult analyzeContradictionPerComponent(Concept concept, Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap) throws Exception 
    {
    	boolean isDuplicateEdit = false;
		boolean isSingleEdit = false;
		ContradictionResult result = ContradictionResult.NONE;
    	
		for (ComponentType type : ComponentType.values()) {
			if (type != ComponentType.REFSET) 
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
	        return ContradictionResult.DUPLICATE_NEW;
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
        	((RelationshipAttributeComparer)comparer).setRelationshipType(ComponentType.SRC_RELATIONSHIP);
            componentTuples = concept.getSourceRels();
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
        boolean isContradiction = false;
        ContradictionResult result = ContradictionResult.NONE;
	    
	    ComponentVersionBI latestOriginVersion = null;
	    ComponentVersionBI latestAdjudicatedVersion = null; 
	    Set<ComponentVersionBI> developerVersions = new HashSet<ComponentVersionBI>();
		
	    ComponentType compType = comparer.getComponentType();
        
        try {
			// Examine Refsets.  Only continue to concept's components if refset check doesn't identify contradiction
			ContradictionResult refsetResult = refsetMembershipConflictFound(concept, foundPositionsMap, comp.getNid());
			
			if (refsetResult == ContradictionResult.CONTRADICTION) {
				isContradiction = true;
			} else if (refsetResult == ContradictionResult.SINGLE_MODELER_CHANGE) {
				isSingleEdit = true;
        	} else if (refsetResult == ContradictionResult.DUPLICATE_EDIT){
        		isDuplicateEdit = true;
        	} 
			
        	if (refsetResult != ContradictionResult.CONTRADICTION)
			{
				PathBI originPath = Terms.get().getPath(commonOriginPathNid.get());
	
				latestAdjudicatedVersion = identifyLatestAdjudicationVersion(comp);
				latestOriginVersion = identifyLatestOriginVersion(comp, originPath);
			    Map<Integer, ComponentVersionBI> latestDeveloperVersionMap = identifyLatestDeveloperVersions(comp, originPath);
				
				if (!latestDeveloperVersionMap.isEmpty()) {
					if (latestAdjudicatedVersion != null) { 
						// if adjudication has been performed on componentId
						result = handleAdjudication(concept, compType, latestAdjudicatedVersion, latestOriginVersion, latestDeveloperVersionMap, developerVersions);
					} else {
						// if never had adjudication performed on componentId
						result = handleNonAdjudication(latestDeveloperVersionMap, developerVersions);
					}
		
		        	if (result == ContradictionResult.SINGLE_MODELER_CHANGE) {
						isSingleEdit = true;
						singleVersion = developerVersions.iterator().next();
		        	} else if (result == ContradictionResult.CONTRADICTION)	{
			        	// Have potential contradiction, unless developers made same changes
						ComponentVersionBI compareWithVersion = latestOriginVersion;
						if (latestAdjudicatedVersion != null) {
							compareWithVersion = latestAdjudicatedVersion;
						} 
						
						// Check if for given CompId, have multiple versions with same changes
						if (!isContradictionWithSameValues(comparer, compareWithVersion, developerVersions)) {
							// Concept contains contradiction.  No need to update positions as check complete for concept
							isContradiction = true;
						} else {
							isDuplicateEdit = true;
						}
					} 
	
					// Add Position to List
		        	if (!isContradiction) {
		        		boolean success = updateFoundPositionsForAllVersions(concept, foundPositionsMap, developerVersions.iterator(), comp.getNid(), compType);
						
						if (!success) {
							return ContradictionResult.ERROR;
						}
		        	}
				}
	    	}
		} catch (ContraditionException ce) {
			return ContradictionResult.CONTRADICTION;
		} 
		
		return identifyContradictionResult(isContradiction, isSingleEdit, isDuplicateEdit, false);
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
					
					if (isReturnVersionsUseCase.get()) {
						returnVersionCollection.add(initialVersion);
						returnVersionCollection.add(version);
					}
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
			ComponentVersionBI adjudicatorVersion = latestAdjudicatedVersion;
			
			if (compType != ComponentType.REFSET) {
				adjudicatorVersion = getAdjudicatorVersion(concept, compType, latestAdjudicatedVersion);
			} 

			// Using Adj Versions, run fastRelativeMapper twice to identify changes since
			for (ComponentVersionBI version : latestDeveloperVersionMap.values())
			{
				ComponentVersionBI testingVersion = version;
				
				if (compType != ComponentType.REFSET) {
					// TODO: Do I need this or is it same object??? (latestAdjudicatedVersion & adjudicatorVersion)
					testingVersion = getCurrentVersion(concept, compType, version);
				}

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

					if (relPosition == RELATIVE_POSITION.AFTER) 
					{
						if (isReturnVersionsUseCase.get()) {
							returnVersionCollection.add(testingVersion);
						}
			/*
			 * TODO: Unnecessary?			
			 */
//						// For Edge Case -- Time-based contradiction Idetifier
//						RELATIVE_POSITION relSecondPosition = conflictMapper.fastRelativePosition((Version)adjudicatorVersion, (Version)testingVersion, Precedence.TIME);
//
//						if (relSecondPosition == RELATIVE_POSITION.CONTRADICTION) {
//							throw new TerminologyException("Can't be contra via adjud path");
//						}				
//
//						if (relSecondPosition != relPosition) 
//						{
							
							if (!isSingleEdit) {
								isSingleEdit = true;
							} else {
								isContradiction = true;
							}
//						}

//						if (relPosition != RELATIVE_POSITION.BEFORE && relSecondPosition != RELATIVE_POSITION.BEFORE)
//						{
//							// Remove Pre-Adjudication Versions from Dev list
//							foundPositionsVersions.remove(testingVersion);
//						}
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
 			if (part.getPathNid() == viewPathNid.get())
 			
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
			if ((part.getPathNid() == commonOriginPathNid.get()) || (isOriginVersion(originPath, part))) {
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
	    	
	    	if ((part.getPathNid() != viewPathNid.get()) &&
				(part.getPathNid() != commonOriginPathNid.get()) &&
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
				if (isNewComponent && !comparer.isInitialized()) {
					comparer.initializeAttributes(version);
				} else {
					if (!comparer.hasSameAttributes(version))
					{
						if (!isNewComponent) {
						if (!secondaryComparer.isInitialized())
						{
							secondaryComparer.initializeAttributes(version);
						}
						else if (!secondaryComparer.hasSameAttributes(version))
						{
							return false;
						}
						} else {
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
							if (isReturnVersionsUseCase.get()) {
								returnVersionCollection.add(testingVersion);
								returnVersionCollection.add(currentVersion);
							}
							
							if (isSameVersionValues(currentVersion, testingVersion)) {
								return ContradictionResult.DUPLICATE_NEW;
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
			else if (compType == ComponentType.SRC_RELATIONSHIP)
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
			} else if (compType == ComponentType.REFSET) {
				I_ExtendByRefVersion member = (I_ExtendByRefVersion)version;
				
				currentVersion = member;
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
			else if (compType == ComponentType.SRC_RELATIONSHIP)
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
    private boolean updateFoundPositionsForAllVersions(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, 
            Iterator<?> versions, 
            int componentNid, 
            ComponentType compType) throws ContraditionException {
        while (versions.hasNext()) {
            ComponentVersionBI v = (ComponentVersionBI) versions.next();

			PositionForSet pfsKey = new PositionForSet(v.getTime(), v.getPathNid());
			ComponentVersionBI developerVersion = getCurrentVersion(concept, compType, v);

			if (developerVersion == null) {
				return false;
        	}
			updateFoundPositionForSingleVersion(foundPositionsMap, pfsKey, componentNid, developerVersion);
        }
        
        return true;
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

    private ContradictionResult refsetMembershipConflictFound(
            Concept concept, 
            Map<PositionForSet, HashMap<Integer, ComponentVersionBI>> foundPositionsMap, int componentNid) throws TerminologyException, IOException, ContraditionException 
	{
        boolean isSingleEdit = false;
        boolean isDuplicateEdit = false;
        boolean isContradiction = false;
	    ContradictionResult result = ContradictionResult.NONE;

	    ComponentVersionBI latestAdjudicatedVersion = null;
	    ComponentVersionBI latestOriginVersion = null;
	    Map<Integer, ComponentVersionBI> latestDeveloperVersionMap = new HashMap<Integer, ComponentVersionBI>();
	    Set<ComponentVersionBI> developerVersions = new HashSet<ComponentVersionBI>();

	    PathBI originPath = Terms.get().getPath(commonOriginPathNid.get());
        List<? extends I_ExtendByRef> members = Terms.get().getAllExtensionsForComponent(componentNid);

        for (I_ExtendByRef member : members) 
        {
 			// Identify Adjudication versions and find latest 
         	for (ComponentVersionBI part : member.getMutableParts()) {
     			if (part.getPathNid() == viewPathNid.get()) {
     				if (latestAdjudicatedVersion  == null || part.getTime() > latestAdjudicatedVersion.getTime()) {
     					latestAdjudicatedVersion = part;
                    }
                }
            }

			// Identify Origins versions and find latest
         	for (ComponentVersionBI part : member.getMutableParts()) {
    			if ((part.getPathNid() == commonOriginPathNid.get()) || (isOriginVersion(originPath, part))) {
     				if (latestOriginVersion  == null || part.getTime() > latestOriginVersion.getTime()) {
     					latestOriginVersion = part;
		            }
		        }
     		}

         	// Identify all developer edits and per path, get latest developer version
    	    for (ComponentVersionBI part : member.getMutableParts()) 
    	    {
    	    	boolean putIntoMap = false;
    	    	Integer pathNidObj = null;

    	    	if ((part.getPathNid() != viewPathNid.get()) &&
    				(part.getPathNid() != commonOriginPathNid.get()) &&
    				(!isOriginVersion(originPath, part))) 
    	    	{
    				// Identify Latest Developer version
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
    				// Overwrite current latest version
    				latestDeveloperVersionMap.put(pathNidObj, part);
	            }
	        }

			if (!latestDeveloperVersionMap.isEmpty()) 
			{
				if (latestAdjudicatedVersion != null) { 
					// if adjudication has been performed on componentId
					result = handleAdjudication(concept, ComponentType.REFSET, latestAdjudicatedVersion, latestOriginVersion, latestDeveloperVersionMap, developerVersions);
				} else {
					// if never had adjudication performed on componentId
					result = handleNonAdjudication(latestDeveloperVersionMap, developerVersions);
	            }
	
	        	if (result == ContradictionResult.SINGLE_MODELER_CHANGE) {
					isSingleEdit = true;
					singleVersion = developerVersions.iterator().next();
	        	} else if (result == ContradictionResult.CONTRADICTION)	{
		        	// Have potential contradiction, unless developers made same changes
					ComponentVersionBI compareWithVersion = latestOriginVersion;
					if (latestAdjudicatedVersion != null) {
						compareWithVersion = latestAdjudicatedVersion;
					}
					
					// Check if for given CompId, have multiple versions with same changes
					if (!isContradictionWithSameValues(new RefsetAttributeComparer(), compareWithVersion, developerVersions)) 
					{
						// Concept contains contradiction.  No need to update positions as check complete for concept
						isContradiction = true;
						break;
		            } else {
						isDuplicateEdit = true;
	                }
	            }
	
	        	// Add Position to List
	        	if (!isContradiction) {
					boolean success = updateFoundPositionsForAllVersions(concept, foundPositionsMap, developerVersions.iterator(), member.getMemberId(), ComponentType.REFSET);
					
					if (!success) {
						return ContradictionResult.ERROR;
					}
	        	}
	        }
        }

        return identifyContradictionResult(isContradiction, isSingleEdit, isDuplicateEdit, false);
    }

	private PositionBI determineLeastCommonAncestor(Set<HashSet<PositionBI>> originsByVersion)
	{
		Set<PositionBI> testingAncestors = new HashSet<PositionBI>(); 

		HashSet<PositionBI> testingSet = originsByVersion.iterator().next();
		originsByVersion.remove(testingSet);
		
		for (PositionBI testingPos : testingSet)
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


	private WorkflowHistoryJavaBean createWfHxJavaBean(I_ExtendByRefVersion refsetVersion) {
		try {
			StrMember.Version stringVersion = (StrMember.Version)refsetVersion;
			return WorkflowHelper.populateWorkflowHistoryJavaBean(refsetVersion.getMemberId(),
					   											  Terms.get().nidToUuid(refsetVersion.getReferencedComponentNid()), 
															      stringVersion.getStringValue(), 
															      refsetVersion.getTime());
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to read WfHx Java Bean from Refset Version");
		}
		
		return null;
	}


	// Initialize the PositionMapper used for identifying contradictions
	private void initializeViewPos(PositionBI pos) {
		try 
		{
        	setViewPos(pos);
        	
            conflictMapper = Bdb.getSapDb().getMapper(pos);

            if (!conflictMapper.isSetup()) {
            	conflictMapper.queueForSetup();
            }
            
            // find the least common ancestor based on view position
            PathBI commonOriginPath = identifyCommonOriginPosition(pos).getPath();
			commonOriginPathNid = new AtomicInteger(commonOriginPath.getConceptNid());

			workflowRefsetNid = new AtomicInteger(Terms.get().uuidToNative(RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid()));
			
			if (isReturnVersionsUseCase.get()) {
				returnVersionCollection = new HashSet<ComponentVersionBI>();
			}
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failure to Initialize Globals", e);
        }
	}

    private PositionBI identifyCommonOriginPosition(PositionBI viewPos) throws TerminologyException, IOException {
        // Identify leastCommonAncestor of View Position from Origins of view position 
        Set<HashSet<PositionBI>> groupedOriginsOfOrigins = new HashSet<HashSet<PositionBI>>();

        // While single origin, go up before finding common origin.  Because the 
        // recursive instances of a single origin directly over the view position is a false-positive.
		while (viewPos.getPath().getOrigins().size() == 1) {
			viewPos = viewPos.getPath().getOrigins().iterator().next();
		}
		
		// Ignoring actual viewPos as that will always be leastCommonAncestor by definition
		for (PositionBI origin : viewPos.getPath().getOrigins()) {
			if (!origin.equals(viewPos)) {
				HashSet<PositionBI> s = new HashSet<PositionBI>();
				s.add(origin);
				s.addAll(origin.getPath().getInheritedOrigins());
				groupedOriginsOfOrigins.add(s);
			}
		}
		
		// Filter out anything origin of LCA
		return determineLeastCommonAncestor(groupedOriginsOfOrigins);
	}


	private void setViewPos(PositionBI position) {
		// Note, adjudications are assumed to be based on view Path
		viewPathNid = new AtomicInteger(position.getPath().getConceptNid());
	}

	@Override
	public Collection<? extends ComponentVersionBI> getReturnVersions() {
		return returnVersionCollection;
	}
}


