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
package org.ihtsdo.translation.tasks;

import java.beans.BeanDescriptor;

import org.dwfa.ace.task.commit.TestForUneditedDefaultsBeanInfo;

public class TestRejectRelsAndAttributesChangesBeanInfo extends TestForUneditedDefaultsBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TestRejectRelsAndAttributesChanges.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>Rels and Attrs changes");
        return bd;
    }

}
