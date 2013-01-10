/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.lucene.SearchResult;

/**
 * The Class CreatePermissionsInBatchBasedOnCreatorProfile.
 */
@BeanList(specs = {
    @Spec(directory = "tasks/project tasks", type = BeanType.TASK_BEAN)})
public class CreatePermissionsInBatchBasedOnCreatorProfile extends AbstractTask {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1;
    /**
     * The Constant dataVersion.
     */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);

    }

    /**
     * Read object.
     *
     * @param in the in
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * Instantiates a new creates the permissions in batch based on creator
     * profile.
     *
     * @throws MalformedURLException the malformed url exception
     */
    public CreatePermissionsInBatchBasedOnCreatorProfile() throws MalformedURLException {
        super();
    }
    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

        File userFile = new File("../../src/main/users/userPermissionRels.txt");

        try {
            FileReader fr = new FileReader(userFile);
            BufferedReader br = new BufferedReader(fr);

            // Read the header line...
            br.readLine();

            String userLine = br.readLine();

            while (userLine != null) {
                String[] parts = userLine.split("\t");

                addPermission(parts[0], parts[1], parts[2], parts[3], parts[4]);
                userLine = br.readLine();
            }
        } catch (Exception ex) {
            throw new TaskFailedException(ex);
        }

        try {
            Terms.get().commit();
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    /**
     * Adds the permission.
     *
     * @param userName the user name
     * @param typeUid the type uid
     * @param typeName the type name
     * @param targetUid the target uid
     * @param targetName the target name
     * @throws Exception the exception
     */
    private void addPermission(String userName, String typeUid, String typeName,
            String targetUid, String targetName) throws Exception {
        I_TermFactory tf = Terms.get();
        I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

        I_GetConceptData user = null;
        SearchResult results = tf.doLuceneSearch(userName);
        for (int i = 0; i < results.topDocs.scoreDocs.length; i++) {
            Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
            int cnid = Integer.parseInt(doc.get("cnid"));
            int dnid = Integer.parseInt(doc.get("dnid"));
            //System.out.println(doc);
            I_DescriptionVersioned<?> foundDescription = tf.getDescription(dnid);
            if (foundDescription.getTuples(
                    config.getConflictResolutionStrategy()).iterator().next().getText().equals(userName)) {
                user = tf.getConcept(cnid);
                break;
            }
        }
        if (user == null) {
            throw new Exception("User unknown");
        } else {
            tf.newRelationship(UUID.randomUUID(), user,
                    tf.getConcept(UUID.fromString(typeUid)),
                    tf.getConcept(UUID.fromString(targetUid)),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()),
                    tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),
                    0, config);
            tf.addUncommittedNoChecks(user);
        }
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    /* (non-Javadoc)
     * @see org.dwfa.bpa.tasks.AbstractTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[]{};
    }
}