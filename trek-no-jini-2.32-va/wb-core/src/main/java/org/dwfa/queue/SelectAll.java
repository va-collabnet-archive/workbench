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
/*
 * Created on Apr 21, 2005
 */
package org.dwfa.queue;

import java.io.Serializable;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;

/**
 * @author kec
 * 
 */
public class SelectAll implements I_SelectProcesses, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
     */
    public boolean select(I_DescribeBusinessProcess process) {
        return true;
    }

    public boolean select(I_DescribeObject obj) {
        return true;
    }

}
