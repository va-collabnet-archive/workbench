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
package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * This class is an experiment to improve load, classify, and write performance
 * over what is provided in FasterLoad, which loads quickly, but has horrible
 * write performance.
 * <p>
 * 
 * <ol>
 * <li>Simplify the test to determine if a concept is part of the classification
 * hierarchy. The <code>isParentOfOrEqualTo</code> method is to slow. We will
 * instead check based on the following assumptions.
 * <ul>
 * <li>The is-a relationship is unique to the classification. For example, the
 * SNOMED is-a has a different concept id than the ace-auxiliary is-a
 * relationship. So every concept (except the concept root) will have at least
 * one is-a relationship of the proper type.
 * <li>There is a single root concept, and that root is part of the set of
 * included concept
 * <li>Assume that the versions are linear, independent of path, and therefore
 * the status with the latest date on an allowable path is the latest status.
 * <li>Assume that relationships to retired concepts will have a special status
 * so that retired concepts are not encountered by following current
 * relationships
 * 
 * <ul>
 * </ol>
 * These assumptions should allow determination of included concepts in liner
 * time - O(n), with a relatively small constant since they can be performed
 * with a simple integer comparison on the concept type.
 * 
 * @author kec
 * 
 */

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class LoadClassifyWrite extends AbstractTask {

    final static Object LOCK = new Object();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private static volatile int conceptCount = 0;
    private static volatile int classifyConceptCount = 0;
    private static volatile int relCount = 0;
    private static volatile int relsOnPathCount = 0;
    private static volatile int activeRelCount = 0;
    private static volatile int statedRelCount = 0;

    private static int multipleRelEntriesForVersion = 0;
    private static int multipleRelEntriesForVersion2 = 0;
    private static int multipleAttrEntriesForVersion = 0;

    private static int isaId = Integer.MIN_VALUE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        final int objDataVersion = in.readInt();

        if (objDataVersion <= dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    private static class MockSnorocketFactory implements I_SnorocketFactory {

        private int conceptCount = 0;
        private int isACount = 0;
        private int relationshipCount = 0;

        synchronized public void addConcept(int conceptId, boolean fullyDefined) {
            conceptCount++;
        }

        public void setIsa(int id) {
            isACount++;
        }

        synchronized public void addRelationship(int c1, int rel, int c2, int group) {
            relationshipCount++;
        }

        public void classify() {
            System.err.println("*** " + conceptCount + "\t" + relationshipCount);
        }

        public void getResults(I_Callback callback) {
        }

        public I_SnorocketFactory createExtension() {
            // TODO Auto-generated method stub
            return null;
        }

        public InputStream getStream() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class ProcessConcepts implements I_ProcessConcepts {

        final private ClassifierUtil util;

        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket) throws TerminologyException, IOException {
            util = new ClassifierUtil(worker.getLogger(), rocket);
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            conceptCount++;
            util.processConcept(concept, false);
        }

    }

    public static void logMemory(String tag, I_Work worker) {
        boolean log_memory_p = true;
        if (!log_memory_p)
            return;
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        System.out.println(">>>" + "@" + st[3].getClassName() + "." + st[3].getMethodName() + ":"
            + st[3].getLineNumber() + ">>>" + tag);
        Runtime rt = Runtime.getRuntime();
        // EKM - comment this in to meter memory
        // rt.gc();
        System.out.println(">>>" + "Used memory @ " + tag + ": "
            + ((rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)));
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            logMemory("LCW evaluate start", worker);
            worker.getLogger().info("LCW start evaluate()");

            conceptCount = 0;
            classifyConceptCount = 0;
            relCount = 0;
            relsOnPathCount = 0;
            activeRelCount = 0;
            statedRelCount = 0;

            long startTime = System.currentTimeMillis();

            I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
            // new MockSnorocketFactory();

            final I_TermFactory tf = LocalVersionedTerminology.get();

            if (tf.getActiveAceFrameConfig().getEditingPathSet().size() != 1) {
                throw new TaskFailedException("Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet());
            }

            isaId = tf.uuidToNative(SNOMED.Concept.IS_A.getUids());
            rocket.setIsa(isaId);

            tf.iterateConcepts(new ProcessConcepts(worker, rocket));

            worker.getLogger().info("LCW load time (ms): " + (System.currentTimeMillis() - startTime));
            logMemory("LCW load", worker);

            startTime = System.currentTimeMillis();
            worker.getLogger().info(
                "LCW executionService termination time (ms): " + (System.currentTimeMillis() - startTime));
            worker.getLogger().info(
                "Processed total concepts: " + conceptCount + " for classification: " + classifyConceptCount);
            worker.getLogger().info(
                "Processed total rels: " + relCount + " rels on path: " + relsOnPathCount + " active: "
                    + activeRelCount + " stated: " + statedRelCount);
            worker.getLogger().info("Rels with multiple entries for version: " + multipleRelEntriesForVersion);
            worker.getLogger().info("Rels with multiple entries for version2: " + multipleRelEntriesForVersion2);
            worker.getLogger().info("Concepts with multiple entries for attribute: " + multipleAttrEntriesForVersion);

            worker.getLogger().info("Starting classify. ");
            logMemory("LCW pre classify", worker);
            startTime = System.currentTimeMillis();

            rocket.classify();
            worker.getLogger().info("LCW classify time: " + (System.currentTimeMillis() - startTime));
            logMemory("LCW post classify", worker);

            process.writeAttachment(ProcessKey.SNOROCKET.getAttachmentKey(), rocket);
            logMemory("LCW evaluate end", worker);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
