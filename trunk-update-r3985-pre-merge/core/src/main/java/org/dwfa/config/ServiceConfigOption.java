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
package org.dwfa.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServiceConfigOption {
    private String name;

    private String policy;

    private String securePolicy;

    private String propName;

    private String desc;

    private boolean enabled;

    private String codebase;

    private String jarDir;

    private String classPath;

    private String mainClass;

    private String[] args;

    private String[] secureArgs;

    private boolean optional;

    private boolean alertIfSelected;

    private boolean alertIfDeselected;

    private String alertString;

    private transient String prefix;

    /**
     * @param name
     * @param desc
     * @param enabled
     * @param codebase
     * @param classPath
     * @param args
     */
    public ServiceConfigOption(String name, String policy, String securePolicy, String propName, String desc,
            boolean enabledByDefault, String codebase, String jarDir, String classPath, String mainClass,
            String[] args, String[] secureArgs, boolean optional, boolean alertIfSelected, boolean alertIfDeselected,
            String alertString) {
        super();
        this.name = name;
        this.policy = policy;
        this.securePolicy = securePolicy;
        this.propName = propName;
        this.desc = desc;
        this.enabled = enabledByDefault;
        this.codebase = codebase;
        this.jarDir = jarDir;
        this.mainClass = mainClass;
        this.classPath = classPath;
        this.args = args;
        this.secureArgs = secureArgs;
        this.optional = optional;
        this.alertIfSelected = alertIfSelected;
        this.alertIfDeselected = alertIfDeselected;
        this.alertString = alertString;
        Properties sysProps = System.getProperties();
        sysProps.setProperty(this.propName, new Boolean(enabledByDefault).toString());

    }

    public String getPrefix() {
        if (this.prefix == null) {
            this.prefix = this.propName.split("[.]")[2];
        }
        return this.prefix;
    }

    public String[] getSetupStrings(boolean secure) {
        List<String> stringList = new ArrayList<String>();
        String localPolicy;
        String[] localArgs;
        if (secure) {
            localPolicy = this.securePolicy;
            localArgs = this.secureArgs;
        } else {
            localPolicy = this.policy;
            localArgs = this.args;
        }
        if (this.codebase != null && this.codebase.length() > 4) {
            if (secure) {
                StringBuffer urlBuff = new StringBuffer();
                urlBuff.append("private static " + this.getPrefix() + "_URL = ");
                urlBuff.append("ConfigUtil.concat(new String[] {\"httpmd://\", host, ");
                urlBuff.append(this.codebase);
                urlBuff.append(";sha=0\"});");
                stringList.add(urlBuff.toString());
                StringBuffer mdBuff = new StringBuffer();
                mdBuff.append("private static " + this.getPrefix() + "_MD = ");
                mdBuff.append("HttpmdUtil.computeDigestCodebase(\"");
                mdBuff.append(this.jarDir);
                mdBuff.append("\", " + this.getPrefix() + "_URL);");
                stringList.add(mdBuff.toString());
                StringBuffer codebaseBuff = new StringBuffer();
                codebaseBuff.append("private static " + this.getPrefix() + "_Codebase = ");
                codebaseBuff.append("ConfigUtil.concat(new String[] {");
                codebaseBuff.append(this.getPrefix() + "_MD , \" \", jskCodebaseMd});");
                stringList.add(codebaseBuff.toString());

            } else {
                StringBuffer urlBuff = new StringBuffer();
                urlBuff.append("private static " + this.getPrefix() + "_URL = ");
                urlBuff.append("ConfigUtil.concat(new String[] {\"http://\", host, ");
                urlBuff.append(this.codebase);
                urlBuff.append("\"});");
                stringList.add(urlBuff.toString());
                StringBuffer codebaseBuff = new StringBuffer();
                codebaseBuff.append("private static " + this.getPrefix() + "_Codebase = ");
                codebaseBuff.append("ConfigUtil.concat(new String[] {");
                codebaseBuff.append(this.getPrefix() + "_URL , \" \", jskCodebase});");
                stringList.add(codebaseBuff.toString());
            }
        } else {
            if (secure) {
                stringList.add("private static " + this.getPrefix() + "_Codebase = \"\";");
            } else {
                stringList.add("private static " + this.getPrefix() + "_Codebase = \"\";");
            }
        }
        stringList.add("private static " + this.getPrefix() + "_Policy = \"" + localPolicy + "\";");
        stringList.add("private static " + this.getPrefix() + "_Classpath = " + this.classPath + ";");
        stringList.add("private static " + this.getPrefix() + "_Class = \"" + this.mainClass + "\";");
        //
        StringBuffer argBuff = new StringBuffer();
        argBuff.append("new String[] { ");
        for (int i = 0; i < localArgs.length; i++) {
            if (localArgs[i].equals("jiniPort")) {
                argBuff.append(localArgs[i]);
                if (i == localArgs.length - 1) {
                    argBuff.append(" ");
                } else {
                    argBuff.append(", ");
                }
            } else {
                argBuff.append("\"" + localArgs[i]);
                if (i == localArgs.length - 1) {
                    argBuff.append("\" ");
                } else {
                    argBuff.append("\", ");
                }
            }
        }
        argBuff.append("}");
        stringList.add("private static " + this.getPrefix() + "_Args = " + argBuff.toString() + ";");
        //
        String[] setupStrings = new String[stringList.size()];
        return (String[]) stringList.toArray(setupStrings);

    }

    public String getNonActivatableServiceDescriptor() {
        StringBuffer buff = new StringBuffer();
        buff.append("new NonActivatableServiceDescriptor(");
        buff.append(this.getPrefix() + "_Codebase,");
        buff.append(this.getPrefix() + "_Policy,");
        buff.append(this.getPrefix() + "_Classpath,");
        buff.append(this.getPrefix() + "_Class,");
        buff.append(this.getPrefix() + "_Args)");
        return buff.toString();
    }

    /**
     * @return Returns the alertIfDeselected.
     */
    public boolean isAlertIfDeselected() {
        return alertIfDeselected;
    }

    /**
     * @param alertIfDeselected The alertIfDeselected to set.
     */
    public void setAlertIfDeselected(boolean alertIfDeselected) {
        this.alertIfDeselected = alertIfDeselected;
    }

    /**
     * @return Returns the alertIfSelected.
     */
    public boolean isAlertIfSelected() {
        return alertIfSelected;
    }

    /**
     * @param alertIfSelected The alertIfSelected to set.
     */
    public void setAlertIfSelected(boolean alertIfSelected) {
        this.alertIfSelected = alertIfSelected;
    }

    /**
     * @return Returns the alertString.
     */
    public String getAlertString() {
        return alertString;
    }

    /**
     * @param alertString The alertString to set.
     */
    public void setAlertString(String alertString) {
        this.alertString = alertString;
    }

    /**
     * @return Returns the args.
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * @return Returns the classPath.
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * @param classPath The classPath to set.
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    /**
     * @return Returns the codebase.
     */
    public String getCodebase() {
        return codebase;
    }

    /**
     * @param codebase The codebase to set.
     */
    public void setCodebase(String codebase) {
        this.codebase = codebase;
    }

    /**
     * @return Returns the desc.
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc The desc to set.
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return Returns the jarDir.
     */
    public String getJarDir() {
        return jarDir;
    }

    /**
     * @param jarDir The jarDir to set.
     */
    public void setJarDir(String jarDir) {
        this.jarDir = jarDir;
    }

    /**
     * @return Returns the mainClass.
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * @param mainClass The mainClass to set.
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the optional.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional The optional to set.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * @return Returns the policy.
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * @param policy The policy to set.
     */
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * @return Returns the propName.
     */
    public String getPropName() {
        return propName;
    }

    /**
     * @param propName The propName to set.
     */
    public void setPropName(String propName) {
        this.propName = propName;
    }

    /**
     * @return Returns the secureArgs.
     */
    public String[] getSecureArgs() {
        return secureArgs;
    }

    /**
     * @param secureArgs The secureArgs to set.
     */
    public void setSecureArgs(String[] secureArgs) {
        this.secureArgs = secureArgs;
    }

    /**
     * @return Returns the securePolicy.
     */
    public String getSecurePolicy() {
        return securePolicy;
    }

    /**
     * @param securePolicy The securePolicy to set.
     */
    public void setSecurePolicy(String securePolicy) {
        this.securePolicy = securePolicy;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
