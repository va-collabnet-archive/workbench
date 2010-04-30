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
 * Created on Dec 11, 2005
 */
package org.ihtsdo.mojo.maven;

public class BeanSpec {
    private String sourceName;
    private String dirName;
    private String type;
    private String format = "standard";
    private String constructArg = null;
    private String beanName = null;

    public BeanSpec() {
        super();
    }

    /**
     * @param name
     * @param name2
     */
    public BeanSpec(String sourceName, String dirName, String type, String format, String constructArg, String beanName) {
        super();
        this.sourceName = sourceName;
        this.dirName = dirName;
        this.type = type;
        this.format = format;
        this.constructArg = constructArg;
        this.beanName = beanName;
    }

    /**
     * @return Returns the sourceName.
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * @param sourceName The sourceName to set.
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * @return Returns the dirName.
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * @param dirName The dirName to set.
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getType() + " " + this.sourceName + " write to: " + this.dirName;
    }

    public String getType() {
        if (type == null) {
            return "task";
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the queueFormat.
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param queueFormat The queueFormat to set.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return Returns the constructArg.
     */
    public String getConstructArg() {
        return constructArg;
    }

    /**
     * @param constructArg The constructArg to set.
     */
    public void setConstructArg(String constructArg) {
        this.constructArg = constructArg;
    }

    /**
     * @return Returns the beanName.
     */
    public String getBeanName() {
        return beanName;
    }

    /**
     * @param beanName The beanName to set.
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

}
