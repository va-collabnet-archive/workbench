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
package org.ihtsdo.mojo.mojo;

/**
 * <h1>ComparisonSpec</h1>
 * <p>
 * This class is used to create an object to hold configuration details used by
 * the {@link CompareFileContent} mojo.
 * </p>
 * 
 * @see org.ihtsdo.mojo.mojo.CompareFileContent
 * 
 * 
 */
public class ComparisonSpec {
    private String comparisonBase = null;
    private String delimeter = null;
    private String excludedFields = null;

    public void setComparisonBase(String base) {
        this.comparisonBase = base;
    }

    public String getComparisonBase() {
        return this.comparisonBase;
    }

    public void setDelimeter(String delimeter) {
        this.delimeter = delimeter;
    }

    public String getDelimeter() {
        return this.delimeter;
    }

    public void setExcludedFields(String excludedFields) {
        this.excludedFields = excludedFields;
    }

    public String getExcludedFields() {
        return this.excludedFields;
    }

    public ComparisonSpec() {
        super();
    }

}// End class ComparisonSpec
