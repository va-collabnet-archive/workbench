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

/**
 * The Class EccsDependency represents a changeset dependency for particular
 * data in the database. Allows a dependency to be created and can check if the
 * dependency is satisfied.
 */
public class EccsDependency extends DbDependency {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the
     * <code>EccsDependency</code> object to the specified output stream.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    /**
     * Reads the
     * <code>EccsDependency</code> object from the specified input stream.
     *
     * @param in the input stream
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
     * Instantiates a new eccs dependency based on the given
     * <code>name</code> and
     * <code>sizeInBytes</code> of the changeset.
     *
     * @param name a string representing the name of the changeset
     * @param sizeInBytes a string representing the size, in bytes, of the changeset
     */
    public EccsDependency(String name, String sizeInBytes) {
        super(name, sizeInBytes);
    }

    /**
     * Gets the name of the changeset associated with this eccs dependency.
     *
     * @return the name of the changeset
     */
    public String getName() {
        return getKey();
    }

    /**
     * Gets a string representing the size in bytes of the change set associated with this
     * eccs dependency.
     *
     * @return the size in bytes
     */
    public String getSizeInBytes() {
        return getValue();
    }

    /**
     *
     * @param value the value to test
     * @return <code>true</code>, if the dependency is satisfied
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
