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
package org.ihtsdo.helper.report;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 *Implements the processing of sap nids and concepts according to the
 * specified date range and list of sap nids respectively.
 * @author akf
 */
public class Reporter implements ProcessSapDataBI, ProcessUnfetchedConceptDataBI {
    Long startDate;
    Long endDate;
    ConcurrentSkipListSet<Integer> foundSapNids = new ConcurrentSkipListSet<Integer>();
    ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<Integer>();

    /**
     * Constructs the reporter object using the starting date and time and the
     * ending date and time.
     * @param startDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @throws ParseException
     */
    protected Reporter(String startDate, String endDate) throws ParseException {
        this.startDate = TimeHelper.getTimeFromString(startDate, TimeHelper.getDateFormat());
        this.endDate = TimeHelper.getTimeFromString(endDate, TimeHelper.getDateFormat());
    }

    @Override
    public void processSapData(SapBI sap) throws Exception {
        process(sap);
    }
    
    private void process(SapBI sap){
        if(startDate <= sap.getTime() && sap.getTime()<= endDate){
            foundSapNids.add(sap.getSapNid());
        }
    }

    
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        processConcept(fetcher.fetch());
    }
    
    private void processConcept(ConceptChronicleBI concept) throws IOException{
        for(int sapNid : concept.getAllSapNids()){
            if(foundSapNids.contains(sapNid)){
                if(!resultNids.contains(concept.getNid())){
                    resultNids.add(concept.getNid());
                }
                break;
            }
        }
    }

    
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return Ts.get().getAllConceptNids();
    }

    
    @Override
    public boolean continueWork() {
        return true;
    }
    
    /**
     * Returns a <code>NidList</code> of the nids that changed during the given time period.
     * @return a <code>NidList</code> of nids that met the given criteria
     */
    public NidList report(){  //@akf should be NidSetBI?
        NidList list = new NidList();
        list.addAll(resultNids);
        return list;
    }
}
