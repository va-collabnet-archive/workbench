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

import java.util.EnumSet;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidList;


/**
 * Used to get reports for a specified date range.
 */
public class ReportingHelper {

    String startTime;
    String endTime;
    
//      idea for adding specific test classes
//     public ReportingHelper(SapTest... test) {
//        this.startTime = startTime;
//        this.endTime = endTime;
//    }
    
    /**
     * Returns a list of nids which changed during the time specified until present.
     * @param startTime the time and date, in the form MM/dd/yy HH:mm:ss
     * @return a <code>NideList</code> of the nids which changed during the given time period
     * @throws Exception indicates an exception has occurred
     */
    public static NidList getChangedConceptNids(String startTime) throws Exception{
        String endTime = "latest";
        return find(startTime, endTime, null);
    }
    
    /**
     * Returns a list of nids which changed during the given time period.
     * @param startTime the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endTime a <code>NidList</code> of the nids which changed during the given time period
     * @return a <code>NideList</code> of the nids which changed during the given time period
     * @throws Exception indicates an exception has occurred
     */
    public static NidList getChangedConceptNids(String startTime, String endTime) throws Exception{
        return find(startTime, endTime, null);
    }
    
    /**
     * Returns a list of nids which changed during the time specified until present.
     * @param startTime the time and date, in the form MM/dd/yy HH:mm:ss
     * @param reportTypes list of <code>REPORT_TYPES</code> for which the report should return.
     * @return a <code>NideList</code> of the nids which changed during the given time period
     * @throws Exception indicates an exception has occurred
     */
    public static NidList getChangedConceptNids(String startTime, EnumSet<REPORT_TYPE> reportTypes) throws Exception{
        String endTime = "latest";
        return find(startTime, endTime, reportTypes);
    }
    
    /**
     * Returns a list of nids which changed during the given time period.
     * @param startTime the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endTime a <code>NidList</code> of the nids which changed during the given time period
     * @param reportTypes list of <code>REPORT_TYPES</code> for which the report should return.
     * @return <code>NidList</code>
     * @throws Exception indicates an exception has occurred
     */
    public static NidList getChangedConceptNids(String startTime, String endTime, EnumSet<REPORT_TYPE> reportTypes) throws Exception{
        return find(startTime, endTime, reportTypes);
    }
    
    /**
     * Returns a list of concepts nids for concepts which were edited between the specified <code>startTime</code> and <code>endTime</code>.
     * @param startTime the start time
     * @param endTime the end time
     * @param reportTypes the types of edits to consider
     * @return a list of concepts nids for concepts which were edited between the specified <code>startTime</code> and <code>endTime</code>
     * @throws Exception indicates an exception has occurred
     */
    private static NidList find(String startTime, String endTime, EnumSet<REPORT_TYPE> reportTypes) throws Exception {
        if(reportTypes == null){
            Reporter dateReporter = new Reporter(startTime, endTime);
            Ts.get().iterateStampDataInSequence(dateReporter);
            Ts.get().iterateConceptDataInParallel(dateReporter);
            return dateReporter.report();
        }
        
        Reporter dateReporterByType = new Reporter(startTime, endTime, reportTypes);
        Ts.get().iterateStampDataInSequence(dateReporterByType);
        Ts.get().iterateConceptDataInParallel(dateReporterByType);
        return dateReporterByType.report();
    }
}
