/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.task.datacheck;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 *
 * @author marc
 */
public class MultiEditorContradictionDetectorTaskBeanInfo extends SimpleBeanInfo {

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        String s = "<html><font color='#0087FF'>";
        s = s.concat("<center>Contradiction Detector");
        BeanDescriptor bd = new BeanDescriptor(MultiEditorContradictionDetectorTask.class);
        bd.setDisplayName(s);
        return bd;
    }
}
