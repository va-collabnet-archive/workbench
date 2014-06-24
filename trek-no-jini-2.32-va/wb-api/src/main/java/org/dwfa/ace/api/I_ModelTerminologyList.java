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
package org.dwfa.ace.api;

import javax.swing.ListModel;

public interface I_ModelTerminologyList extends ListModel {
    public I_GetConceptData getElementAt(int index);

    public int getSize();

    public boolean addElement(I_GetConceptData concept);

    public void addElement(int index, I_GetConceptData element);

    public I_GetConceptData removeElement(int index);

    public void clear();

}
