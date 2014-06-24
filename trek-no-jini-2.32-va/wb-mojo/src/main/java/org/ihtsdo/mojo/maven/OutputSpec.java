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
package org.ihtsdo.mojo.maven;

public class OutputSpec {
    private InputFileSpec[] inputSpecs;
    private I_TransformAndWrite[] writers;
    private I_ReadAndTransform[] constantSpecs;

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("\nInputFileSpec: \n");
        for (InputFileSpec is : inputSpecs) {
            b.append(is);
        }
        b.append("\nWriters: \n");
        for (I_TransformAndWrite w : writers) {
            b.append(w);
        }
        return b.toString();
    }

    public InputFileSpec[] getInputSpecs() {
        return inputSpecs;
    }

    public void setInputSpecs(InputFileSpec[] inputSpecs) {
        this.inputSpecs = inputSpecs;
    }

    public I_TransformAndWrite[] getWriters() {
        return writers;
    }

    public void setWriters(I_TransformAndWrite[] writers) {
        this.writers = writers;
    }

    public I_ReadAndTransform[] getConstantSpecs() {
        return constantSpecs;
    }

    public void setConstantSpecs(I_ReadAndTransform[] constantSpecs) {
        this.constantSpecs = constantSpecs;
    }

}
