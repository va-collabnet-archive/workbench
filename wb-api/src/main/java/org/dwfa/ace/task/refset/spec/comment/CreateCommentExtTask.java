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
package org.dwfa.ace.task.refset.spec.comment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceConfiguration;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Creates a comment extension on the refset currently in the refset spec panel,
 * based on the string stored in the
 * comments prop name. The refset spec input parameter specifies the refset spec
 * to use (to derive the appropriate
 * comments refset).
 * 
 * @author Chrissy Hill
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class CreateCommentExtTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 2;
    private I_TermFactory termFactory;

    private String commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
    private String refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(commentsPropName);
        out.writeObject(refsetSpecUuidPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                commentsPropName = (String) in.readObject();
            } else {
                commentsPropName = ProcessAttachmentKeys.MESSAGE.getAttachmentKey();
            }
            if (objDataVersion >= 2) {
                refsetSpecUuidPropName = (String) in.readObject();
            } else {
                refsetSpecUuidPropName = ProcessAttachmentKeys.REFSET_SPEC_UUID.getAttachmentKey();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            termFactory = Terms.get();
            I_GetConceptData refsetSpecConcept = AceTaskUtil.getConceptFromProperty(process, refsetSpecUuidPropName);
            String comments = (String) process.getProperty(commentsPropName);
            if (comments != null && !comments.trim().equals("")) {
            	AceLog.getAppLog().info("Found comment to add: " + comments);
                UUID currentUuid = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next();
                if (refsetSpecConcept != null) {
                    //TODO use other than termFactory.getActiveAceFrameConfig();
                    RefsetSpec refsetSpec = new RefsetSpec(refsetSpecConcept, Terms.get().getActiveAceFrameConfig());
                    I_GetConceptData commentsRefset = refsetSpec.getCommentsRefsetConcept();
                    I_GetConceptData memberRefset = refsetSpec.getMemberRefsetConcept();
                    if (commentsRefset == null) {
                    	// Try again, and assume the wrong thing was passed in...
                    	Set<? extends I_GetConceptData> commentRefsetSet  = 
                    		Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig()).
                    			getCommentsRefsetForRefset(refsetSpecConcept, Terms.get().getActiveAceFrameConfig());
                    	if (commentRefsetSet != null && commentRefsetSet.size() > 0) {
                    		commentsRefset = commentRefsetSet.iterator().next();
                    		memberRefset = refsetSpecConcept;
                    	} else {
                        	AceLog.getAppLog().info("commentsRefset is null");
                    	}
                    }
                    if (commentsRefset != null && memberRefset != null) {
                        I_HelpSpecRefset specRefsetHelper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
                        for (I_Path path : termFactory.getActiveAceFrameConfig().getEditingPathSet()) {
                            boolean added = specRefsetHelper.newStringRefsetExtension(commentsRefset.getConceptId(),
                                memberRefset.getConceptId(), comments, UUID.randomUUID(), termFactory.getConcept(
                                    path.getConceptId()).getUids().iterator().next(), currentUuid, Integer.MAX_VALUE);
                            if (added) {
                            	AceLog.getAppLog().info("added comment: " + comments);
                            } else {
                            	AceLog.getAppLog().info("failed to add comment: " + comments);
                            }
                        }
                    }
                } else {
                	AceLog.getAppLog().info("refsetSpecConcept is null for: " + process.getProperty(refsetSpecUuidPropName));
                }
            } else {
            	AceLog.getAppLog().info("No comment to add: " + comments);
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

    public String getCommentsPropName() {
        return commentsPropName;
    }

    public void setCommentsPropName(String commentsPropName) {
        this.commentsPropName = commentsPropName;
    }

    public String getRefsetSpecUuidPropName() {
        return refsetSpecUuidPropName;
    }

    public void setRefsetSpecUuidPropName(String refsetSpecUuidPropName) {
        this.refsetSpecUuidPropName = refsetSpecUuidPropName;
    }
}
