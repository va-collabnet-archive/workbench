/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.db;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * The Class DbDependency represents a data dependency for particular data in
 * the database. Allows a dependency to be created and can check if the
 * dependency is satisfied.
 */
public abstract class DbDependency implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the
     * <code>DbDependency</code> object to the specified output stream.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(key);
        out.writeUTF(value);
    }

    /**
     * Reads the
     * <code>DbDependency</code> object from the specified input stream.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            key = in.readUTF();
            value = in.readUTF();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    private String key;
    private String value;

    /**
     * Instantiates a new db dependency based on the given
     * <code>key</code> and
     * <code>value</code>.
     *
     * @param key the key
     * @param value the value
     */
    public DbDependency(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the key associated with this db dependency.
     *
     * @return the string representation of the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value associated with this db dependency.
     *
     * @return the string representation of the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Tests if the given <code>value</code> satisfies this db dependency.
     *
     * @param value the value to test
     * @return <code>true</code>, if the dependency is satisfied
     */
    public abstract boolean satisfactoryValue(String value);
}
