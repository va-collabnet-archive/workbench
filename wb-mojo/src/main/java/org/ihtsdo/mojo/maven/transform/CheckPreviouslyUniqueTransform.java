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
package org.ihtsdo.mojo.maven.transform;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;

import org.ihtsdo.mojo.maven.Transform;

public class CheckPreviouslyUniqueTransform extends AbstractTransform {

    static Set<String> dups;
    boolean duplicatesFound = false;

    @SuppressWarnings("unchecked")
    @Override
    public void setupImpl(Transform transformer) throws IOException, ClassNotFoundException {
        if (dups == null) {
            if (CheckUniqueTransform.dupFile.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CheckUniqueTransform.dupFile));
                dups = (Set<String>) ois.readObject();
                ois.close();
                transformer.getLog().info(" Existing dups set. Size: " + dups.size());
            } else {
                dups = new HashSet<String>();
                transformer.getLog().info(" Creating new dups set. ");
            }
        } else {
            transformer.getLog().info(" Dup set exists with size: " + dups.size());
        }
    }

    public String transform(String input) throws Exception {
        if (dups.contains(input)) {
            return setLastTransform("Duplicate: " + input);
        } else {
            return setLastTransform(input);
        }
    }

    @Override
    public void cleanup(Transform transformer) throws Exception {
        super.cleanup(transformer);
        if (duplicatesFound) {
            transformer.getLog().info(
                this.getName() + " FOUND DUPLICATES. *** Please view the output file for details.");
        } else {
            transformer.getLog().info(this.getName() + " found no duplicates.");
        }
    }

}
