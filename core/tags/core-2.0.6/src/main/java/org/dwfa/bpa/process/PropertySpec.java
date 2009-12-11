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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author kec
 *
 */
public class PropertySpec implements Serializable {
    public static enum SourceType { TASK, DATA_CONTAINER, ATTACHMENT };
    
    private SourceType type;
    private int id;
    private String propertyName;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(PropertySpec.dataVersion);
        out.writeInt(this.id);
        out.writeObject(this.propertyName);
        out.writeObject(this.type);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            this.id = in.readInt();
            this.propertyName = (String) in.readObject();
            if (objDataVersion >= 2) {
                this.type = (SourceType) in.readObject();
            } else {
                this.type = SourceType.TASK; 
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

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
		this.propertyName = propertyName;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
        if (PropertySpec.class.isAssignableFrom(obj.getClass())) {
        	    PropertySpec another = (PropertySpec) obj;
            return ((this.id == another.id) && 
                (this.propertyName.equals(another.propertyName) &&
                        this.type == another.type));
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
     * @param propertyName The propertyName to set.
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @return Returns the taskId.
     */
    public int getId() {
        return id;
    }

    /**
     * @param taskId The taskId to set.
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


}
