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
package org.dwfa.ace.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Reads match review item from URL/File and attaches the components to the
 * business process
 * 
 * @author Eric Mays (EKM)
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class CreateMatchReviewAssignments extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String inputFileNamePropName = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();

    private String bpFileNamePropName = "A: BP_FILE";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFileNamePropName);
        out.writeObject(bpFileNamePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            inputFileNamePropName = (String) in.readObject();
            bpFileNamePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    /*
     * Reads two properties: <br> inputFileNamePropName - the directory
     * containing the match review assignments <br> bpFileNamePropName - the
     * file containing the match review business process <br> The resulting
     * business processes are written into profiles/aao_inbox <br> (non-Javadoc)
     * 
     * @seeorg.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.
     * I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            System.out.println("F:" + new File("./").getAbsolutePath());
            // Write to this directory
            String out_dir = "../../src/main/profiles/aao_inbox/";
            // Get the business process
            String bpFileName = (String) process.getProperty(bpFileNamePropName);
            // Get the directory containing the match review items
            String inputFileName = (String) process.getProperty(inputFileNamePropName);
            File f = new File(inputFileName);
            int i = 0;
            // Process match review item in the directory
            for (File ff : new File(f.getParent()).listFiles()) {
                // Just in case there's some other stuff lingering
                if (!ff.toString().endsWith(".txt"))
                    continue;
                // if (++i == 5)
                // break;
                System.out.println("FF: " + ff);
                // Read the business process
                File bp_file = new File(bpFileName);
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(bp_file)));
                BusinessProcess bp = (BusinessProcess) ois.readObject();
                ois.close();
                // Read the match review item
                MatchReviewItem mri = new MatchReviewItem();
                mri.createFromFile(ff.getPath());
                // Attach the input term
                bp.writeAttachment(MatchReviewItem.AttachmentKeys.TERM.getAttachmentKey(), mri.getTerm());
                // Attach the matches
                bp.writeAttachment(MatchReviewItem.AttachmentKeys.UUID_LIST_LIST.getAttachmentKey(),
                    mri.getUuidListList());
                // Attach the HTML for the message
                bp.writeAttachment(MatchReviewItem.AttachmentKeys.HTML_DETAIL.getAttachmentKey(), mri.getHtml());
                // Attach the HTML for the signpost
                bp.writeAttachment(MatchReviewItem.AttachmentKeys.HTML_DETAIL.toString(), mri.getHtml());
                // Create a file name and subject for the business process so
                // that the sort order preserves the order in the original file
                String bp_name = ff.getName().replace(".txt", "");
                bp.setName("Review Match");
                String bp_id = bp_name.replace("tm", "");
                while (bp_id.length() < 4) {
                    bp_id = "0" + bp_id;
                }
                bp.setSubject(bp_id + ": " + mri.getTerm());
                // Write the business process
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(out_dir + bp_name + ".bp"));
                oos.writeObject(bp);
                oos.close();
            }

            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getInputFileNamePropName() {
        return inputFileNamePropName;
    }

    public void setInputFileNamePropName(String fileName) {
        this.inputFileNamePropName = fileName;
    }

    public String getBpFileNamePropName() {
        return bpFileNamePropName;
    }

    public void setBpFileNamePropName(String fileName) {
        this.bpFileNamePropName = fileName;
    }

}
