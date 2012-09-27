/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.binding.snomed;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class Taxonomies.
 */
public class Taxonomies {

	/** The wb aux. */
	public static ConceptSpec WB_AUX = 
		new ConceptSpec("Terminology Auxiliary concept", 
				UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029"));
	
	/** The refset aux. */
	public static ConceptSpec REFSET_AUX = 
		new ConceptSpec("Refset Auxiliary Concept", 
				UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5"));
	
	/** The queue type. */
	public static ConceptSpec QUEUE_TYPE = 
		new ConceptSpec("Queue Type", 
				UUID.fromString("fb78d89e-6953-3456-8903-8ee9d25539bc"));
	
	/** The snomed. */
	public static ConceptSpec SNOMED = 
		new ConceptSpec("SNOMED CT Concept", 
				         UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
}
