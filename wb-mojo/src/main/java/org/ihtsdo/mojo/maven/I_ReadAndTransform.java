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

import java.io.IOException;

public interface I_ReadAndTransform {
    public void setup(Transform transformer) throws IOException, ClassNotFoundException;

    public void cleanup(Transform transformer) throws Exception;

    public String transform(String input) throws Exception;

    public I_ReadAndTransform getChainedTransform();

    public String getLastTransform();

    public String getName();

    public void setName(String name);

    public void setColumnId(int id);

    public int getColumnId();
}
