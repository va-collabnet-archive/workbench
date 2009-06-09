package org.dwfa.ace.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.tapi.TerminologyException;

/**
 * Interface for classes that resolve conflicts - i.e. the scenario where two
 * different paths have different data for a given component.
 * <p>
 * Given a component's tuples and an optional time point (latest is assumed without a
 * time point), implementations of this interface will calculate the "conflict
 * resolved view" of the component at that time point.
 */
public interface I_ManageConflict extends Serializable {
	/**
	 * Method to get the display name of this conflict resolution strategy.
	 * Note that this is intended to be something meaningful to an end user
	 * attempting to choose a conflict resolution strategy.
	 * 
	 * @return The display name of this conflict resolution strategy
	 */
	String getDisplayName();
	
	/**
	 * Method to get a description of this conflict resolution strategy.
	 * Note that this is intended to be something meaningful to an end user
	 * attempting to choose a conflict resolution strategy. This content
	 * may contain XHTML markup for readability.
	 * 
	 * @return The display name of this conflict resolution strategy
	 */
	String getDescription();
	
	/**
	 * Resolves the supplied tuples, which may be from more than one entity,
	 * to a conflict resolved latest state.
	 * <p>
	 * Best case this will resolve to one tuple, however this will depend
	 * upon the data and the resolution strategy in use.
	 * <p>
	 * Note that the input list of tuples will not be modified by this method.
	 * 
	 * @param tuples
	 * @return tuples resolved as per the resolution strategy
	 */
	<T extends I_AmTuple> List<T> resolveTuples(List<T> tuples);

	/**
	 * Resolves the supplied parts to a conflict resolved latest state.
	 * <p>
	 * Best case this will resolve to one part, however this will depend
	 * upon the data and the resolution strategy in use.
	 * <p>
	 * Note that the input list of parts will not be modified by this method.
	 * <p>
	 * <strong>
	 * NB This method requires that all the parts are from the same entity! If
	 * they are not there is no way for this method to determine that and 
	 * resolution will take place assuming they are all from the same entity.
	 * </strong>
	 * 
	 * @param parts
	 * @return parts resolved as per the resolution strategy
	 */
	<T extends I_AmPart> List<T> resolveParts(List<T> parts);

	/**
	 * @param concept concept to test
	 * @return true if this concept is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_GetConceptData concept) throws IOException, TerminologyException;

	/**
	 * @param concept concept to test
	 * @param includeDependentEntities indicates that this concept should be considered
	 * 		in conflict if the concept's parts of any of its dependent objects (descriptions,
	 * 		relationships, extensions...etc) are in conflict.
	 * @return true if this concept is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_GetConceptData concept, boolean includeDependentEntities) throws IOException, TerminologyException;
	
	/**
	 * @param description description to test
	 * @return true if this description is in conflict according to the resolution strategy
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	boolean isInConflict(I_DescriptionVersioned description) throws IOException, TerminologyException;

	/**
	 * @param extension extension to test
	 * @return true if this extension is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_ThinExtByRefVersioned extension) throws TerminologyException, IOException;
	
	/**
	 * @param id id to test
	 * @return true if this id is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_IdVersioned id) throws IOException, TerminologyException;

	/**
	 * @param image image to test
	 * @return true if this image is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_ImageVersioned image) throws IOException, TerminologyException;

	/**
	 * @param relationship relationship to test
	 * @return true if this relationship is in conflict according to the resolution strategy
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	boolean isInConflict(I_RelVersioned relationship) throws IOException, TerminologyException;

	/**
	 * @param conceptAttribute concept attribute to test
	 * @return true if this conceptAttribute is in conflict according to the resolution strategy
	 * @throws IOException 
	 * @throws TerminologyException 
	 */
	boolean isInConflict(I_ConceptAttributeVersioned conceptAttribute) throws TerminologyException, IOException;
}
