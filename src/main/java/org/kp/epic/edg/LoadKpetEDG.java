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
package org.kp.epic.edg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.id.Type5UuidFactory;

@BeanList(specs = { @Spec(directory = "tasks/kp/edg", type = BeanType.TASK_BEAN) })
public class LoadKpetEDG extends AbstractTask {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String inputFilePropName = "A: INPUT_FILE";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            inputFilePropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    private static final int CMS_ID_FIELD = 0;
    private static final int FSN_FIELD = 1;
    private static final int DESCRIPTION_FIELD = 2;
    private static final int DESCRIPTION_TYPE_FIELD = 3;
    private static final int REFSET_NAME_FIELD = 4;
    private static final int DESCRIPTION_STATUS_FIELD = 5;
    private static final int REFSET_REASON_FOR_SOFT_DELETE_FIELD = 6;
    private static final int DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD = 7;
    private static final int DESCRIPTION_EXTERNAL_DOT1_FIELD = 8;
    private static final int DESCRIPTION_EXTERNAL_ITEM_11_FIELD = 9;
    private static final int DESCRIPTION_EXTERNAL_ITEM_40_FIELD = 10;
    private static final int DESCRIPTION_EXTERNAL_ITEM_100_FIELD = 11;
    private static final int DESCRIPTION_EXTERNAL_ITEM_200_FIELD = 12;
    private static final int DESCRIPTION_EXTERNAL_ITEM_207_FIELD = 13;
    private static final int DESCRIPTION_EXTERNAL_ITEM_2000_FIELD = 14;
    private static final int DESCRIPTION_EXTERNAL_ITEM_7010_FIELD = 15;

    int count = 0;

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String inputFileName = (String) process.getProperty(inputFilePropName);
            File inputFile = new File(inputFileName);
            if (!inputFile.exists() || !inputFile.canRead()) {
                throw new TaskFailedException("Specified file '" + inputFileName
                    + "' either does not exist or cannot be read");
            }

            I_TermFactory tf = LocalVersionedTerminology.get();
            new File("edg-kpe").mkdirs();
            FileWriter descriptionsWriter = new FileWriter(new File("edg-kpe/descriptions.txt"));
            FileWriter idsWriter = new FileWriter(new File("edg-kpe/ids.txt"));
            FileWriter booleanRefSetWriter = new FileWriter(new File("edg-kpe/boolean.refset"));
            FileWriter integerRefSetWriter = new FileWriter(new File("edg-kpe/integer.refset"));
            FileWriter stringRefSetWriter = new FileWriter(new File("edg-kpe/string.refset"));

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String s;
            reader.readLine(); // skip first line...

            I_ConfigAceFrame profile = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
            int line = 0;
            while ((s = reader.readLine()) != null) {
                line++;
                if (line % 1000 == 0) {
                    AceLog.getAppLog().info("Line: " + line);
                }
                String fields[] = s.split("\t");
                if (fields != null && fields.length > 0) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].startsWith("\"") && fields[i].endsWith("\"")) {
                            fields[i] = fields[i].substring(1, fields[i].length() - 1);
                        }
                    }
                    String csmCid = fields[CMS_ID_FIELD];
                    UUID conceptUuid = Type5UuidFactory.get("org.kp." + csmCid);
                    count++;
                    checkForDescriptionAndAdd(tf, fields, conceptUuid, profile, descriptionsWriter, idsWriter,
                        booleanRefSetWriter, integerRefSetWriter, stringRefSetWriter);
                }
            }
            reader.close();
            descriptionsWriter.close();
            idsWriter.close();
            booleanRefSetWriter.close();
            integerRefSetWriter.close();
            stringRefSetWriter.close();
            AceLog.getAppLog().info("Processed KPET: " + count + " records.");

            return Condition.CONTINUE;

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    private static String currentUuidStr;

    private static String getCurrentUuidStr() {
        if (currentUuidStr == null) {
            currentUuidStr = ArchitectonicAuxiliary.Concept.CURRENT.getUids().iterator().next().toString();
        }
        return currentUuidStr;
    }

    private static String getEffectiveDate() {
        return "2009-11-15 00:00:00";
    }

    private static String getPathUuidStr() {
        return "2bfc4102-f630-5fbe-96b8-625f2a6b3d5a";
    }

    private static String synonymUuidStr;

    private static String getSynonymUuidStr() {
        if (synonymUuidStr == null) {
            synonymUuidStr = ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()
                .iterator()
                .next()
                .toString();
        }
        return synonymUuidStr;
    }

    private static void checkForDescriptionAndAdd(I_TermFactory tf, String[] fields, UUID conceptUuid,
            I_ConfigAceFrame config, FileWriter descriptionsWriter, FileWriter idsWriter,
            FileWriter booleanRefSetWriter, FileWriter integerRefSetWriter, FileWriter stringRefSetWriter)
            throws TerminologyException, IOException, NoSuchAlgorithmException {
        I_GetConceptData concept = tf.getConcept(new UUID[] { conceptUuid });
        I_DescriptionVersioned matchedDesc = null;
        UUID descId = null;
        for (I_DescriptionVersioned desc : concept.getDescriptions()) {
            for (I_DescriptionPart dt : desc.getVersions()) {
                if (dt.getText().toLowerCase().equals(fields[DESCRIPTION_FIELD].toLowerCase())) {
                    matchedDesc = desc;
                    descId = tf.getId(desc.getDescId()).getUIDs().iterator().next();
                    break;
                }
            }
            if (matchedDesc != null) {
                break;
            }
        }
        if (matchedDesc == null) {
            // Create new description.
            descId = Type5UuidFactory.get("org.kp.desc." + fields[DESCRIPTION_FIELD].toLowerCase()
                + concept.getUids().iterator().next());

            descriptionsWriter.write(descId.toString());
            descriptionsWriter.write("\t");
            descriptionsWriter.write(getCurrentUuidStr());
            descriptionsWriter.write("\t");
            descriptionsWriter.write(concept.getUids().iterator().next().toString());
            descriptionsWriter.write("\t");
            descriptionsWriter.write(fields[DESCRIPTION_FIELD].toLowerCase());
            descriptionsWriter.write("\t");
            descriptionsWriter.write("0");
            descriptionsWriter.write("\t");
            descriptionsWriter.write(getSynonymUuidStr());
            descriptionsWriter.write("\t");
            descriptionsWriter.write("en");
            descriptionsWriter.write("\t");
            descriptionsWriter.write(getEffectiveDate()); // effective date
            descriptionsWriter.write("\t");
            descriptionsWriter.write(getPathUuidStr()); // path id
            descriptionsWriter.write("\n");
        }

        // clinical item 11
        if (fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD].length() > 0) {
            idsWriter.write(descId.toString());
            idsWriter.write("\t");
            idsWriter.write("e3dadc2a-196d-5525-879a-3037af99607d");
            idsWriter.write("\t");
            idsWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_11_FIELD]);
            idsWriter.write("\t");
            idsWriter.write(getCurrentUuidStr());
            idsWriter.write("\t");
            idsWriter.write(getEffectiveDate()); // effective date
            idsWriter.write("\t");
            idsWriter.write(getPathUuidStr()); // path id
            idsWriter.write("\n");
        }
        // clinical dot 1
        if (fields[DESCRIPTION_EXTERNAL_DOT1_FIELD] != null && fields[DESCRIPTION_EXTERNAL_DOT1_FIELD].length() > 0) {

            idsWriter.write(descId.toString());
            idsWriter.write("\t");
            idsWriter.write("e49a55a7-319d-5744-b8a9-9b7cc86fd1c6");
            idsWriter.write("\t");
            idsWriter.write(fields[DESCRIPTION_EXTERNAL_DOT1_FIELD]);
            idsWriter.write("\t");
            idsWriter.write(getCurrentUuidStr());
            idsWriter.write("\t");
            idsWriter.write(getEffectiveDate()); // effective date
            idsWriter.write("\t");
            idsWriter.write(getPathUuidStr()); // path id
            idsWriter.write("\n");
        }

        // CSMID
        if (fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD].length() > 0) {

            idsWriter.write(descId.toString());
            idsWriter.write("\t");
            idsWriter.write("01384e4a-844c-5c23-aac5-fc0a28b6a2b7");
            idsWriter.write("\t");
            idsWriter.write(fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
            idsWriter.write("\t");
            idsWriter.write(getCurrentUuidStr());
            idsWriter.write("\t");
            idsWriter.write(getEffectiveDate()); // effective date
            idsWriter.write("\t");
            idsWriter.write(getPathUuidStr()); // path id
            idsWriter.write("\n");
        }

        // Add to refsets...

        // Region refset...
        UUID refsetConceptId = Type5UuidFactory.get("org.kp.refset." + fields[REFSET_NAME_FIELD]);
        UUID memberId = Type5UuidFactory.get("org.kp.refset.member."
            + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD] + "." + refsetConceptId + ".true");

        booleanRefSetWriter.write(refsetConceptId.toString());
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write(memberId.toString());
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write(getCurrentUuidStr());
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write(descId.toString());
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write(getEffectiveDate()); // effective date
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write(getPathUuidStr()); // path id
        booleanRefSetWriter.write("\t");
        booleanRefSetWriter.write("true");
        booleanRefSetWriter.write("\n");

        // item 207
        if (fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD].length() > 0) {
            UUID item207RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 207");
            UUID item207MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 207."
                + fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
            integerRefSetWriter.write(item207RefsetId.toString());
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(item207MemberId.toString());
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(getCurrentUuidStr());
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(descId.toString());
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(getEffectiveDate()); // effective date
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(getPathUuidStr()); // path id
            integerRefSetWriter.write("\t");
            integerRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_207_FIELD]);
            integerRefSetWriter.write("\n");
        }

        // item 7010
        if (fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD].length() > 0) {
            UUID item7010RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 7010");
            UUID item7010MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 7010."
                + fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);

            stringRefSetWriter.write(item7010RefsetId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(item7010MemberId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getCurrentUuidStr());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(descId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getEffectiveDate()); // effective date
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getPathUuidStr()); // path id
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_7010_FIELD]);
            stringRefSetWriter.write("\n");
        }

        // item 2000
        if (fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD].length() > 0) {
            UUID item2000RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 2000");
            UUID item2000MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 2000."
                + fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);

            stringRefSetWriter.write(item2000RefsetId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(item2000MemberId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getCurrentUuidStr());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(descId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getEffectiveDate()); // effective date
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getPathUuidStr()); // path id
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_2000_FIELD]);
            stringRefSetWriter.write("\n");
        }

        // item 200
        if (fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD].length() > 0) {
            UUID item200RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 200");
            UUID item200MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 200."
                + fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);

            stringRefSetWriter.write(item200RefsetId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(item200MemberId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getCurrentUuidStr());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(descId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getEffectiveDate()); // effective date
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getPathUuidStr()); // path id
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_200_FIELD]);
            stringRefSetWriter.write("\n");
        }

        // item 100
        if (fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD].length() > 0) {
            UUID item100RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 100");
            UUID item100MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 100."
                + fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);
            stringRefSetWriter.write(item100RefsetId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(item100MemberId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getCurrentUuidStr());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(descId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getEffectiveDate()); // effective date
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getPathUuidStr()); // path id
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_100_FIELD]);
            stringRefSetWriter.write("\n");
        }

        // item 40
        if (fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD] != null
            && fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD].length() > 0) {
            UUID item40RefsetId = Type5UuidFactory.get("org.kp.refset.EDG Clinical Item 40");
            UUID item40MemberId = Type5UuidFactory.get("org.kp.refset.member.EDG Clinical Item 40."
                + fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);

            stringRefSetWriter.write(item40RefsetId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(item40MemberId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getCurrentUuidStr());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(descId.toString());
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getEffectiveDate()); // effective date
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(getPathUuidStr()); // path id
            stringRefSetWriter.write("\t");
            stringRefSetWriter.write(fields[DESCRIPTION_EXTERNAL_ITEM_40_FIELD]);
            stringRefSetWriter.write("\n");
        }

        if (fields[DESCRIPTION_STATUS_FIELD].trim().toLowerCase().equals("slate for sd")) {
            if (fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD] != null
                && fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD].length() > 0) {
                // Reason for soft delete
                UUID reasonForSoftDeleteRefsetId = Type5UuidFactory.get("org.kp.refset.Reason for Soft Delete");
                UUID reasonForSoftDeleteMemberId = Type5UuidFactory.get("org.kp.refset.member.Reason for Soft Delete."
                    + fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD] + fields[DESCRIPTION_EXTERNAL_CLINICAL_CSMID_FIELD]);

                stringRefSetWriter.write(reasonForSoftDeleteRefsetId.toString());
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(reasonForSoftDeleteMemberId.toString());
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(getCurrentUuidStr());
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(descId.toString());
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(getEffectiveDate()); // effective date
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(getPathUuidStr()); // path id
                stringRefSetWriter.write("\t");
                stringRefSetWriter.write(fields[REFSET_REASON_FOR_SOFT_DELETE_FIELD]);
                stringRefSetWriter.write("\n");
            }
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getInputFilePropName() {
        return inputFilePropName;
    }

    public void setInputFilePropName(String inputFilePropName) {
        this.inputFilePropName = inputFilePropName;
    }

}
