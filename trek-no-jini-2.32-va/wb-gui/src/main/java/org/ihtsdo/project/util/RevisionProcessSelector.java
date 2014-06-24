/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.util;

import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_DescribeObject;
import org.dwfa.bpa.process.I_SelectProcesses;
import org.ihtsdo.project.view.TranslationHelperPanel;

/**
 * The Class RevisionProcessSelector.
 */
public class RevisionProcessSelector implements I_SelectProcesses {


    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_SelectProcesses#select(org.dwfa.bpa.process.I_DescribeBusinessProcess)
     */
    @Override
    public boolean select(I_DescribeBusinessProcess process) {
        String sub = process.getSubject();
        if (sub != null) {
            return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_SelectObjects#select(org.dwfa.bpa.process.I_DescribeObject)
     */
    @Override
    public boolean select(I_DescribeObject object) {
        I_DescribeBusinessProcess objectBP = (I_DescribeBusinessProcess) object;
        String sub = objectBP.getSubject();
        if (sub != null) {
            return sub.equals(TranslationHelperPanel.AUTO_PROCESS_WORKLIST_MEMBERS_REVIEW);
        }

        return false;
    }
}
