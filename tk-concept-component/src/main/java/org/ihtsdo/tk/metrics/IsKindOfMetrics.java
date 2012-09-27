/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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



package org.ihtsdo.tk.metrics;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Class IsKindOfMetrics.
 *
 * @author kec
 */
public class IsKindOfMetrics implements ProcessUnfetchedConceptDataBI {
   
   /** The kind of count. */
   private AtomicInteger      kindOfCount    = new AtomicInteger();
   
   /** The not kind of count. */
   private AtomicInteger      notKindOfCount = new AtomicInteger();
   
   /** The elapsed sum. */
   AtomicLong                 elapsedSum     = new AtomicLong();
   
   /** The kind of nid. */
   private int                kindOfNid;
   
   /** The store. */
   private TerminologyStoreDI store;
   
   /** The vc. */
   private ViewCoordinate     vc;

    /**
     * Instantiates a new is kind of metrics.
     *
     * @param kindOfNid the kind of nid
     * @param vc the vc
     */
    public IsKindOfMetrics(int kindOfNid, ViewCoordinate vc) {
        this.kindOfNid = kindOfNid;
        this.vc = vc;
        this.store = Ts.get();
    }

   //~--- methods -------------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
    */
   @Override
   public boolean continueWork() {
      return true;
   }

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
    */
   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      long startNanoTime = System.nanoTime();

      if (store.isKindOf(cNid, kindOfNid, vc)) {
         kindOfCount.incrementAndGet();
      } else {
         notKindOfCount.incrementAndGet();
      }

      long elapsedNanoTime = System.nanoTime() - startNanoTime;

      elapsedSum.addAndGet(elapsedNanoTime);
   }

   //~--- get methods ---------------------------------------------------------

   /* (non-Javadoc)
    * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
    */
   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return null;
   }

   /**
    * Gets the report.
    *
    * @return the report
    */
   public String getReport() {
      StringBuilder sb = new StringBuilder();

      sb.append("\nkindOfCount: ").append(kindOfCount);
      sb.append("\nnotKindOfCount: ").append(notKindOfCount);
      sb.append("\nelapsedSum [ns]: ").append(elapsedSum);
      sb.append("\nAverage elapsed time [ns]: ").append((elapsedSum.get()
              / (kindOfCount.get() + notKindOfCount.get())));

      return sb.toString();
   }
}
