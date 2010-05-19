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
package org.ihtsdo.mojo.maven;

public class ExtractAndProcessSpec {

    public static class SubstutionSpec {
        private String propertyName = null;

        private String patternStr = null;

        private String replacementStr = null;

        private NULL_ACTION nullAction = NULL_ACTION.MAKE_UUID;

        public SubstutionSpec() {
            super();
        }

        public NULL_ACTION getNullAction() {
            return nullAction;
        }

        public String getNullActionStr() {
            return nullAction.toString();
        }

        public void setNullAction(NULL_ACTION nullAction) {
            this.nullAction = nullAction;
        }

        public void setNullActionStr(String nullActionStr) {
            this.nullAction = NULL_ACTION.valueOf(nullActionStr);
        }

        public String getPatternStr() {
            return patternStr;
        }

        public void setPatternStr(String patternStr) {
            this.patternStr = patternStr;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getReplacementStr() {
            return replacementStr;
        }

        public void setReplacementStr(String replacementStr) {
            this.replacementStr = replacementStr;
        }

        public String toString() {
            return "propertyName: " + propertyName + " patternStr: " + patternStr + " replacementStr: "
                + replacementStr + " nullAction: " + nullAction;
        }
    }

    public enum NULL_ACTION {
        PROMPT, MAKE_UUID, REPLACE_LITERAL, EMPTY_STRING
    };

    private String filePatternStr;

    private String destDir;

    private SubstutionSpec[] substitutions = new SubstutionSpec[0];

    private boolean retainDirStructure = false;

    private boolean executable = false;

    private boolean saveBeanForQueue = false;

    public ExtractAndProcessSpec() {
        super();
    }

    public String getFilePatternStr() {
        return filePatternStr;
    }

    public void setFilePatternStr(String filePatternStr) {
        this.filePatternStr = filePatternStr;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("filePatternStr: ");
        buff.append(filePatternStr);

        buff.append(" patternStr: ");
        buff.append(" substutions: ");
        for (SubstutionSpec s : substitutions) {
            buff.append(s + " ");
        }
        return buff.toString();
    }

    public String getDestDir() {
        return destDir;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public SubstutionSpec[] getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(SubstutionSpec[] substitutions) {
        this.substitutions = substitutions;
    }

    public boolean getRetainDirStructure() {
        return retainDirStructure;
    }

    public void setRetainDirStructure(boolean retainDirStructure) {
        this.retainDirStructure = retainDirStructure;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public boolean getSaveBeanForQueue() {
        return saveBeanForQueue;
    }

    public void setSaveBeanForQueue(boolean saveBeanForQueue) {
        this.saveBeanForQueue = saveBeanForQueue;
    }

}
