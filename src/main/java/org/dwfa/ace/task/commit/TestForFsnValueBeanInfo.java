/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.ace.task.commit;

import org.dwfa.ace.task.commit.*;
import java.beans.BeanDescriptor;

/**
 * The <code>TestForFsnValueBeanInfo</code> class represents a BeanInfo helper class for the
 * {@link TestForFsnValue} Task for use within the Process Builder in ACE.
 *
 * @author Matthew Edwards
 */
public class TestForFsnValueBeanInfo extends TestForUneditedDefaultsBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(TestForIsa.class);
        bd.setDisplayName("<html><font color='green'><center>Test For<br>Fully Specified Name value");
        return bd;
    }
}
