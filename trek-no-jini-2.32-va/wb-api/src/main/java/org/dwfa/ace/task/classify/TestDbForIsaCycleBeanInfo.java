/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.task.classify;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 *
 * @author marc
 */
public class TestDbForIsaCycleBeanInfo extends SimpleBeanInfo {
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        String s = "<html><font color='#FF8000'>";
        s = s.concat("<center>Run Cycle Check");
        BeanDescriptor bd = new BeanDescriptor(TestDbForIsaCycle.class);
        bd.setDisplayName(s);
        return bd;
    }

}
