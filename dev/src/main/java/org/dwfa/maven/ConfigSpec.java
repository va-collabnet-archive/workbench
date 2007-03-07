/*
 * Created on Jan 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.maven;

public class ConfigSpec {

    public ConfigSpec() {
        super();
        // TODO Auto-generated constructor stub
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
