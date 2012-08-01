/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

/**
 *
 * @author kec
 */
public class IsKindOfMetrics implements ProcessUnfetchedConceptDataBI {
   private AtomicInteger      kindOfCount    = new AtomicInteger();
   private AtomicInteger      notKindOfCount = new AtomicInteger();
   AtomicLong                 elapsedSum     = new AtomicLong();
   private int                kindOfNid;
   private TerminologyStoreDI store;
   private ViewCoordinate     vc;

    public IsKindOfMetrics(int kindOfNid, ViewCoordinate vc) {
        this.kindOfNid = kindOfNid;
        this.vc = vc;
        this.store = Ts.get();
    }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean continueWork() {
      return true;
   }

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

   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return null;
   }

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
