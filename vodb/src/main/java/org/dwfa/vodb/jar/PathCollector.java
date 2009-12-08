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
package org.dwfa.vodb.jar;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Path;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.types.I_ProcessPathEntries;
import org.dwfa.vodb.types.Path;

import com.sleepycat.je.DatabaseEntry;

@Deprecated
public class PathCollector implements I_ProcessPathEntries {
    List<I_Path> paths = new ArrayList<I_Path>();
    PathBinder binder = new PathBinder();

    public PathCollector() {

    }

    public void processPath(DatabaseEntry key, DatabaseEntry value) throws Exception {
        Path p = (Path) binder.entryToObject(value);
        paths.add(p);
    }

    public List<I_Path> getPaths() {
        return paths;
    }

    public DatabaseEntry getDataEntry() {
        return new DatabaseEntry();
    }

    public DatabaseEntry getKeyEntry() {
        return new DatabaseEntry();
    }
}
