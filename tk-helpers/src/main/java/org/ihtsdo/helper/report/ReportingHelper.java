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

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;


/**
 *Used to get reports for a specified date range.
 * @author akf
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
     * @throws Exception
     */
    public static NidList getReport(String startTime) throws Exception{
        String endTime = "latest";
        return find(startTime, endTime);
    }
    
    /**
     * Returns a list of nids which changed during the given time period.
     * @param startTime the time and date, in the form MM/dd/yy HH:mm:ss
     * @param endTime a <code>NidList</code> of the nids which changed during the given time period
     * @return <code>NidList</code>
     * @throws Exception
     */
    public static NidList getReport(String startTime, String endTime) throws Exception{
        return find(startTime, endTime);
    }
    
    private static NidList find(String startTime, String endTime) throws Exception {
        Reporter dateReporter = new Reporter(startTime, endTime);
        Ts.get().iterateSapDataInSequence(dateReporter);
        Ts.get().iterateConceptDataInParallel(dateReporter);
        return dateReporter.report();
    }
}
