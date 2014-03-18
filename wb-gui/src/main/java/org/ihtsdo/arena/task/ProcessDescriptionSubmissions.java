/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.arena.task;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.helper.msfile.DescriptionAdditionFileHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

/**
 * Processes description submission files (TDTF) and returns a list of concepts
 * with descriptions added
 *
 * @author akf
 *
 */
@BeanList(specs = {
    @Spec(directory = "tasks/arena", type = BeanType.TASK_BEAN)})
public class ProcessDescriptionSubmissions extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;
    private transient Condition returnCondition;
    private String msFileProp = ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey();
    private String uuidListListPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();//@akf todo: make this a list of concepts to be put in the list view
    private ArrayList<List<UUID>> uuidList;
    private boolean addSecondDialectRefex = false; //true if lang is EN and description is valid for both us and gb spellings
    private LANG_CODE dialect = null;
    private ArrayList<ConceptChronicleBI> conceptsToCommit;
    private ArrayList<String> duplicateDescriptions;
    private ArrayList<String> descMissingParentConcept;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(msFileProp);
        out.writeObject(uuidListListPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            msFileProp = (String) in.readObject();
            uuidListListPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {
        int count = 0;
        try {
            uuidList = new ArrayList<List<UUID>>();
            duplicateDescriptions = new ArrayList<>();
            descMissingParentConcept = new ArrayList<>();
            conceptsToCommit = new ArrayList<>();
            I_ConfigAceFrame config = (I_ConfigAceFrame) worker.readAttachement(
                    WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            String fileName = (String) process.getProperty(
                    ProcessAttachmentKeys.DEFAULT_FILE.getAttachmentKey());
            File file = new File(fileName);
            ArrayList<String> descFileList = DescriptionAdditionFileHelper.getDescFileList(file);
            Iterator<String> iterator = descFileList.iterator();

            Integer uuidPosition = null;
            Integer sctPosition = null;
            Integer descPosition = null;
            Integer langPosition = null;
            Integer dialectPosition = null;
            Integer casePosition = null;

            Integer acceptSct = null;
            Integer acceptUuid = null;

            String line = iterator.next();
            if (line.startsWith("<")) {
                line = iterator.next();
                count++;
            }
            String[] configParts = line.split("\t");

            for (int i = 0; i < configParts.length; i++) {
                String configPos = configParts[i];
                configPos = configPos.trim();
                if (configPos.equalsIgnoreCase("uuid")) {
                    uuidPosition = i;
                } else if (configPos.equalsIgnoreCase("sctId")) {
                    sctPosition = i;
                } else if (configPos.equalsIgnoreCase("description")) {
                    descPosition = i;
                } else if (configPos.equalsIgnoreCase("language")) {
                    langPosition = i;
                } else if (configPos.equalsIgnoreCase("dialect")) {
                    dialectPosition = i;
                } else if (configPos.equalsIgnoreCase("acceptabilitySct")) {
                    acceptSct = i;
                } else if (configPos.equalsIgnoreCase("acceptabilityUuid")) {
                    acceptUuid = i;
                } else if (configPos.equalsIgnoreCase("caseSensitive")) {
                    casePosition = i;
                }
            }

            int conceptNid = 0;
            String descText = null;
            LANG_CODE lang = null;
            int langRefexNid = 0;
            int secondDialectRefexNid = 0;
            int acceptabilityNid = 0;
            boolean caseSensitive = false;

            line = iterator.next();
            if (line.startsWith("<")) {
                line = iterator.next();
                count++;
            }
            count++;
            boolean add = true;
            while (line != null) {
                add = true;
                count++;
                conceptNid = 0;
                descText = null;
                lang = null;
                langRefexNid = 0;
                secondDialectRefexNid = 0;
                acceptabilityNid = 0;
                caseSensitive = false;

                List<UUID> list = new ArrayList<UUID>();
                String[] parts = line.split("\t");
                String part = null;
                if (sctPosition != null) {
                    part = parts[sctPosition];
                    Set<I_GetConceptData> concepts = Terms.get().getConcept(part);
                    for (I_GetConceptData concept : concepts) {
                        conceptNid = concept.getNid();
                    }
                    if (conceptNid == 0) {
                        descMissingParentConcept.add(line);
                        add = false;
                    }
                }
                if (uuidPosition != null && sctPosition == null) {
                    part = parts[uuidPosition];
                    if (!Ts.get().hasUuid(UUID.fromString(part))) {
                        descMissingParentConcept.add(line);
                        add = false;
                    } else {
                        conceptNid = Ts.get().getNidForUuids(UUID.fromString(part));
                    }
                }
                if (add) {
                    if (conceptNid == 0) {
                        throw new TaskFailedException("Must specify either UUID or SCT ID for concept.");
                    }
                    if (descPosition != null) {
                        part = parts[descPosition];
                        descText = part;
                    } else {
                        throw new TaskFailedException("Description text cannot be empty.");
                    }
                    if (langPosition != null) {
                        part = parts[langPosition];
                        lang = LANG_CODE.getLangCode(part);
                    } else {
                        lang = LANG_CODE.EN;
                    }
                    if (dialectPosition != null) {
                        part = parts[dialectPosition];
                        dialect = LANG_CODE.getLangCode(part);
                    } else if (lang == LANG_CODE.EN) {
                        dialect = LANG_CODE.EN_US;
                    }
                    if (casePosition != null) {
                        part = parts[casePosition];
                        if (part.equalsIgnoreCase("true")) {
                            caseSensitive = true;
                        } else if (part.equalsIgnoreCase("false")) {
                            caseSensitive = false;
                        } else {
                            throw new TaskFailedException("Cannot read preference for case sensitivity. Must be either true or false.");
                        }
                    }
                    if (acceptSct != null) {
                        part = parts[acceptSct];
                        Set<I_GetConceptData> concepts = Terms.get().getConcept(part);
                        for (I_GetConceptData concept : concepts) {
                            acceptabilityNid = concept.getNid();
                        }
                    }
                    if (acceptUuid != null) {
                        part = parts[acceptUuid];
                        acceptabilityNid = Ts.get().getNidForUuids(UUID.fromString(part));
                    }
                    if (acceptabilityNid == 0) {
                        acceptabilityNid = SnomedMetadataRfx.getDESC_ACCEPTABLE_NID();
                    }

                    if (dialect == LANG_CODE.EN_US) {
                        langRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
                        checkForDialectVariant(descText);
                        if (addSecondDialectRefex) {
                            secondDialectRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
                        }
                    } else if (dialect == LANG_CODE.EN_GB) {
                        langRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
                        checkForDialectVariant(descText);
                        if (addSecondDialectRefex) {
                            secondDialectRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
                        }
                    } else if (lang == LANG_CODE.NL) {
                        langRefexNid = RefsetAux.NL_REFEX.getStrict(config.getViewCoordinate()).getNid();
                    } else if (lang == LANG_CODE.SV) {
                        langRefexNid = RefsetAux.SV_REFEX.getStrict(config.getViewCoordinate()).getNid();
                    } else if (lang == LANG_CODE.DA) {
                        langRefexNid = RefsetAux.DA_REFEX.getStrict(config.getViewCoordinate()).getNid();
                    } else {
                        throw new TaskFailedException("Cannot determine appropriate language/dialect refset.");
                    }
                    //add description to concept
                    ConceptChronicleBI concept = Ts.get().getConceptForNid(conceptNid);
                    DescriptionCAB descBp = new DescriptionCAB(conceptNid,
                            SnomedMetadataRfx.getDES_SYNONYM_NID(),
                            lang,
                            descText,
                            caseSensitive);
                    TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
                    ComponentChronicleBI<?> component = Ts.get().getComponent(descBp.getComponentUuid());
                    if (component == null) {
                        DescriptionChronicleBI description = builder.construct(descBp);
                        RefexCAB annotBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                description.getNid(),
                                langRefexNid);
                        annotBp.put(RefexCAB.RefexProperty.CNID1, acceptabilityNid);
                        RefexChronicleBI<?> annotation = builder.construct(annotBp);
                        description.addAnnotation(annotation);
                        if (addSecondDialectRefex) {
                            RefexCAB secondAnnotBp = new RefexCAB(TK_REFEX_TYPE.CID,
                                    description.getNid(),
                                    secondDialectRefexNid);
                            secondAnnotBp.put(RefexCAB.RefexProperty.CNID1, acceptabilityNid);
                            RefexChronicleBI<?> secondAnnotation = builder.construct(secondAnnotBp);
                            description.addAnnotation(secondAnnotation);
                            addSecondDialectRefex = false;
                        }
                        conceptsToCommit.add(concept);
                        list.add(Ts.get().getUuidPrimordialForNid(conceptNid));
                        uuidList.add(list);
                    } else {
                        duplicateDescriptions.add(line);
                    }
                }
                if (iterator.hasNext()) {
                    line = iterator.next();
                    if (line.startsWith("<")) {
                        line = iterator.next();
                        count++;
                    }
                } else {
                    line = null;
                }
            }
            process.setProperty(uuidListListPropName, uuidList);
            ACE.suspendDatacheckDisplay();
            for (ConceptChronicleBI concept : conceptsToCommit) {
                Ts.get().addUncommitted(concept);
            }
            Ts.get().waitTillDatachecksFinished();
            ACE.resumeDatacheckDisplay();
            if (!duplicateDescriptions.isEmpty()) {
                worker.getLogger().info("NOT ADDING. Found " + duplicateDescriptions.size() + " existing descriptions matching. ");
                int count1 = 1;
                for (String dup : duplicateDescriptions) {
                    System.out.println("DUP " + count1 + " - " + dup);
                    count1++;
                }
            }
            if (!descMissingParentConcept.isEmpty()) {
                int count1 = 1;
                worker.getLogger().info("NOT ADDING. Found " + descMissingParentConcept.size() + " descriptions with a parent concept that did not exist. ");
                for (String missing : descMissingParentConcept) {
                    System.out.println("MISSING PARENT " + count1 + " - " + missing);
                    count1++;
                }
            }
            returnCondition = Condition.CONTINUE;
        } catch (IndexOutOfBoundsException e) {
            throw new TaskFailedException("<html>Import process is expecting data which is missing.<br>Please review import file. Line: " + count, e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (ParseException e) {
            throw new TaskFailedException(e);
        } catch (UnsupportedDialectOrLanguage e) {
            throw new TaskFailedException(e);
        } catch (InvalidCAB e) {
            throw new TaskFailedException(e);
        } catch (ContradictionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        }
        return returnCondition;
    }

    private void checkForDialectVariant(String descText) throws UnsupportedDialectOrLanguage, IOException {
        if (DialectHelper.isTextForDialect(descText, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(descText, Language.EN_UK.getLenient().getNid())) {
            dialect = LANG_CODE.EN;
            addSecondDialectRefex = true;
        }
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }

    public String getUuidListListPropName() {
        return uuidListListPropName;
    }

    public void setUuidListListPropName(String uuidListListPropName) {
        this.uuidListListPropName = uuidListListPropName;
    }

    public String getMsFileProp() {
        return msFileProp;
    }

    public void setMsFileProp(String msFileProp) {
        this.msFileProp = msFileProp;
    }
}
