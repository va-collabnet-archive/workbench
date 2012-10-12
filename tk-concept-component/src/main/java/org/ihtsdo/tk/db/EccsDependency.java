/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// TODO: Auto-generated Javadoc
/**
 * The Class EccsDependency.
 */
public class EccsDependency extends DbDependency {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        	//
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    
	/**
	 * Instantiates a new eccs dependency.
	 *
	 * @param name the name
	 * @param sizeInBytes the size in bytes
	 */
	public EccsDependency(String name, String sizeInBytes) {
		super(name, sizeInBytes);
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return getKey();
	}

	/**
	 * Gets the size in bytes.
	 *
	 * @return the size in bytes
	 */
	public String getSizeInBytes() {
		return getValue();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.db.DbDependency#satisfactoryValue(java.lang.String)
	 */
	@Override
	public boolean satisfactoryValue(String value) {
		if (value == null) {
			return false;
		}
		Long dependencySize = Long.valueOf(getSizeInBytes());
		Long comparisonSize = Long.valueOf(value);
		return comparisonSize >= dependencySize;
	}

}
