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
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.String;
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
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Updates dialect variant file based on text file.
 * 
 * @author akf
 * 
 */
@BeanList(specs = {
    @Spec(directory = "tasks/ide", type = BeanType.TASK_BEAN)})
public class DialectListMaintenance extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private String dialectListFileNameProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    ;
    private ConceptChronicleBI dialectRefexColl;
    private ConceptChronicleBI refexColl;
    private TerminologyBuilderBI tc;
    private ViewCoordinate vc;
    private EditCoordinate ec;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(dialectListFileNameProp);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            dialectListFileNameProp = (String) in.readObject();
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
            vc = Ts.get().getMetadataViewCoordinate();
            ec = new EditCoordinate(config.getDbConfig().getUserConcept().getNid(),
                    config.getEditCoordinate().getModuleNid(),
                    vc.getPositionSet().getViewPathNidSet());
            String fileName = (String) process.getProperty(dialectListFileNameProp);
            File icsFile = new File(fileName);
            tc = Ts.get().getTerminologyBuilder(ec, vc);

            InputStreamReader isr =
                    new InputStreamReader(new FileInputStream(icsFile),
                    "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            refexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.EN_TEXT_WITH_DIALECT_VARIANTS.getUids());

            if (fileName.contains("AU")) {
                dialectRefexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.EN_AU_TEXT_VARIANTS.getUids());
            } else if (fileName.contains("CA")) {
                dialectRefexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.EN_CA_TEXT_VARIANTS.getUids());
            } else if (fileName.contains("UK")) {
                dialectRefexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.EN_UK_TEXT_VARIANTS.getUids());
            } else if (fileName.contains("US")) {
                dialectRefexColl = Ts.get().getConcept(RefsetAuxiliary.Concept.EN_US_TEXT_VARIANTS.getUids());
            } else {
                throw new UnsupportedDialectOrLanguage("<html>File name does not contain a supported dialect.<br>"
                        + "Name should include either: AU, CA, UK, or US. ");
            }
            Collection<? extends RefexVersionBI<?>> currentDialectRefsetMembers =
                    dialectRefexColl.getRefsetMembersActive(vc);
            ArrayList<RefexStringVersionBI> dialectMemberList = new ArrayList<RefexStringVersionBI>();
            for (RefexVersionBI rv : currentDialectRefsetMembers) {
                RefexStringVersionBI member = (RefexStringVersionBI) rv;
                dialectMemberList.add(member);
            }
            Collection<? extends RefexVersionBI<?>> currentRefsetMembers =
                    refexColl.getRefsetMembersActive(vc);
            ArrayList<RefexStringVersionBI> memberList = new ArrayList<RefexStringVersionBI>();
            for (RefexVersionBI rv : currentRefsetMembers) {
                RefexStringVersionBI member = (RefexStringVersionBI) rv;
                memberList.add(member);
            }
            try {
                String line = br.readLine();
                String[] parts = line.split("\\|");
                int wordIndex = 0;
                int variantIndex = 1;
                if (parts[1].equalsIgnoreCase("text")) {
                    wordIndex = 1;
                    variantIndex = 0;
                }
                line = br.readLine();
                while (line != null) {
                    if(line.length() > 3){
                        parts = line.split("\\|");
                        String word = parts[wordIndex];
                        String variant = parts[variantIndex];
                        boolean found = false;
                        
                        for (RefexStringVersionBI member : dialectMemberList) {
                            if (member.getString1().equals(variant)) {
                                found = true;
                                dialectMemberList.remove(member);
                                break;
                            }
                        }
                        if (!found) {
                            addMember(variant, word);
                        }
                    }
                    
                    line = br.readLine();
                }
                for (RefexStringVersionBI member : dialectMemberList) {
                    retireMember(member, dialectRefexColl);
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

    private void addMember(String variant, String word) throws IOException, InvalidCAB, ContradictionException {
        RefexCAB textRefexSpec = new RefexCAB(TK_REFEX_TYPE.STR,
                refexColl.getNid(), refexColl.getNid());
        textRefexSpec.with(RefexProperty.STRING1, word);
        textRefexSpec.with(RefexProperty.STATUS_NID,
                SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        textRefexSpec.setMemberContentUuid();

        RefexChronicleBI<?> textRefex = tc.constructIfNotCurrent(textRefexSpec);

        RefexCAB variantRefexSpec = new RefexCAB(TK_REFEX_TYPE.STR,
                textRefex.getNid(), dialectRefexColl.getNid());

        variantRefexSpec.with(RefexProperty.STRING1, variant);
        variantRefexSpec.with(RefexProperty.STATUS_NID,
                SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
        variantRefexSpec.setMemberContentUuid();
        tc.constructIfNotCurrent(variantRefexSpec);
        Ts.get().addUncommitted(dialectRefexColl);
        Ts.get().addUncommitted(refexColl);
    }

    private void retireMember(RefexStringVersionBI member, ConceptChronicleBI collConcept ) throws IOException {
        for (int pathNid : ec.getEditPaths()) {
            member.makeAnalog(
                    SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(),
                    Long.MAX_VALUE,
                    ec.getAuthorNid(),
                    ec.getModuleNid(),
                    pathNid);
        }
        Ts.get().addUncommitted(collConcept);
    }
    
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getDialectListFileNameProp() {
        return dialectListFileNameProp;
    }

    public void setDialectListFileNameProp(String dialectListFileNameProp) {
        this.dialectListFileNameProp = dialectListFileNameProp;
    }
}
