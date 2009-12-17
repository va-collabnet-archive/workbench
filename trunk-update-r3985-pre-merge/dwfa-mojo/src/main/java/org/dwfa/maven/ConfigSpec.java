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
/*
 * Created on Jan 7, 2006
 */
package org.dwfa.maven;

public class ConfigSpec {

    public ConfigSpec() {
        super();
    }

    private String className;
    private String configFileName;
    private String methodName;

    /**
     * @param name
     * @param name2
     */
    public ConfigSpec(String className, String dirName, String methodName) {
        super();
        this.className = className;
        this.configFileName = dirName;
        this.methodName = methodName;
    }

    /**
     * @return Returns the className.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return Returns the configFileName.
     */
    public String getConfigFileName() {
        return configFileName;
    }

    /**
     * @param configFileName The configFileName to set.
     */
    public void setConfigFileName(String dirName) {
        this.configFileName = dirName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.className + " write to: " + this.configFileName;
    }

    /**
     * @return Returns the methodName.
     */
    public String getMethodName() {
        return methodName;
    }

}
