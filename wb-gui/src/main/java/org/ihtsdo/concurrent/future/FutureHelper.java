
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.concurrent.future;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.log.AceLog;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author kec
 */
public class FutureHelper implements Runnable {
   static final Thread                futureHelperThread = new Thread(new FutureHelper(), "FutureHelper");
   static LinkedBlockingQueue<Future> futures            = new LinkedBlockingQueue<Future>();

   //~--- static initializers -------------------------------------------------

   static {
      futureHelperThread.setDaemon(true);
      futureHelperThread.start();
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void run() {
      while (true) {
         try {
            Future f = futures.take();

            f.get();
         } catch (Throwable ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }
   
   public static void addFuture(Future f) {
        try {
            futures.put(f);
        } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
   }
}
