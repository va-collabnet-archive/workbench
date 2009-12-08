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
package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.file.IterableFileReader;

/**
 * Extends the abstract mojo to ensure a plug-able file handler is provided to
 * the concrete implementation.
 * Any implementations can make use of the extensions created by the defined
 * file handler.
 * 
 */
public abstract class ImportFromFile extends AbstractMojo {

    /**
     * Defines the FileHandler implementation to be used.
     * 
     * <p>
     * Example configuration:
     * 
     * <pre>
     * &lt;fileHandler implementation="org.dwfa.mojo.refset.ExportedRefsetHandler"&gt;
     *    ...
     * &lt;/fileHandler&gt;
     * 
     * @parameter
     * @required
     */
    public IterableFileReader<I_ThinExtByRefVersioned> fileHandler;

}
