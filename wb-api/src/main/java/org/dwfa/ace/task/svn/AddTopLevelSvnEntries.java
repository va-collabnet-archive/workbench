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
package org.dwfa.ace.task.svn;

import java.io.*;
import java.util.Collection;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnWorkingCopyInfo;

/**
 *
 * @author kec
 */
@BeanList(specs = {@Spec(directory = "tasks/ide/svn", type = BeanType.TASK_BEAN)})
public class AddTopLevelSvnEntries extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;
    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(profilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 2) {
                profilePropName = (String) in.readObject();
            } else {
                profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    @Override
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    @Override
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) process.readAttachement(ProcessAttachmentKeys.WORKING_PROFILE.name());
            File dir = new File(System.getProperty("user.dir"));
            File[] childrenDirs = dir.listFiles();
            if (childrenDirs != null) {
                for (File svnDir : childrenDirs) {
                    File svnMetaDir = new File(svnDir, ".svn");
                    if (svnMetaDir.exists()) {
                        String workingCopy = FileIO.getRelativePath(svnDir);

                        SubversionData svd = config.getSubversionMap().get(workingCopy);
                        if (svd == null) {
                            svd = new SubversionData(null, workingCopy);
                            SvnInfo svnInfo = new SvnInfo();
                            SvnWorkingCopyInfo svnWcInfo = new SvnWorkingCopyInfo();
                            svnWcInfo.setWcRoot(svnDir.getParentFile());
                            svnInfo.setWcInfo(svnWcInfo);
                            AceLog.getAppLog().info("Found url " + svnInfo.getUrl().toString() + " for working copy: " + svd.getWorkingCopyStr());
                            svd.setRepositoryUrlStr(svnInfo.getUrl().toString());
                            config.getSubversionMap().put(workingCopy, svd);
                        }
                    }
                }
            }
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    @Override
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    @Override
    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }
}