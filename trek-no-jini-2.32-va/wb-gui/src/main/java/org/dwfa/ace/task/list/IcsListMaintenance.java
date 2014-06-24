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
package org.dwfa.ace.task.list;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Updates initial capital status list based on text file.
 * 
 * @author akf
 * 
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN)})
public class IcsListMaintenance extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String icsListFileNameProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();;
    private ConceptChronicleBI caseSensitiveRefexColl;
    private TerminologyBuilderBI tc;
    private ViewCoordinate vc;
    private EditCoordinate ec;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(icsListFileNameProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            icsListFileNameProp = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            String fileName = (String) process.getProperty(icsListFileNameProp);
            File icsFile = new File(fileName);
            vc = Ts.get().getMetadataViewCoordinate();
            ec = new EditCoordinate(config.getDbConfig().getUserConcept().getNid(),
                    config.getEditCoordinate().getModuleNid(),
                    vc.getPositionSet().getViewPathNidSet());
            tc = Ts.get().getTerminologyBuilder(ec, vc);
            caseSensitiveRefexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.CASE_SENSITIVE_WORDS.getUids());

            InputStreamReader isr =
                    new InputStreamReader(new FileInputStream(icsFile),
                    "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            Collection<? extends RefexVersionBI<?>> currentRefsetMembers =
                    caseSensitiveRefexColl.getRefsetMembersActive(vc);
            ArrayList<RefexNidStringVersionBI> memberList = new ArrayList<RefexNidStringVersionBI>();
            for (RefexVersionBI rv : currentRefsetMembers) {
                RefexNidStringVersionBI member = (RefexNidStringVersionBI) rv;
                memberList.add(member);
            }
            try {
                String line = br.readLine();
                String[] parts = line.split(" ");
                int wordIndex = 0;
                int caseIndex = 1;

                line = br.readLine();
                while (line != null && line.length() > 1) {
                    parts = line.split(" ");
                    String word = parts[wordIndex];

                    int caseType = Integer.parseInt(parts[caseIndex]);
                    int icsTypeNid;
                    if (caseType == 1) {
                        icsTypeNid = CaseSensitive.IC_SIGNIFICANT.getLenient().getNid();
                    } else {
                        icsTypeNid = CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid();
                    }
                    boolean found = false;

                    for (RefexNidStringVersionBI member : memberList) {
                        if (member.getString1().equals(word)) {
                            found = true;
                            if(icsTypeNid != member.getNid1()){
                                updateMember(member, icsTypeNid);
                            }
                            memberList.remove(member);
                            break;
                        }
                    }
                    if(found == false){
                        addMember(word, icsTypeNid);
                    }
                    line = br.readLine();
                }
                for(RefexNidStringVersionBI member : memberList){
                    retireMember(member);
                }
            } catch (EOFException ex) {
                // nothing to do...
            } finally {
                br.close();
            }

            return Condition.CONTINUE;

        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (MalformedURLException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private void addMember(String word, int icsTypeNid) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB wordRefexSpec = new RefexCAB(TK_REFEX_TYPE.CID_STR,
                caseSensitiveRefexColl.getNid(), caseSensitiveRefexColl.getNid());
        wordRefexSpec.with(RefexProperty.STRING1, word);
        wordRefexSpec.with(RefexProperty.CNID1, icsTypeNid);
        wordRefexSpec.with(RefexProperty.STATUS_NID, SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        wordRefexSpec.setMemberContentUuid();

        RefexChronicleBI<?> wordRefex = tc.constructIfNotCurrent(wordRefexSpec);
        Ts.get().addUncommitted(caseSensitiveRefexColl);
    }

    private void retireMember(RefexNidStringVersionBI member) throws IOException {
        for (int pathNid : ec.getEditPaths()) {
            member.makeAnalog(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(),
                    Long.MAX_VALUE,
                    ec.getAuthorNid(),
                    ec.getModuleNid(),
                    pathNid);
        }
        Ts.get().addUncommitted(caseSensitiveRefexColl);
    }
    
    private void updateMember(RefexNidStringVersionBI member, int icsTypeNid) throws IOException, PropertyVetoException {
        for (int pathNid : ec.getEditPaths()) {
           RefexNidStringAnalogBI analog =  (RefexNidStringAnalogBI) member.makeAnalog(
                    SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(),
                    Long.MAX_VALUE,
                    ec.getAuthorNid(),
                    ec.getModuleNid(),
                    pathNid);
           analog.setNid1(icsTypeNid);
        }
        Ts.get().addUncommitted(caseSensitiveRefexColl);
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getIcsListFileNameProp() {
        return icsListFileNameProp;
    }

    public void setIcsListFileNameProp(String icsListFileNameProp) {
        this.icsListFileNameProp = icsListFileNameProp;
    }
}
