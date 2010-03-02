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
package org.dwfa.ace.tree;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;

public interface I_GetConceptDataForTree extends I_GetConceptData {
    public boolean isParentOpened();

    public void setParentOpened(boolean opened);

    public int getParentDepth();

    public boolean isSecondaryParentNode();

    public I_GetConceptData getCoreBean();

    public List<DefaultMutableTreeNode> getExtraParentNodes();

    public I_DescriptionTuple getDescTuple(I_ConfigAceFrame aceConfig) throws IOException;

    public int getRelId();
}
