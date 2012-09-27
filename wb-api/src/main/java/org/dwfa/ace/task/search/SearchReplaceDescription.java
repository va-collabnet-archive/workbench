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
package org.dwfa.ace.task.search;

public class SearchReplaceDescription {

    private String origDesc;
    private String finalDesc;
    private String origDescHtml;
    private String finalDescHtml;
    private String descType;

    public SearchReplaceDescription(String origDesc, String finalDesc, String origDescHtml, String finalDescHtml,
            String descType) {
        super();
        this.origDesc = origDesc;
        this.finalDesc = finalDesc;
        this.origDescHtml = origDescHtml;
        this.finalDescHtml = finalDescHtml;
        this.descType = descType;
    }

    public String getOrigDesc() {
        return origDesc;
    }

    public void setOrigDesc(String origDesc) {
        this.origDesc = origDesc;
    }

    public String getFinalDesc() {
        return finalDesc;
    }

    public void setFinalDesc(String finalDesc) {
        this.finalDesc = finalDesc;
    }

    public String getOrigDescHtml() {
        return origDescHtml;
    }

    public void setOrigDescHtml(String origDescHtml) {
        this.origDescHtml = origDescHtml;
    }

    public String getFinalDescHtml() {
        return finalDescHtml;
    }

    public void setFinalDescHtml(String finalDescHtml) {
        this.finalDescHtml = finalDescHtml;
    }

    public String getDescType() {
        return descType;
    }

    public void setDescType(String descType) {
        this.descType = descType;
    }
}
