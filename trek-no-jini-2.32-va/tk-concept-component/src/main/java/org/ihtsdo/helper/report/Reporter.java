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
package org.ihtsdo.helper.report;

import java.io.IOException;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

/**
 * Implements the processing of stamp nids and concepts according to the
 * specified date range and list of stamp nids respectively.
 */
public class Reporter implements ProcessStampDataBI, ProcessUnfetchedConceptDataBI {

    Long startDate;
    Long endDate;
    ConcurrentSkipListSet<Integer> foundSapNids = new ConcurrentSkipListSet<Integer>();
    ConcurrentSkipListSet<Integer> resultNids = new ConcurrentSkipListSet<Integer>();
    EnumSet<REPORT_TYPE> reportTypes;

    /**
     * Constructs the reporter object using the starting date and time and the
     * ending date and time.
     *
     * @param startDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @throws ParseException indicates a parse exception occurred
     */
    protected Reporter(String startDate, String endDate) throws ParseException {
        this.startDate = TimeHelper.getTimeFromString(startDate, TimeHelper.getDateFormat());
        this.endDate = TimeHelper.getTimeFromString(endDate, TimeHelper.getDateFormat());
    }

    /**
     * Constructs the reporter object using the starting date and time and the
     * ending date and time.
     *
     * @param startDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endDate the time and date, in the form MM/dd/yy HH:mm:ss
     * @throws ParseException indicates a parse exception occurred
     */
    protected Reporter(String startDate, String endDate, EnumSet<REPORT_TYPE> reportTypes) throws ParseException {
        this.startDate = TimeHelper.getTimeFromString(startDate, TimeHelper.getDateFormat());
        this.endDate = TimeHelper.getTimeFromString(endDate, TimeHelper.getDateFormat());
        this.reportTypes = reportTypes;
    }

    /**
     * Returns a
     * <code>NidList</code> of the nids that changed during the given time
     * period.
     *
     * @return a <code>NidList</code> of nids that met the given criteria
     */
    public NidList report() {  //@akf should be NidSetBI?
        NidList list = new NidList();
        list.addAll(resultNids);
        return list;
    }

    /**
     * Processes the stamp to see if the time is within the specified range.
     *
     * @param sap the <code>StampBI</code> object representing the stamp nid.
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public void processStampData(StampBI sap) throws Exception {
        process(sap);
    }

    /**
     * Processes the stamp to see if the time is within the specified range.
     *
     * @param sap the <code>StampBI</code> object representing the stamp nid.
     */
    private void process(StampBI sap) {
        if (startDate <= sap.getTime() && sap.getTime() <= endDate) {
            foundSapNids.add(sap.getStampNid());
        }
    }

    /**
     * Processes the specified concept based on the
     * <code>reportTypes</code>.
     *
     * @param cNid the nid of the concept to process
     * @param fetcher the fetcher for getting the concept version associated
     * with the <code>cNid</code> from the database
     * @throws Exception indicates an exception occurred
     */
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        if (reportTypes == null || reportTypes.isEmpty()) {
            processConceptAll(fetcher.fetch());
        } else {
            for (REPORT_TYPE type : reportTypes) {
                switch (type) {
                    case CONCEPT:
                        processConcept(fetcher.fetch());
                        break;
                    case DESC:
                        processDesc(fetcher.fetch());
                        break;
                    case REL:
                        processRel(fetcher.fetch());
                        break;
                }
            }
        }
    }

    /**
     * Adds the nid of the
     * <code>concept</code> to the results nids if any of the concept's stamp
     * nids are with in the specified range.
     *
     * @param concept the concept to process
     * @throws IOException indicates an I/O exception has occurred
     */
    private void processConceptAll(ConceptChronicleBI concept) throws IOException {
        Set<Integer> saps = concept.getAllStampNids();
        saps.retainAll(foundSapNids);
        if (!saps.isEmpty()) {
            resultNids.add(concept.getNid());
        }
    }

    /**
     * Adds the nid of the
     * <code>concept</code> to the results nids if any of the concept attribute stamp
     * nids are with in the specified range.
     * @param concept the concept to process
     * @throws IOException indicates an I/O exception has occurred
     */
    private void processConcept(ConceptChronicleBI concept) throws IOException {
        Set<Integer> saps = concept.getConceptAttributes().getAllStampNids();
        saps.retainAll(foundSapNids);
        if (!saps.isEmpty()) {
            resultNids.add(concept.getNid());
        }
    }

    /**
     * Adds the nid of the
     * <code>concept</code> to the results nids if any of the concept's description stamp
     * nids are with in the specified range.
     * @param concept the concept to process
     * @throws IOException indicates an I/O exception has occurred
     */
    private void processDesc(ConceptChronicleBI concept) throws IOException {
        for (DescriptionChronicleBI desc : concept.getDescriptions()) {
            Set<Integer> saps = desc.getAllStampNids();
            saps.retainAll(foundSapNids);
            if (!saps.isEmpty()) {
                resultNids.add(concept.getNid());
            }
        }
    }

    /**
     * Adds the nid of the
     * <code>concept</code> to the results nids if any of the concept's relationship stamp
     * nids are with in the specified range.
     * @param concept the concept to process
     * @throws IOException indicates an I/O exception has occurred
     */
    private void processRel(ConceptChronicleBI concept) throws IOException {
        for (RelationshipChronicleBI rel : concept.getRelationshipsOutgoing()) {
            Set<Integer> saps = rel.getAllStampNids();
            saps.retainAll(foundSapNids);
            if (!saps.isEmpty()) {
                resultNids.add(concept.getNid());
            }
        }
    }

    /**
     * 
     * @return a nid set contains all of the concept nids in the database
     * @throws indicates an I/O exception has occurred
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return Ts.get().getAllConceptNids();
    }

    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }
}
