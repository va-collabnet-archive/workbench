/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
 * The Class IsKindOfMetrics finds and reports metrics on how many concepts are a kind
 * of the specified concept.
 *
 */
public class IsKindOfMetrics implements ProcessUnfetchedConceptDataBI {

    private AtomicInteger kindOfCount = new AtomicInteger();
    private AtomicInteger notKindOfCount = new AtomicInteger();
    AtomicLong elapsedSum = new AtomicLong();
    private int kindOfNid;
    private TerminologyStoreDI store;
    private ViewCoordinate vc;

    /**
     * Instantiates a new is kind of metrics based on the given parent nid and
     * <code>viewCoordiante</code>.
     *
     * @param kindOfNid the nid representing the parent concept
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     */
    public IsKindOfMetrics(int kindOfNid, ViewCoordinate viewCoordinate) {
        this.kindOfNid = kindOfNid;
        this.vc = viewCoordinate;
        this.store = Ts.get();
    }

    //~--- methods -------------------------------------------------------------
    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     * Determines if the specified concept is a logical KIND-OF the concept
     * associated with this
     * <code>IsKindOfMetrics</code>.
     *
     * @param cNid the nid representing the concept to process
     * @param fetcher to fetch the specified concept from the database
     * @throws Exception indicates an exception has occurred
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
    /**
     *
     * @return a set of nids representing the concepts to process
     * @throws IOException
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return null;
    }

    /**
     * Gets the report of how many concepts are a kind of the specified concept.
     * Reports the count of kind of, not kind of, total, and averaged elapsed
     * time.
     *
     * @return a String representing the report
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
