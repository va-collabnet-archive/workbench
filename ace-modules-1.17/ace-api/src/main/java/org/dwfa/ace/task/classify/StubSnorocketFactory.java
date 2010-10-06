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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.InputStream;

/* !!! OBSOLETE */
public class StubSnorocketFactory implements I_SnorocketFactory {

    public void classify() {
        // TODO Auto-generated method stub

    }

    public void addConcept(int conceptId, boolean fullyDefined) {
        // TODO Auto-generated method stub

    }

    public void addRelationship(int c1, int rel, int c2, int group) {
        // TODO Auto-generated method stub

    }

    public void setIsa(int id) {
        // TODO Auto-generated method stub

    }

    public void getResults(I_Callback callback) {
        // TODO Auto-generated method stub

    }

    public I_SnorocketFactory createExtension() {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream getStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void getEquivConcepts(I_EquivalentCallback callback) {
        // TODO Auto-generated method stub

    }

    public void addRoleComposition(int[] lhsIds, int rhsId) {
        // TODO Auto-generated method stub

    }

    public void addRoleRoot(int id, boolean inclusive) {
        // TODO Auto-generated method stub

    }

    public void addRoleNeverGrouped(int id) {
        // TODO Auto-generated method stub

    }

}
