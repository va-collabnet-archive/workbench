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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_SupportClassifier;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/classify", type = BeanType.TASK_BEAN) })
public class GetResults extends AbstractTask {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private static int version = Integer.MAX_VALUE;

    private static I_SupportClassifier tf;

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

    public void complete(I_EncodeBusinessProcess arg0, I_Work arg1) throws TaskFailedException {
        // nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            I_SnorocketFactory rocket = (I_SnorocketFactory) process.readAttachement(ProcessKey.SNOROCKET.getAttachmentKey());
            tf = (I_SupportClassifier) LocalVersionedTerminology.get();
            if (tf.getActiveAceFrameConfig().getEditingPathSet().size() != 1) {
                throw new TaskFailedException("Profile must have only one edit path. Found: "
                    + tf.getActiveAceFrameConfig().getEditingPathSet());
            }

            getClassifierResults(worker, rocket);
            worker.getLogger().info("Finished get results. ");
            // worker.getLogger().info(
            // "Stated and inferred: " + statedAndInferredCount
            // + " stated and subsumbed: "
            // + statedAndSubsumedCount + " inferred count: "
            // + inferredRelCount);

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    private void getClassifierResults(I_Work worker, I_SnorocketFactory rocket) throws Exception {
        long startTime = System.currentTimeMillis();
        version = tf.convertToThinVersion(startTime);
        ExecutorService executionService = Executors.newFixedThreadPool(15);

        Future<Boolean> resultFuture = executionService.submit(new ProcessResults(worker, rocket, executionService));
        resultFuture.get();

        worker.getLogger().info("LCW getResults time: " + (System.currentTimeMillis() - startTime));

        executionService.shutdown();
        startTime = System.currentTimeMillis();
        executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        worker.getLogger().info("LCW termination time: " + (System.currentTimeMillis() - startTime));
    }

    private static class ProcessResults implements Callable<Boolean>, I_SnorocketFactory.I_Callback {

        private I_Work worker;
        private I_SnorocketFactory rocket;
        private ExecutorService executionService;

        private I_TermFactory termFactory = LocalVersionedTerminology.get();
        public I_GetConceptData relCharacteristic;
        public I_GetConceptData relRefinability;
        public I_GetConceptData relStatus;
        private Semaphore resultSemaphore = new Semaphore(20);
        private int returnedRelCount = 0;

        public ProcessResults(final I_Work worker, final I_SnorocketFactory rocket, ExecutorService executorService)
                throws Exception {
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executorService;

            relCharacteristic = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            relRefinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());
            relStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            worker.getLogger().info("Inferred id is " + relCharacteristic.getConceptId());
            worker.getLogger().info("Inferred UUIDs are " + relCharacteristic.getUids());
            worker.getLogger().info("Inferred concept is " + relCharacteristic);
        }

        public Boolean call() throws Exception {
            worker.getLogger().info("get results");
            rocket.getResults(this);
            worker.getLogger().info("Returned " + returnedRelCount + " rels.");

            return true;
        }

        public void addRelationship(int conceptId1, int roleId, int conceptId2, int group) {
            try {
                resultSemaphore.acquire();
                returnedRelCount++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (false) {
                // AddNewRelationship addNewRelationship = new
                // AddNewRelationship(
                // conceptId1, roleId, conceptId2, group, resultSemaphore);
                // executionService.execute(addNewRelationship);
            } else {
                resultSemaphore.release();
            }
        }

    }

}
