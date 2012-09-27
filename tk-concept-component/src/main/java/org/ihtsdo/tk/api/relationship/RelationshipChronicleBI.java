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
package org.ihtsdo.tk.api.relationship;

import org.ihtsdo.tk.api.ComponentChronicleBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface RelationshipChronicleBI.
 */
public interface RelationshipChronicleBI extends ComponentChronicleBI<RelationshipVersionBI> {

    /**
     * Gets the source nid.
     *
     * @return the source nid
     */
    int getSourceNid();

    /**
     * Gets the target nid.
     *
     * @return the target nid
     */
    int getTargetNid();
}
