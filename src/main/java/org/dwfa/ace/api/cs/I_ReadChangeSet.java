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
package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Provides an interface to read change sets. Provides methods to allow reading
 * of a collection
 * of change sets to be imported in a serialized sequence according to their
 * commit time.
 * 
 * @author kec
 * 
 */
public interface I_ReadChangeSet extends Serializable {

    /**
     * 
     * @return the time in ms of the next commit contined within this change
     *         set.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public long nextCommitTime() throws IOException, ClassNotFoundException;

    /**
     * Read this change set until the specified commit time.
     * 
     * @param time the commit time to read until.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readUntil(long time) throws IOException, ClassNotFoundException;

    /**
     * Read this file until the end.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void read() throws IOException, ClassNotFoundException;

    /**
     * 
     * @param changeSetFile the change set file to validate and read.
     */
    public void setChangeSetFile(File changeSetFile);

    /**
     * 
     * @param changeSetFile the change set file to validate and read.
     */
    public File getChangeSetFile();

    /**
     * A counter to be incremented each time a change set component is imported.
     * 
     * @param counter
     */
    public void setCounter(I_Count counter);

    /**
     * 
     * @return the list of falidators associated with this change set.
     */
    public List<I_ValidateChangeSetChanges> getValidators();

    public int availableBytes() throws FileNotFoundException, IOException, ClassNotFoundException;

}
