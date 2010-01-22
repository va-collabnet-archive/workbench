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
package org.dwfa.ace.task.commit;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Data test applies only to a concept where at least one description is EDG
 * Clinical Item 2 refset member.<br>
 * <br>
 * If this concept is not a child of one of
 * <ul>
 * <li>Clinical finding,</li>
 * <li>Event,</li>
 * <li>or Situation with explicit contest</li>
 * </ul>
 * THEN warn and block commit.
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/commit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/precommit", type = BeanType.TASK_BEAN),
                   @Spec(directory = "plugins/commit", type = BeanType.TASK_BEAN) })
public class TestForEdgEType2Req extends AbstractExtensionTest {

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    // CORE CONSTANTS
    // private static int isaNid = Integer.MIN_VALUE;
    // private static int nidCURRENT = Integer.MIN_VALUE;
    private static int nidRETIRED = Integer.MIN_VALUE;
    // private static int nidVersion = Integer.MAX_VALUE;

    // UUIDs
    private UUID uuidICD9CodeMappings;

    private UUID uuidEDGClinicalItem_2_National;

    private UUID uuidSnoConClinicalFinding;
    private UUID uuidSnoConEvent;
    private UUID uuidSnoConSituation;

    // NIDs
    private int nidICD9CodeMappings;
    private int nidsEDGClinicalItem_2[];

    private int nidSnoConClinicalFinding;
    private int nidSnoConEvent;
    private int nidSnoConSituation;
    private int[] nidSnoConParents;

    // INTERFACE
    private I_TermFactory tf = null;
    private boolean isInitialized = false;

    @Override
    public List<AlertToDataConstraintFailure> test(I_ThinExtByRefVersioned extension,
            boolean forCommit) throws TaskFailedException {
        ArrayList<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();

        // STEP 0. Setup up all relevant parameters
        if (tf == null || isInitialized == false) {
            tf = LocalVersionedTerminology.get();
            setupUUIDs();
        }
        if (isInitialized == false) {
            return alertList;
        }

        // Check if EDG Clinical Item 2 refset member
        int refSetNid = extension.getRefsetId();
        boolean found = false;
        for (int i = 0; i < nidsEDGClinicalItem_2.length; i++)
            if (refSetNid == nidsEDGClinicalItem_2[i])
                found = true;
        if (!found)
            return alertList; // Return if not Clinical Item 2

        int nidDesc = extension.getComponentId();

        I_DescriptionVersioned descBean = null;
        I_GetConceptData concept = null;
        try {
            // :TODO:NOTE: revisit getDescription() API
            // when newer DB structure in place
            I_Identify descId = tf.getId(nidDesc);
            String descIdStr = descId.getUUIDs().iterator().next().toString();
            descBean = tf.getDescription(descIdStr);
            // :NOTE: get concept which encloses description
            int conNid = descBean.getConceptId();
            concept = tf.getConcept(conNid);
        } catch (TerminologyException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (concept == null)
            return alertList;

        // STEP 1. Find all Descriptions w/ EDG Clinical Item 2 refset member
        List<I_DescriptionVersioned> descList = null;
        try {
            descList = findDescription_Type2(concept);
        } catch (TerminologyException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if (descList == null || descList.size() < 1) {
            // DID NOT FIND AND DESCRIPTIONS AS CLINICAL TYPE 2 MEMBER
            // NOTHING TO TEST HERE, NO ALERT TO ADD
            return alertList;
        }

        // STEP 2. TEST IF DESCRIPTION LENGTH EXCEEDS 80 CHARACTERS
        for (I_DescriptionVersioned desc : descList) {
            for (I_DescriptionPart part : desc.getMutableParts()) {
                if (part.getVersion() == Integer.MAX_VALUE) {
                    int len = part.getText().length();
                    if (len > 80) {
                        alertList.add(new AlertToDataConstraintFailure(
                            (forCommit ? AlertToDataConstraintFailure.ALERT_TYPE.ERROR
                                      : AlertToDataConstraintFailure.ALERT_TYPE.WARNING),
                            "<html><font color=blue>" + part.getText().substring(0, 40)
                                + "</font>... " + "<br>exceeds 80 character limit by  "
                                + (len - 80) + " characters.", concept));
                    }
                }
            }
        }

        // STEP 3. TEST CONCEPT AS CHILD-OF
        List<I_RelVersioned> combList = new ArrayList<I_RelVersioned>();
        try {
            combList.addAll(concept.getSourceRels());
            combList.addAll(concept.getUncommittedSourceRels());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        boolean isChildOf = false;
        for (I_RelVersioned rv : combList) {
            List<? extends I_RelTuple> rvtl = rv.getTuples();
            for (I_RelTuple rt : rvtl) {
                try {

                    boolean testChildOf = SnoTable.testIsaChildOf(nidSnoConParents, rt.getTypeId(),
                        rt.getC1Id());
                    // boolean test = SnoTable.findIsaCycle(rt.getC1Id(),
                    // rt.getTypeId(), rt.getC2Id());
                    if (testChildOf)
                        isChildOf = true;
                } catch (TerminologyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (isChildOf == false) {
            String msg = "<html>EDG Clinical Item 2: NON-CHILD ERROR.<br>"
                + "Must be child of Clinical Finding, Event or Situation. ";
            alertList.add(new AlertToDataConstraintFailure(
                AlertToDataConstraintFailure.ALERT_TYPE.WARNING, msg, concept));
        }

        // STEP 4. IF PRESENT, ICD9 MAPPING MUST NOT BE RETIRED.
        try {
            List<? extends I_ThinExtByRefVersioned> extList;
            extList = concept.getExtensions();
            I_ThinExtByRefVersioned extResult = null;
            I_ThinExtByRefPart extPartResult = null;
            for (I_ThinExtByRefVersioned ext : extList) {
                if (ext.getRefsetId() == nidICD9CodeMappings) {
                    if (extResult == null) {
                        extResult = ext;
                        List<? extends I_ThinExtByRefPart> partList = ext.getMutableParts();
                        int lastVersion = Integer.MIN_VALUE;
                        for (I_ThinExtByRefPart part : partList) {
                            if (part.getVersion() > lastVersion) {
                                lastVersion = part.getVersion();
                                extPartResult = part;
                            }
                        }
                    } else
                        ;
                }
            }

            if (extPartResult != null && extPartResult.getStatusId() == nidRETIRED) {
                String msg = "<html>IDC9-CM Code Mapping must not be RETIRED.";
                alertList.add(new AlertToDataConstraintFailure(
                    AlertToDataConstraintFailure.ALERT_TYPE.WARNING, msg, concept));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return alertList;
    }

    private List<I_DescriptionVersioned> findDescription_Type2(I_GetConceptData concept)
            throws TerminologyException, IOException {
        // Create list of descriptions which have type 2 extensions
        List<I_DescriptionVersioned> resultList = new ArrayList<I_DescriptionVersioned>();

        boolean addUncommited = true;
        List<? extends I_DescriptionVersioned> descList = concept.getDescriptions();
        for (I_DescriptionVersioned desc : descList) {
            List<? extends I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(desc.getNid(),
                addUncommited);
            // check each member for presence of Clinical Type 2 extension
            for (I_ThinExtByRefVersioned ext : extList) {
                int refSetNid = ext.getRefsetId();
                int len = nidsEDGClinicalItem_2.length;
                boolean found = false;
                for (int i = 0; i < len; i++)
                    if (refSetNid == nidsEDGClinicalItem_2[i])
                        found = true;
                if (found)
                    resultList.add(desc);
            }
        }

        descList = concept.getUncommittedDescriptions();
        for (I_DescriptionVersioned desc : descList) {
            List<? extends I_ThinExtByRefVersioned> extList = tf.getAllExtensionsForComponent(desc.getNid(),
                addUncommited);
            // check each member for presence of Clinical Type 2 extension
            for (I_ThinExtByRefVersioned ext : extList) {
                int refSetNid = ext.getRefsetId();
                int len = nidsEDGClinicalItem_2.length;
                boolean found = false;
                for (int i = 0; i < len; i++)
                    if (refSetNid == nidsEDGClinicalItem_2[i])
                        found = true;
                if (found)
                    resultList.add(desc);
            }
        }

        return resultList;
    }

    private List<I_GetConceptData> findRefSets_Type2() throws TerminologyException, ParseException,
            IOException {
        List<I_GetConceptData> returnRefsets = new ArrayList<I_GetConceptData>();

        // Get the children refsets of EDGClinicalItem_2_National refset
        int refsetId = tf.uuidToNative(uuidEDGClinicalItem_2_National);
        I_GetConceptData parent = tf.getConcept(refsetId);
        List<? extends I_RelVersioned> childRelList = parent.getDestRels();
        returnRefsets.add(parent);
        for (I_RelVersioned childRel : childRelList) {

            // :NYI: does not check for latest status of most current version
            // :@@@: treat all child refsets as current for KP Pilot
            int childNid = childRel.getC1Id();
            I_GetConceptData childCB = tf.getConcept(childNid);
            returnRefsets.add(childCB);

        }
        return returnRefsets;
    }

    private void setupUUIDs() {
        uuidICD9CodeMappings = UUID.fromString("30d00210-3bf5-559c-8969-b74b5f85c07e");

        uuidEDGClinicalItem_2_National = UUID.fromString("d1b595e1-5f8c-5c0e-90b4-81445018c76a");

        uuidSnoConClinicalFinding = UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c");
        uuidSnoConEvent = UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698");
        uuidSnoConSituation = UUID.fromString("27d03723-07c3-3de9-828b-76aa05a23438");

        try {
            nidICD9CodeMappings = tf.getConcept(uuidICD9CodeMappings).getConceptId();

            List<I_GetConceptData> type2CBList = findRefSets_Type2();
            int size = type2CBList.size();
            nidsEDGClinicalItem_2 = new int[size];
            for (int i = 0; i < size; i++)
                nidsEDGClinicalItem_2[i] = type2CBList.get(i).getNid();

            nidSnoConClinicalFinding = tf.getConcept(uuidSnoConClinicalFinding).getConceptId();
            nidSnoConEvent = tf.getConcept(uuidSnoConEvent).getConceptId();
            nidSnoConSituation = tf.getConcept(uuidSnoConSituation).getConceptId();

            nidSnoConParents = new int[3];
            nidSnoConParents[0] = nidSnoConClinicalFinding;
            nidSnoConParents[1] = nidSnoConEvent;
            nidSnoConParents[2] = nidSnoConSituation;

            isInitialized = true;
        } catch (TerminologyException e) {
            isInitialized = false;
        } catch (IOException e) {
            isInitialized = false;
        } catch (ParseException e) {
            isInitialized = false;
        }
    }

}
