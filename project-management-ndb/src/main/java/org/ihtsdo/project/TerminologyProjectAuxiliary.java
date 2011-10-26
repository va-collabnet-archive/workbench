/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project;

import java.util.UUID;

/**
 * The Class TerminologyProjectAuxiliary.
 * @deprecated merged with cement hierarchies in ArchitectonicAuxiliary February 09 2010
 */
public class TerminologyProjectAuxiliary {
	
	/**
	 * The Enum Concept.
	 */
	public enum Concept {
	    
    	/** The PROJECT s_ root. */
    	PROJECTS_ROOT				(UUID.fromString("b9390524-d1ee-3871-bf53-e8607ea004f3")),
	    
    	/** The PROJEC t_ extensio n_ refset. */
    	PROJECT_EXTENSION_REFSET	(UUID.fromString("903773c7-38c9-3c3a-87ac-8c46aedd77a7")),
	    
    	/** The WORKSET s_ root. */
    	WORKSETS_ROOT				(UUID.fromString("3d94eb21-f593-31df-b9de-e9e20928283e")),
	    
    	/** The WORKSE t_ extensio n_ refset. */
    	WORKSET_EXTENSION_REFSET	(UUID.fromString("895d42ee-2944-36f0-8462-8731e152c8fc")),
	    
    	/** The WORKLIST s_ root. */
    	WORKLISTS_ROOT				(UUID.fromString("13af6ec7-3f8e-32de-b13d-0288486e580e")),
	    
    	/** The WORKLIS t_ extensio n_ refset. */
    	WORKLIST_EXTENSION_REFSET	(UUID.fromString("4f697f04-895f-32ae-a21c-4d61dfafd12d")),
	    
    	/** The INCLUDE s_ fro m_ attribute. */
    	INCLUDES_FROM_ATTRIBUTE 	(UUID.fromString("9ee43d53-9e3d-3440-884d-1bcda413c752"));
//	    PROJECTS_ROOT				(UUID.fromString("e2e6d9aa-e671-3a74-81f7-bbd91f4bed4c")),
//	    PROJECT_EXTENSION_REFSET	(UUID.fromString("4c3abe03-0ee6-3531-a06f-178e76817929")),
//	    WORKSETS_ROOT				(UUID.fromString("99fb3132-c6af-38c0-9393-c8e5e715750e")),
//	    WORKSET_EXTENSION_REFSET	(UUID.fromString("6c94335c-4eb8-3680-89bc-490dfcaae524")),
//	    WORKLISTS_ROOT				(UUID.fromString("b4a1d476-8719-3a7f-a925-d41d82d3b572")),
//	    WORKLIST_EXTENSION_REFSET	(UUID.fromString("b215c0c9-c93c-3764-8b60-473a40d20c44")),
//	    INCLUDES_FROM_ATTRIBUTE 	(UUID.fromString("5bb63aeb-def6-33e9-9fbe-a6c61f92ec3b"));


	    /** The uid. */
private final UUID uid;
	    
	    /**
    	 * Instantiates a new concept.
    	 * 
    	 * @param uid the uid
    	 */
    	Concept(UUID uid) {
	        this.uid = uid;
	    }
	    
	    /**
    	 * Gets the uid.
    	 * 
    	 * @return the uid
    	 */
    	public UUID getUid() {
	    	return uid;
	    }
	}
	
}
