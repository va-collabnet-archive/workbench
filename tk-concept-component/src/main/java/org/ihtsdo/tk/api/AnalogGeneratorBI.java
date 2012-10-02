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
package org.ihtsdo.tk.api;

/**
 * The Interface AnalogGeneratorBI can be used for generating analogs of components. 
 * It is preferable to use a blueprint rather than an analog when creating or modifying components.
 *
 * @param <T> The type of object returned by the analog generator.
 * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint
 */
public interface AnalogGeneratorBI <T extends AnalogBI> {
        
        /**
         * Creates an analog based on a component and using the given values.
         *
         * @param statusNid the nid representing the new status
         * @param time the time to be associated with the new component
         * @param authorNid the nid representing the new author
         * @param moduleNid the nid representing the new module
         * @param pathNid the nid representing the new path
         * @return the generic type of an object returned by the analog generator
         */
        T makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid);
}
