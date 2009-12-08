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
package org.dwfa.ace.task.svn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.BundleType;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.I_ConfigAceFrame.SPECIAL_SVN_ENTRIES;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN) })
public class CheckoutSvnEntry extends AbstractSvnEntryTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            // ;
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    protected void doSvnTask(I_ConfigAceFrame config, SubversionData svd, String svnEntryKey)
            throws TaskFailedException {
        try {
            SPECIAL_SVN_ENTRIES entry = SPECIAL_SVN_ENTRIES.valueOf(svnEntryKey);
            BundleType bundleType = config.getBundleType();
            switch (entry) {
            case PROFILE_CSU:
                switch (bundleType) {
                case STAND_ALONE:
                    // nothing to do...
                    break;

                case CHANGE_SET_UPDATE:
                    config.svnCheckout(svd);
                    break;
                default:
                    throw new TaskFailedException("Can't handle: " + bundleType);
                }

                break;
            default:
                throw new TaskFailedException("Don't know how to handle: " + entry);
            }
        } catch (IllegalArgumentException e) {
            config.svnCheckout(svd);
        }
    }

}
