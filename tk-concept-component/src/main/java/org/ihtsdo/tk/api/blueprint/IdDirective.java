/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.tk.api.blueprint;

/**
 *
 * @author kec
 * 
 * 
 * 
 */
public enum IdDirective {
    /**
     * Directive for a blueprint which will have the same component identifiers as the component(s) 
     * from which the blueprints were derived.<br>
     * <br>
     * The use case for this behavior is when using a blueprint to modify 
     * a field of an existing component.
     */
    PRESERVE, 
    /**
     * GENERATE_RANDOM use is discouraged. 
     * 
     * Use one of another IdDirective whenever possible.
     */
    GENERATE_RANDOM, 
    /**
     * Directive to create a component id based on the blueprint's 
     * standard hash algorithm for that component type.
     * 
     * This directive is the preferred method for generating new content,
     * when a N-to-1 (where N &lt;= 1) relationship exists 
     * between a potential refex member and the referenced component.
     * 
     * N is the potential number of refex members for any given component.
     * 
     * (No more than one refex member per referenced component.)
     */
    GENERATE_HASH, 
    /**
     * Directive to create a component id based on the blueprint's 
     * standard hash algorithm for that component type.<br>
     * 
     * This directive is the preferred method for generating new content,
     * when N-to-1 (where N == 0 or N >= 1) relationship exists 
     * between a potential refex member and the referenced component.
     * 
     * N is the potential number of refex members for any given component.
     * 
     * (No more than one refex member per referenced component.)
     */
    GENERATE_REFEX_CONTENT_HASH, 
    /**
     * Used in CloneConcept.  Not expected to be used any place else.
     * 
     */
    GENERATE_RANDOM_CONCEPT_REST_HASH,
    /**
     * Use when adding new components to an existing concept using blueprints.
     */
    PRESERVE_CONCEPT_REST_HASH
}
