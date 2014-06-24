/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.mojo.mojo;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.mojo.maven.graph.MojoGraph;
import org.ihtsdo.tk.api.PositionBI;

/**
 * 
 * ReportConceptStatus <br/>
 * <p>
 * The <code>ReportConceptStatus</code> class queries the database to get
 * details of concept status changes over time.
 * </p>
 * <p>
 * This data is then used by an instance of the <code>MojoGraph</code> class to
 * create a graphical representation
 * </p>
 * <p>
 * of the details for use in a maven generated site.
 * </p>
 * <br/>
 * <br/>
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @see <code>org.ihtsdo.mojo.maven.graph.mojoGraph</code>
 * @author PeterVawser
 * @goal reportstatus
 */
public class ReportConceptStatus extends AbstractMojo {

    /**
     * Location of the directory to output data files to.
     * 
     * @parameter expression="${project.build.directory}/.."
     * @required
     */
    private String outputDirectory;

    /**
     * Output file name
     * 
     * @parameter expression="conceptStatusReport"
     * @required
     */
    private String fileName;

    /**
     * Status' to include in report
     * 
     * @parameter
     * @required
     */
    private List<String> includedStatus;

    /**
     * View path name, to source data from
     * 
     * @parameter
     * @required
     */
    private String viewPath;

    private HashMap<String, DataObject> mappedReportStatus = new HashMap<String, DataObject>();

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    private class CheckConceptStatus implements I_ProcessConcepts {
        I_TermFactory termFactory;
        I_IntSet statusTypeSet;

        private HashMap<String, double[][]> statusMap = new HashMap<String, double[][]>();

        private double[][] pointData = new double[60][2];

        public CheckConceptStatus() throws Exception {
            termFactory = Terms.get();
            statusTypeSet = termFactory.newIntSet();

            for (int i = 0; i < 60; i++) {
                pointData[i][0] = 0;
                pointData[i][1] = 0;
            }

            if (includedStatus != null && !includedStatus.isEmpty()) {
                for (String status : includedStatus) {
                    ArchitectonicAuxiliary.Concept archConcept = ArchitectonicAuxiliary.Concept.valueOf(status);
                    if (archConcept != null) {
                        int tmpNativeStatus = termFactory.uuidToNative(archConcept.getUids());

                        mappedReportStatus.put(status, new DataObject(tmpNativeStatus));
                        statusTypeSet.add(tmpNativeStatus);
                    }// End if
                }// End for loop
            }// End if

            if (mappedReportStatus.isEmpty()) {
                int tmpNativeStatus = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
                mappedReportStatus.put("CURRENT", new DataObject(tmpNativeStatus));

                tmpNativeStatus = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
                mappedReportStatus.put("RETIRED", new DataObject(tmpNativeStatus));
            }// End if

        }// End constructor CheckConceptStatus

        public HashMap<String, double[][]> getStatusCounts() {
            return statusMap;
        }

        public void processConcept(I_GetConceptData concept) throws Exception {

            // PathBI architectonicPath = termFactory.getPath(
            // ArchitectonicAuxiliary.
            // Concept.ARCHITECTONIC_BRANCH.
            // getUids());
            //
            // I_Position latestOnArchitectonicPath = termFactory.newPosition(
            // architectonicPath,
            // Integer.MAX_VALUE);
            // Set<I_Position> positionSet = new HashSet<I_Position>();
            // positionSet.add(latestOnArchitectonicPath);

            // TODO replace with passed in config...
            I_ConfigAceFrame activeConfig = Terms.get().getActiveAceFrameConfig();
            PositionSetReadOnly positionSet = null;

            if (!viewPath.equalsIgnoreCase("DEFAULT")) {

                positionSet = new PositionSetReadOnly(new HashSet<PositionBI>((termFactory.getPath(ArchitectonicAuxiliary.Concept.valueOf(viewPath).getUids())
                    .getOrigins())));

            } else {
                positionSet = activeConfig.getViewPositionSetReadOnly();
            }

            List<? extends I_ConceptAttributeTuple> statusTuples = concept.getConceptAttributeTuples(statusTypeSet, positionSet, 
                activeConfig.getPrecedence(), activeConfig.getConflictResolutionStrategy());

            Iterator<? extends I_ConceptAttributeTuple> it = statusTuples.iterator();

            int conceptStatus = 0;
            int versionId = 0;
            while (it.hasNext()) {
                I_ConceptAttributeTuple tuple = it.next();
                conceptStatus = tuple.getStatusId();
                versionId = tuple.getVersion();
            }

            GregorianCalendar checkDate;
            GregorianCalendar prevCheckDate = new GregorianCalendar();

            for (int i = 59; i > -1; i--) {
                checkDate = (GregorianCalendar) prevCheckDate.clone();
                prevCheckDate.add(Calendar.DAY_OF_MONTH, -1);

                long time = ThinVersionHelper.convert(versionId);
                Date partDate = new Date(time);

                /*
                 * Set the time for each data object
                 */
                long axisTime = checkDate.getTime().getTime();

                for (String key : mappedReportStatus.keySet()) {
                    DataObject dataObj = mappedReportStatus.get(key);

                    dataObj.setDataTime(i, axisTime);

                    if (conceptStatus == dataObj.getNaitveStatus()) {
                        if (partDate.before(checkDate.getTime())) {
                            dataObj.incDataCount(i);
                        }// End if
                    }// End if
                }// End for loop
            }// End For Loop

            for (String key : mappedReportStatus.keySet()) {
                DataObject dataObj = mappedReportStatus.get(key);
                statusMap.put(key, dataObj.getData());
            }// End for loop
        }// End method processConcept

        public String toString() {
            return "CheckConceptStatus";
        }

    }// End class CheckConceptStatus

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            I_TermFactory termFactory = Terms.get();
            CheckConceptStatus ccs = new CheckConceptStatus();
            termFactory.iterateConcepts(ccs);

            MojoGraph mg = new MojoGraph(ccs.getStatusCounts(), MojoGraph.DataType.VALUEOVERTIME);
            mg.setTitle("Concept status' for the past 60 days");
            mg.setAxisLabels("Days", "Number of Concepts");
            mg.setSiteTitle("Concept Status' Progression");
            mg.setSiteDesc("The progression of concept status' of a 60 day period are shown below in table and graph format.");
            mg.setOutputDir(outputDirectory);
            mg.setFileName(fileName);
            mg.createGraph();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }// End method execute

    class DataObject extends Object {
        private double[][] data = new double[60][2];
        private int nativeStatus;

        public int getNaitveStatus() {
            return nativeStatus;
        }

        public void setDataTime(int arrayPos1, double timeValue) {
            data[arrayPos1][1] = timeValue;
        }

        public void incDataCount(int arrayPos) {
            data[arrayPos][0]++;
        }

        public double[][] getData() {
            return data;
        }

        public DataObject(int status) {
            nativeStatus = status;
        }// End constructor

    }// End class DataObject

}// End class ConceptStatusReportMojo
