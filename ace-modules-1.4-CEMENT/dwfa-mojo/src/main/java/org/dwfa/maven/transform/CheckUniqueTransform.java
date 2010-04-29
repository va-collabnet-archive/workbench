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
package org.dwfa.maven.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.maven.Transform;

public class CheckUniqueTransform extends AbstractTransform {

    static File dupFile = new File("target/dups.oos");
    Set<String> keys = new HashSet<String>();
    boolean duplicatesFound = false;
    static Set<String> dups;

    @SuppressWarnings("unchecked")
    @Override
    public void setupImpl(Transform transformer) throws IOException, ClassNotFoundException {
        if (dups == null) {
            if (dupFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dupFile));
                dups = (Set<String>) ois.readObject();
                ois.close();
            } else {
                dups = new HashSet<String>();
            }
        }
    }

    public String transform(String input) throws Exception {
        if (keys.contains(input) || dups.contains(input)) {
            duplicatesFound = true;
            dups.add(input);
            return setLastTransform("Duplicate: " + input);
        } else {
            keys.add(input);
            return setLastTransform(input);
        }
    }

    @Override
    public void cleanup(Transform transformer) throws Exception {
        super.cleanup(transformer);
        if (duplicatesFound) {
            transformer.getLog().info(
                this.getName() + " FOUND DUPLICATES. *** Please view the output file for details. " + dups.size());
            transformer.getLog().info(this.getName() + " Dups: " + dups);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dupFile));
            oos.writeObject(dups);
            oos.close();
        } else {
            transformer.getLog().info(this.getName() + " found no duplicates.");
        }
    }

}
