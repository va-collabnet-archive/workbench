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
package org.dwfa.mojo;

/**
 * Defines the rf2 module for a Position.
 */
public class Rf2ModuleDescriptor extends PositionDescriptor {
    private ConceptDescriptor module;
    private String moduleTimeString;

    /**
     * @return the module
     */
    public final ConceptDescriptor getModule() {
        return module;
    }

    /**
     * @param module the module to set
     */
    public final void setModule(ConceptDescriptor module) {
        this.module = module;
    }

    /**
     * @return the moduleTimeString
     */
    public final String getModuleTimeString() {
        return moduleTimeString;
    }

    /**
     * @param moduleTimeString the moduleTimeString to set
     */
    public final void setModuleTimeString(String moduleTimeString) {
        this.moduleTimeString = moduleTimeString;
    }

    /**
     * @see org.dwfa.mojo.PositionDescriptor#toString()
     */
    public String toString() {
        return super.toString() + " Module: " + module + " position: " + moduleTimeString;
    }
}
