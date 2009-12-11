/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.api;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;



public interface I_GetConceptData extends I_AmTermComponent {

	public I_ConceptAttributeVersioned getConceptAttributes() throws IOException;

	public int getConceptId();

	public List<I_ConceptAttributeTuple> getConceptAttributeTuples(I_IntSet allowedStatus,
			Set<I_Position> positions) throws IOException;

   public List<I_DescriptionTuple> getDescriptionTuples(I_IntSet allowedStatus,
         I_IntSet allowedTypes, Set<I_Position> positions) throws IOException;

   public List<I_ImageTuple> getImageTuples(I_IntSet allowedStatus,
         I_IntSet allowedTypes, Set<I_Position> positions) throws IOException;

	public List<I_RelTuple> getSourceRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted)
			throws IOException;

	public List<I_RelTuple> getDestRelTuples(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted)
			throws IOException;

	public Set<I_GetConceptData> getSourceRelTargets(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted)
			throws IOException;

	public Set<I_GetConceptData> getDestRelOrigins(I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions, boolean addUncommitted)
			throws IOException;

	public List<I_DescriptionVersioned> getDescriptions() throws IOException;

	public List<I_RelVersioned> getDestRels() throws IOException;

	public List<I_RelVersioned> getSourceRels() throws IOException;

	public String getInitialText() throws IOException;

	public boolean isLeaf(I_ConfigAceFrame aceConfig, boolean addUncommitted) throws IOException;

	public List<I_ImageVersioned> getImages() throws IOException;

	public List<UUID> getUids() throws IOException;

	public List<I_DescriptionVersioned> getUncommittedDescriptions();
	
	public List<I_RelVersioned> getUncommittedSourceRels();
	
	public I_ConceptAttributeVersioned getUncommittedConceptAttributes();
	
	public List<I_ImageVersioned> getUncommittedImages();

	public I_IdVersioned getId() throws IOException;

   public I_DescriptionTuple getDescTuple(I_IntList treeDescPreferenceList, 
         I_IntSet allowedStatus, 
         Set<I_Position> positions) throws IOException;

   public I_DescriptionTuple getDescTuple(I_IntList treeDescPreferenceList, I_ConfigAceFrame config) throws IOException;

	public I_IntSet getUncommittedIds();
	
	public UniversalAceBean getUniversalAceBean() throws IOException, TerminologyException;
	
	public boolean isParentOf(I_GetConceptData child, I_IntSet allowedStatus,
			I_IntSet allowedTypes, Set<I_Position> positions,
			boolean addUncommitted) throws IOException;
}
