package org.dwfa.ace.task.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessRelationships;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ace/classify", type = BeanType.TASK_BEAN) })
public class FasterLoad extends AbstractTask {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

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
        
        public void addConcept(int conceptId, boolean fullyDefined) {
            conceptCount++;
            
        }

        public void addIsa(int id) {
            isACount++;
            
        }

        public void addRelationship(int c1, int rel, int c2, int group) {
            relationshipCount++;
            
        }

        public void classify() {
            // TODO Auto-generated method stub
            
        }

        public void getResults(I_Callback callback) {
            // TODO Auto-generated method stub
            
        }

        public int getRowCount() {
            return conceptCount + isACount + relationshipCount;
        }
        
    }
    private static class TestForPrimitiveAndAdd implements Runnable {

        I_SnorocketFactory rocket;
        I_GetConceptData concept;
        
        public TestForPrimitiveAndAdd(I_SnorocketFactory rocket, I_GetConceptData concept) {
            super();
            this.rocket = rocket;
            this.concept = concept;
        }

        public void run() {
            //check if primitive or defined
            
            boolean fullyDefined = true;
            //check if valid status & path
            
            rocket.addConcept(concept.getConceptId(), fullyDefined);
            
        }
        
    }
    
    private static class TestForAllowedRelAndAdd implements Runnable {

        I_SnorocketFactory rocket;
        I_RelVersioned versionedRel;
        
        public TestForAllowedRelAndAdd(I_SnorocketFactory rocket, I_RelVersioned versionedRel) {
            super();
            this.rocket = rocket;
            this.versionedRel = versionedRel;
        }

        public void run() {
            //check if primitive or defined
            
            int type = 0;
            int group = 0;
            
            rocket.addRelationship(versionedRel.getC1Id(), type, versionedRel.getC2Id(), group);
            
        }
        
    }

    private static class ProcessConcepts implements I_ProcessConcepts, Callable<Boolean> {

        private int conceptCount = 0;
        
        private I_Work worker;

        private I_SnorocketFactory rocket;
        
        ExecutorService executionService;
        
        public ProcessConcepts(I_Work worker, I_SnorocketFactory rocket, ExecutorService executionService) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
        }
        
        public void processConcept(I_GetConceptData concept) throws Exception {
            conceptCount++;
            executionService.execute(new TestForPrimitiveAndAdd(rocket, concept));
            
        }
        public Boolean call() throws Exception {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            termFactory.iterateConcepts(this);
            worker.getLogger().info("Processed concepts: " + conceptCount);
            return true;
        }
        
    }
    
    private static class ProcessRelationships implements I_ProcessRelationships, Callable<Boolean> {
        
        private int relCount = 0;
        
        private I_Work worker;
        
        private I_SnorocketFactory rocket;
        
        ExecutorService executionService;

        public ProcessRelationships(I_Work worker, I_SnorocketFactory rocket, ExecutorService executionService) {
            super();
            this.worker = worker;
            this.rocket = rocket;
            this.executionService = executionService;
        }

        public void processRelationship(I_RelVersioned versionedRel) throws Exception {
            relCount++;
            executionService.execute(new TestForAllowedRelAndAdd(rocket, versionedRel));
        }
        public Boolean call() throws Exception {
            I_TermFactory termFactory = LocalVersionedTerminology.get();
            termFactory.iterateRelationships(this);
            worker.getLogger().info("Processed relationships: " + relCount);
            return true;
        }
        
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        
        try {
            long startTime = System.currentTimeMillis();
            
            ExecutorService executionService = Executors.newFixedThreadPool(6);
            MockSnorocketFactory rocket = new MockSnorocketFactory();
            
            Future<Boolean> conceptFuture =  executionService.submit(new ProcessConcepts(worker, rocket, executionService));
            Future<Boolean> relFuture =  executionService.submit(new ProcessRelationships(worker, rocket, executionService));
            
            conceptFuture.get();
            relFuture.get();
            
            worker.getLogger().info("FasterLoad load time: " + (System.currentTimeMillis() - startTime));
            
            executionService.shutdown();
            executionService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            startTime = System.currentTimeMillis();
            worker.getLogger().info("FasterLoad termination time: " + (System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
            rocket.classify();
            worker.getLogger().info("FasterLoad classify time: " + (System.currentTimeMillis() - startTime));
            
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
        return new int[]{};
    }

}
