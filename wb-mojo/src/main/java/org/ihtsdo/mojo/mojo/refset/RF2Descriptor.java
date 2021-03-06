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
package org.ihtsdo.mojo.mojo.refset;

import org.ihtsdo.mojo.mojo.ConceptDescriptor;

public class RF2Descriptor {

    /**
     * The module that this refset belongs to. e.g. AU pathology module.
     */
    ConceptDescriptor module;

    /**
     * The content sub type - this is National, Local or Core.
     */
    String contentSubType;

    /**
     * The two digit country code.
     */
    String countryCode;

    public ConceptDescriptor getModule() {
        return module;
    }

    public void setModule(ConceptDescriptor module) {
        this.module = module;
    }

    public String getContentSubType() {
        return contentSubType;
    }

    public void setContentSubType(String contentSubType) {
        this.contentSubType = contentSubType;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
