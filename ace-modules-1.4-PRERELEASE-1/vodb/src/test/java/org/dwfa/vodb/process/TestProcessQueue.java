package org.dwfa.vodb.process;

import junit.framework.TestCase;

public class TestProcessQueue extends TestCase {

    public void testQueueProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);

        for (int i = 0; i <= 100; i++) {
            processQueue.execute(new SimpleProcessor(30));
        }

        processQueue.awaitCompletion();
        assertTrue("Queue not empty", processQueue.isEmpty());
    }


    public void testQueueFailFastErrorProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);

        for (int i = 0; i <= 100; i++) {
            processQueue.execute(new SimpleProcessor(30));
        }
        processQueue.execute(new ErrorProcessor());

        try{
            processQueue.awaitCompletion();
            fail("Exception not thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
        assertTrue("Queue not empty ", processQueue.isEmpty());
    }

    public void testQueueErrorProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);
        processQueue.setFailFast(false);

        for (int i = 0; i <= 100; i++) {
            processQueue.execute(new SimpleProcessor(30));
        }
        processQueue.execute(new ErrorProcessor());

        try{
            processQueue.awaitCompletion();
            fail("Exception not thrown");
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
        assertTrue("Queue not empty ", processQueue.isEmpty());
    }

    public void testAcceptNoMoreQueueProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);

        for (int i = 0; i <= 100; i++) {
            processQueue.execute(new SimpleProcessor(30));
        }

        processQueue.awaitCompletion();
        assertTrue("Queue not empty", processQueue.isEmpty());

        try{
            processQueue.execute(new SimpleProcessor(30));
            fail("cannot add work items once the pool has awaited completation.");
        } catch(Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    public void testMaxQueueSizeProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);

        for (int i = 0; i <= 200; i++) {
            processQueue.execute(new SimpleProcessor(30));
        }

        processQueue.awaitCompletion();
        assertTrue("Queue not empty", processQueue.isEmpty());

        try{
            processQueue.execute(new SimpleProcessor(30));
            fail("cannot add work items once the pool has awaited completation.");
        } catch(Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    public void testNothingToProcess() throws Exception {
        ProcessQueue processQueue = new ProcessQueue(3);

        processQueue.awaitCompletion();
        assertTrue("Queue not empty", processQueue.isEmpty());

        try{
            processQueue.execute(new SimpleProcessor(30));
            fail("cannot add work items once the pool has awaited completation.");
        } catch(Exception e) {
            assertTrue(e instanceof RuntimeException);
        }
    }

    class SimpleProcessor implements Runnable {
        private long processingTime;


        public SimpleProcessor(long processingTime){
            this.processingTime = processingTime;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(processingTime);
            } catch (InterruptedException ignored) {
            }
        }
    }

    class ErrorProcessor implements Runnable {
        @Override
        public void run() {
            throw new RuntimeException("Test exception handling");
        }
    }
}
