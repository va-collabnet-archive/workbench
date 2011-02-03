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
package org.dwfa.ace.task.wfpanel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.lucene.SearchResult;

public abstract class AbstractSetUserAndPwdPNC extends PreviousNextOrCancel {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    protected transient JTextField fullName;
    protected transient JTextField user;
    protected transient JTextField pwd;
    protected transient JLabel instruction;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            // nothing to read...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        try {
            DoSwing swinger = new DoSwing(process);
            swinger.start();
            swinger.get();
            synchronized (this) {
                this.waitTillDone(worker.getLogger());
            }

            readInput(process);
            restore();

        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (ExecutionException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    @Override
    public String getInvalidInputMessage() {
        return "Username must be unique - inputted username is in use.";
    }

    @Override
    public boolean hasValidInput() {
        try {
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            I_IntSet actives = helper.getCurrentStatusIntSet();

            String userName = fullName.getText();

            String filteredUserName = userName;
            filteredUserName = filteredUserName.trim();

            // new removal using native lucene escaping
            filteredUserName = QueryParser.escape(filteredUserName);
            SearchResult result;

            result = Terms.get().doLuceneSearch(filteredUserName);

            for (int i = 0; i < result.topDocs.totalHits; i++) {
                Document doc = result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                int cnid = Integer.parseInt(doc.get("cnid"));
                int dnid = Integer.parseInt(doc.get("dnid"));

                I_DescriptionVersioned potential_fsn = Terms.get().getDescription(dnid, cnid);
                for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
                    if (actives.contains(part_search.getStatusNid())
                        && part_search.getTypeNid() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
                            .localize().getNid() && part_search.getText().equals(userName)) {
                        return false;
                    }
                }

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private class DoSwing extends SwingWorker<Boolean> {

        I_EncodeBusinessProcess process;

        public DoSwing(I_EncodeBusinessProcess process) {
            super();
            this.process = process;
        }

        @Override
        protected Boolean construct() throws Exception {
            setup(process);
            setupInput(process);
            return true;
        }

        @Override
        protected void finished() {
            Component[] components = workflowPanel.getComponents();
            for (int i = 0; i < components.length; i++) {
                workflowPanel.remove(components[i]);
            }
            workflowPanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 0;
            c.anchor = GridBagConstraints.EAST;
            workflowPanel.add(instruction, c);
            c.weightx = 0.0;
            c.gridx++;
            c.weightx = 1.0;
            if (showFullName()) {
                workflowPanel.add(fullName, c);
                c.gridx++;
            }
            workflowPanel.add(user, c);
            c.gridx++;
            workflowPanel.add(pwd, c);
            c.weightx = 0.0;
            setupPreviousNextOrCancelButtons(workflowPanel, c);
            workflowPanel.setVisible(true);
            finalSetup();
        }

    }

    protected abstract boolean showFullName();

    protected abstract void finalSetup();

    protected abstract void readInput(I_EncodeBusinessProcess process) throws IntrospectionException,
            IllegalAccessException, InvocationTargetException;

    protected abstract void setupInput(I_EncodeBusinessProcess process) throws IllegalArgumentException,
            IntrospectionException, IllegalAccessException, InvocationTargetException;

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do

    }
}
