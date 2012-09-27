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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Reads match review item from attachment
 * 
 * @author Eric Mays (EKM)
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/assignments", type = BeanType.TASK_BEAN) })
public class ReadMatchReviewItemFromAttachment extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String uuidListListPropName = MatchReviewItem.AttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
    public static final String KEY_MATCH_REVIEW_CONTENTS = "MATCH_REVIEW_CONTENTS";
    private String inputFileNamePropName = "A: " + KEY_MATCH_REVIEW_CONTENTS;
    private String htmlPropName = MatchReviewItem.AttachmentKeys.HTML_DETAIL.getAttachmentKey();
    private String termPropName = MatchReviewItem.AttachmentKeys.TERM.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uuidListListPropName);
        out.writeObject(inputFileNamePropName);
        out.writeObject(htmlPropName);
        out.writeObject(termPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion >= 1) {
            uuidListListPropName = (String) in.readObject();
            inputFileNamePropName = (String) in.readObject();
            htmlPropName = (String) in.readObject();
            termPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {

            // String inputFileName = (String) process
            // .getProperty(inputFileNamePropName);
            List<List<UUID>> uuidListOfLists = (List<List<UUID>>) process.readAttachement(MatchReviewItem.AttachmentKeys.UUID_LIST_LIST.getAttachmentKey());
            System.out.println("U:" + uuidListOfLists.size());
            String term = (String) process.readAttachement(MatchReviewItem.AttachmentKeys.TERM.getAttachmentKey());
            System.out.println("T: " + term);
            String html = (String) process.readAttachement(MatchReviewItem.AttachmentKeys.HTML_DETAIL.getAttachmentKey());
            System.out.println("H: " + html);

            // worker.getLogger().info("file is: " + uuidFileName);

            process.setProperty(this.uuidListListPropName, uuidListOfLists);
            process.setProperty(this.htmlPropName, html);
            process.setProperty(this.termPropName, term);

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

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String potDupUuidList) {
        this.uuidListListPropName = potDupUuidList;
    }

    public String getInputFileNamePropName() {
        return inputFileNamePropName;
    }

    public void setInputFileNamePropName(String fileName) {
        this.inputFileNamePropName = fileName;
    }

    public String getHtmlPropName() {
        return htmlPropName;
    }

    public void setHtmlPropName(String htmlPropName) {
        this.htmlPropName = htmlPropName;
    }

    public String getTermPropName() {
        return termPropName;
    }

    public void setTermPropName(String termPropName) {
        this.termPropName = termPropName;
    }

}
