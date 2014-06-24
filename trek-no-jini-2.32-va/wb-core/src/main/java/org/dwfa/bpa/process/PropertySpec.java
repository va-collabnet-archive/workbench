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
 * Created on Jun 9, 2005
 */
package org.dwfa.bpa.process;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author kec
 * 
 */
public class PropertySpec implements Serializable {
    public static enum SourceType {
        TASK, ATTACHMENT
    };

    private SourceType type;
    private int id;
    private String propertyName;
    private String shortDescription;
    private String externalName;
    private String externalToolTip;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 4;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(PropertySpec.dataVersion);
        out.writeInt(this.id);
        out.writeObject(this.propertyName);
        out.writeObject(this.type);
        out.writeObject(this.externalName);
        out.writeObject(this.externalToolTip);
        out.writeObject(this.shortDescription);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            this.id = in.readInt();
            this.propertyName = (String) in.readObject();
            if (objDataVersion >= 2) {
                this.type = (SourceType) in.readObject();
            } else {
                this.type = SourceType.TASK;
            }
            if (objDataVersion >= 3) {
                this.externalName = (String) in.readObject();
                this.externalToolTip = (String) in.readObject();
            } else {
                this.externalName = null;
                this.externalToolTip = null;
            }
            if (objDataVersion >= 4) {
                this.shortDescription = (String) in.readObject();
            } else {
                this.shortDescription = null;
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public String getExternalToolTip() {
        return externalToolTip;
    }

    public void setExternalToolTip(String externalToolTip) {
        this.externalToolTip = externalToolTip;
    }

    public PropertySpec() {
        super();
    }

    /**
     * @param taskId
     * @param propertyName
     */
    public PropertySpec(SourceType type, int id, String propertyName) {
        super();
        this.type = type;
        this.id = id;
        this.propertyName = propertyName.replaceAll("\\<.*?>", "");
        ;
        this.externalName = "not set";
        this.externalToolTip = "Not set";
    }

    public String getKey() {
        if (getType() == SourceType.ATTACHMENT) {
            return "A: " + propertyName;
        }
        return "T" + id + ": " + propertyName;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (PropertySpec.class.isAssignableFrom(obj.getClass())) {
            PropertySpec another = (PropertySpec) obj;
            return ((this.id == another.id) && (this.propertyName.equals(another.propertyName) && this.type == another.type));
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.propertyName.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "PropertySpec taskId: " + this.id + " property name: " + this.propertyName;
    }

    /**
     * @return Returns the propertyName.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param propertyName
     *            The propertyName to set.
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName.replaceAll("\\<.*?>", "");
        ;
    }

    /**
     * @return Returns the taskId.
     */
    public int getId() {
        return id;
    }

    /**
     * @param taskId
     *            The taskId to set.
     */
    public void setId(int taskId) {
        this.id = taskId;
    }

    /**
     * @return Returns the type.
     */
    public SourceType getType() {
        return type;
    }

    public static PropertySpec make(PropertyDescriptor pd, Object target) {
        PropertySpec propSpec;
        if (I_DefineTask.class.isAssignableFrom(target.getClass())) {
            I_DefineTask t = (I_DefineTask) target;
            propSpec = new PropertySpec(PropertySpec.SourceType.TASK, t.getId(), pd.getName());

            propSpec.externalToolTip = pd.getShortDescription();
        } else {
            propSpec = new PropertySpec(PropertySpec.SourceType.ATTACHMENT, -1, pd.getName());
            propSpec.externalToolTip = "Attachment with key: " + pd.getName();
        }
        propSpec.externalName = pd.getDisplayName();
        return propSpec;
    }

}
