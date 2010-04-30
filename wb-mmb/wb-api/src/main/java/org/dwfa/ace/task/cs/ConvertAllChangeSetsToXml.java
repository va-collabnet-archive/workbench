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
package org.dwfa.ace.task.cs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.dwfa.ace.task.cs.transform.ChangeSetTransformFactory;
import org.dwfa.ace.task.cs.transform.ChangeSetTransformer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * @author Dion McMurtrie
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/change sets", type = BeanType.TASK_BEAN) })
public class ConvertAllChangeSetsToXml extends AbstractTask {

    private String outputSuffix = ".xml";

    private String inputSuffix = ".jcs";

    private String rootDirStr = "profiles/";

    private boolean recurseSubdirectories = true;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 0;

    private Logger logger;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(rootDirStr);
        out.writeBoolean(recurseSubdirectories);
        out.writeObject(outputSuffix);
        out.writeObject(inputSuffix);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            rootDirStr = (String) in.readObject();
            recurseSubdirectories = in.readBoolean();
            outputSuffix = (String) in.readObject();
            inputSuffix = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void addAllChangeSetFiles(File rootFile, List<File> changeSetFiles, final String suffix) {
        File[] children = rootFile.listFiles(new FileFilter() {

            public boolean accept(File child) {
                if (child.isHidden() || child.getName().startsWith(".")) {
                    return false;
                }
                if (child.isDirectory()) {
                    return recurseSubdirectories;
                }
                return child.getName().endsWith(suffix);
            }
        });
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    logger.info("recursing directory " + child);
                    addAllChangeSetFiles(child, changeSetFiles, suffix);
                } else {
                    logger.info("adding file " + child);
                    changeSetFiles.add(child);
                }
            }
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        logger = worker.getLogger();
        File rootDir = new File(rootDirStr);

        if (!rootDir.exists() || !rootDir.canRead()) {
            throw new TaskFailedException("Specified root directory '" + rootDirStr
                + "' either does not exist or cannot be read");
        }

        List<File> changeSetFiles = new ArrayList<File>();
        addAllChangeSetFiles(rootDir, changeSetFiles, inputSuffix);

        for (File file : changeSetFiles) {
            try {
                logger.info("Processing change set " + file.getParent() + File.separator + file.getName());

                ChangeSetTransformer encoder = ChangeSetTransformFactory.getTransformForFile(file);
                encoder.setOutputSuffix(outputSuffix);
                encoder.transform(logger, file);

            } catch (Exception e) {
                throw new TaskFailedException("Failed processing file " + file, e);
            }
        }

        return Condition.CONTINUE;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do.

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public void setInputSuffix(String inputSuffix) {
        this.inputSuffix = inputSuffix;
    }

    public String getRootDirStr() {
        return rootDirStr;
    }

    public void setRootDirStr(String rootDirStr) {
        this.rootDirStr = rootDirStr;
    }

    public boolean isRecurseSubdirectories() {
        return recurseSubdirectories;
    }

    public void setRecurseSubdirectories(boolean recurseSubdirectories) {
        this.recurseSubdirectories = recurseSubdirectories;
    }

    public String getOutputSuffix() {
        return outputSuffix;
    }

    public void setOutputSuffix(String outputSuffix) {
        this.outputSuffix = outputSuffix;
    }

    public String getInputSuffix() {
        return inputSuffix;
    }

}
