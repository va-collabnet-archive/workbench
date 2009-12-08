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
package org.dwfa.mojo.epicexport;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LineageHelper;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;
import org.dwfa.tapi.TerminologyException;

/**
 * * GenerateVivisimoThesaurus <br/>
 * <p>
 * The <code>ExportToEpicLoadFilesMojo</code> class generates Epic load files
 * used to populate and update the Epic terminology master files.
 * </p>
 * <p>
 * </p>
 * 
 * 
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Steven Neiner
 * @goal generate-epic-loadfiles
 */
public class ExportToEpicLoadFilesMojo extends AbstractMojo {
    /**
     * Location of the directory to output data files to.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * If true, do not create thesaurus entries for preferred terms with no
     * synonyms.
     * 
     * 
     * 
     */
    // private ExportSpecification[] specs;

    /**
     * Positions to export data.
     * 
     * @parameter
     * @required
     */
    private PositionDescriptor[] positionsForExport;

    /**
     * Status values to include in export
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor[] statusValuesForExport;

    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */

    /**
     * The date to start looking for changes
     * 
     * @parameter
     * @required
     */
    private String deltaStartDate;

    /**
     * The name of the drop
     * 
     * @parameter
     * @required
     */
    private String dropName;

    private File targetDirectory;

    private HashSet<I_Position> positions;

    private I_IntSet statusValues;

    private I_TermFactory termFactory;

    // private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer,
    // RefsetType>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            if (MojoUtil.alreadyRun(getLog(), outputDirectory, this.getClass(), targetDirectory)) {
                return;
            }

            termFactory = LocalVersionedTerminology.get();

            positions = new HashSet<I_Position>(positionsForExport.length);
            for (PositionDescriptor pd : positionsForExport) {
                positions.add(pd.getPosition());
            }

            statusValues = termFactory.newIntSet();
            List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor status : statusValuesForExport) {
                I_GetConceptData statusConcept = status.getVerifiedConcept();
                statusValues.add(statusConcept.getConceptId());
                statusValueList.add(statusConcept);
            }
            getLog().info(" processing concepts for positions: " + positions + " with status: " + statusValueList);

            if (outputDirectory.endsWith("/") == false) {
                outputDirectory = outputDirectory + "/";
            }

            ExportIterator expItr = new ExportIterator(outputDirectory, dropName, deltaStartDate);

            // I_GetConceptData concept =
            // termFactory.getConcept(UUID.fromString("a7130b8c-e6c1-57d8-986a-0d88552c12e4"));
            I_GetConceptData concept = termFactory.getConcept(UUID.fromString("528a6294-a8be-5443-ac3d-e87195f88191"));

            expItr.processConcept(concept);

            // LocalVersionedTerminology.get().iterateConcepts(expItr);
            expItr.close();

        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Unable to export database due to exception", e);
        }

    }// End method execute

    private class ExportIterator implements I_ProcessConcepts {

        private String currentItem;

        private String currentMasterFile;
        private EpicLoadFileFactory exportFactory;
        private EpicExportManager exportManager;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
        private int startingVersion;
        private String dropName;

        public ExportIterator(String outputDir, String drop, String start) {
            Date parsedDate = new Date();

            exportFactory = new EpicLoadFileFactory();
            exportManager = exportFactory.getExportManager(outputDir);
            this.dropName = drop;
            try {
                parsedDate = dateFormat.parse(start);
            } catch (java.text.ParseException e) {
                getLog().error("Invalid date: " + start + " - use format yyyymmssThhmmssZ+8");
                e.printStackTrace();
            }

            this.startingVersion = termFactory.convertToThinVersion(parsedDate.getTime());

        }

        public void close() throws Exception {
            getLog().info("CLOSING");
            this.exportManager.close();
        }

        public void processConcept(I_GetConceptData concept) throws Exception {
            exportRefsetsForConcept(concept);
            List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
            for (Iterator<I_DescriptionVersioned> i = descriptions.iterator(); i.hasNext();) {
                I_DescriptionVersioned desc = i.next();
                List<I_DescriptionTuple> dts = desc.getTuples();
                for (Iterator<I_DescriptionTuple> ti = dts.iterator(); ti.hasNext();) {
                    I_DescriptionTuple descTuple = ti.next();
                }

            }

        }

        private void exportRefsetsForConcept(I_GetConceptData concept) throws TerminologyException, Exception {
            List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());

            for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
                if (termFactory.hasConcept(thinExtByRefVersioned.getRefsetId())) {
                    for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(statusValues,
                        positions, false, false)) {
                        export(thinExtByRefTuple, concept);
                    }
                } else {
                    throw new Exception("No concept for ID " + thinExtByRefVersioned.getRefsetId());
                }
            }

            I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(this.currentMasterFile);
            exportWriter.writeRecord(this.dropName);
            exportWriter.newExportRecord();
        }

        void export(I_ThinExtByRefTuple thinExtByRefTuple, I_GetConceptData parentConcept) throws Exception {
            export(thinExtByRefTuple, thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
                thinExtByRefTuple.getComponentId(), parentConcept, getPreviousVersion(thinExtByRefTuple));
        }

        void export(I_ThinExtByRefTuple thinExtByRefPart, Integer memberId, int refsetId, int componentId,
                I_GetConceptData parentConcept, I_ThinExtByRefPart previousVersion) throws Exception {

            I_GetConceptData refsetConcept = termFactory.getConcept(refsetId);
            String refsetName = refsetConcept.getInitialText();
            exportRefset(refsetName, refsetConcept, thinExtByRefPart, parentConcept, previousVersion);
        }

        private I_ThinExtByRefPart getPreviousVersion(I_ThinExtByRefTuple thinExtByRefTuple) throws Exception {
            List<? extends I_ThinExtByRefPart> versions = thinExtByRefTuple.getVersions();

            I_ThinExtByRefPart newestOldVersion = null;
            // getLog().info("Looking for version " + this.startingVersion);
            for (Iterator<? extends I_ThinExtByRefPart> i = versions.iterator(); i.hasNext();) {
                I_ThinExtByRefPart v = i.next();
                // getLog().info("Checking version " + v.getVersion());
                if (v.getVersion() <= this.startingVersion) {
                    if (newestOldVersion == null) {
                        newestOldVersion = v;
                        // getLog().info("Using version " + v.getVersion());
                    } else if (v.getVersion() > newestOldVersion.getVersion()) {
                        newestOldVersion = v;
                    }
                }
            }
            return newestOldVersion;
        }

        // TODO: Exploratory code
        void exploreConcept(I_GetConceptData concept, I_ThinExtByRefPart thinExtByRefPart) throws Exception {
            for (I_IdPart part : concept.getId().getVersions()) {
                part.getVersion();

            }
        }

        public void exportRefset(String refsetName, I_GetConceptData concept, I_ThinExtByRefTuple thinExtByRefPart,
                I_GetConceptData parentConcept, I_ThinExtByRefPart previousVersion) throws Exception {
            this.setCurrentItem(null, null);
            String stringValue = null;
            String previousStringValue = null;
            String dot1 = null;
            String dot11 = null;
            getLog().info(refsetName);
            // TODO: Re-factor into separate class, allow pattern matching,
            // store and read from pom.xml
            if (refsetName.equals("EDG Billing Item 207")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "207");
            } else if (refsetName.equals("EDG Billing Item 2000")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "2000");
            } else if (refsetName.equals("EDG Billing Item 2")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "2");
                stringValue = getDisplayName(parentConcept);
                previousStringValue = getPreviousDisplayName(parentConcept);
            } else if (refsetName.equals("EDG Billing Item 40")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "40");
            } else if (refsetName.equals("EDG Billing Item 11")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "11");
            } else if (refsetName.equals("EDG Clinical Item 11")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "11");
            } else if (refsetName.equals("EDG Clinical Item 2 National")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2");
                // I_DescriptionVersioned desc =
                // LocalVersionedTerminology.get().getDescription(thinExtByRefPart.getComponentId(),
                // parentConcept.getConceptId());
                // stringValue = desc.toString();
                // previousStringValue = desc.toString();
                stringValue = "no work";
                previousStringValue = stringValue;
                System.out.println("parentConcept=" + getDisplayName(parentConcept));
                System.out.println("concept=" + getDisplayName(concept));
                System.out.println("parentConceptTEST=" + getDisplayNameTEST(parentConcept));
                System.out.println("conceptTEST=" + getDisplayNameTEST(concept));
                System.out.println("xx parentConcept=" + xxgetDisplayName(parentConcept));
                System.out.println("xx concept=" + xxgetDisplayName(concept));
                System.out.println(parentConcept);
                System.out.println(concept);

            } else if (refsetName.equals("EDG Clinical Item 200")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "200");
            } else if (refsetName.equals("EDG Clinical Item 2000")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "2000");
            } else if (refsetName.equals("EDG Clinical Item 40")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "40");
            } else if (refsetName.equals("EDG Clinical Item 80")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "80");
            } else if (refsetName.equals("EDG Clinical Item 91")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "91");
            } else if (refsetName.equals("EDG Clinical Item 100")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "100");
            } else if (refsetName.equals("EDG Clinical Item 207")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "207");
            } else if (refsetName.equals("EDG Clinical Item 7000")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "7000");
            } else if (refsetName.equals("EDG Clinical Item 7010")) {
                this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "7010");
            }

            if (this.currentItem != null) {
                if (stringValue == null)
                    stringValue = getValueAsString(thinExtByRefPart);
                I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(this.currentMasterFile);
                exportWriter.setParentConcept(parentConcept);
                if (previousStringValue == null)
                    previousStringValue = getValueAsString(previousVersion);
                getLog().info(
                    "Exporting item " + this.currentItem + " with a value of " + stringValue
                        + " and a previous value of " + previousStringValue);

                exportWriter.sendItemForExport(this.currentItem, stringValue, previousStringValue);
                /*
                 * Special post handling, such writing id when we encounter a
                 * display name
                 */
                if (refsetName.equals("EDG Clinical Item 2 National")) {
                    dot11 = getIdForConcept(parentConcept, "e3dadc2a-196d-5525-879a-3037af99607d");
                    dot1 = getIdForConcept(parentConcept, "e49a55a7-319d-5744-b8a9-9b7cc86fd1c6");
                    this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "11");
                    exportWriter.sendItemForExport(this.currentItem, dot11, dot11);
                    this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_CLINICAL, "1");
                    exportWriter.sendItemForExport(this.currentItem, dot1, dot1);
                }

            }
        }

        public String getValueAsString(I_ThinExtByRefPart thinExtByRefPart) {
            String value = null;
            if (thinExtByRefPart != null) {
                if (I_ThinExtByRefPartString.class.isAssignableFrom(thinExtByRefPart.getClass())) {
                    I_ThinExtByRefPartString str = (I_ThinExtByRefPartString) thinExtByRefPart;
                    value = str.getStringValue();
                }
                if (I_ThinExtByRefPartInteger.class.isAssignableFrom(thinExtByRefPart.getClass())) {
                    I_ThinExtByRefPartInteger str = (I_ThinExtByRefPartInteger) thinExtByRefPart;
                    value = new Integer(str.getIntValue()).toString();
                }
            }
            return value;

        }

        public void setCurrentItem(String masterFile, String item) {
            this.currentMasterFile = masterFile;
            this.currentItem = item;
        }

        public String xxgetDisplayName(I_GetConceptData conceptData) throws Exception {
            String ret = null;

            conceptData.getDescriptions();

            I_ConfigAceFrame newConfig = NewDefaultProfile.newProfile("", "", "", "", "");
            if (newConfig != null) {
                I_DescriptionTuple it = conceptData.getDescTuple(newConfig.getLongLabelDescPreferenceList(), newConfig);
                if (it == null)
                    ret = null;
                else
                    ret = it.getText();
            }
            return ret;
        }

        public String getDisplayName(I_GetConceptData conceptData) throws Exception {
            String ret = null;

            List<I_DescriptionVersioned> descs = conceptData.getDescriptions();
            for (Iterator<I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
                I_DescriptionVersioned d = i.next();
                I_DescriptionTuple dt = d.getLastTuple();
                I_DescriptionPart part = dt.getPart();
                ret = part.getText();
            }

            return ret;
        }

        public String getPreviousDisplayName(I_GetConceptData conceptData) throws Exception {
            String ret = null;

            List<I_DescriptionVersioned> descs = conceptData.getDescriptions();
            I_DescriptionTuple newestOldTuple = null;
            for (Iterator<I_DescriptionVersioned> i = descs.iterator(); i.hasNext();) {
                I_DescriptionVersioned d = i.next();
                for (I_DescriptionTuple dt : d.getTuples()) {
                    System.out.println(dt.getPart().getText());
                    if (dt.getVersion() < this.startingVersion)
                        if (newestOldTuple == null) {
                            newestOldTuple = dt;
                        } else if (dt.getVersion() > newestOldTuple.getVersion()) {
                            newestOldTuple = dt;
                        }
                }
            }
            if (newestOldTuple != null)
                ret = newestOldTuple.getPart().getText();
            return ret;
        }

        public String getDisplayNameTEST(I_GetConceptData conceptData) throws Exception {

            String ret = null;

            LineageHelper lh = new LineageHelper();
            Set<I_GetConceptData> parents = lh.getAllAncestors(conceptData);
            for (Iterator<I_GetConceptData> i = parents.iterator(); i.hasNext();) {
                I_GetConceptData p = i.next();
                ret = getDisplayName(p);
                getLog().info(ret);
            }

            return ret;
        }

        public String getIdForConcept(I_GetConceptData concept, String idTypeUUID) throws Exception {
            String ret = null;
            I_GetConceptData idSourceConcept = termFactory.getConcept(new UUID[] { UUID.fromString(idTypeUUID) });

            int idSourceNid = idSourceConcept.getConceptId();
            boolean foundIdSource = false;
            for (I_IdPart part : concept.getId().getVersions()) {
                if (part.getSource() == idSourceNid) {
                    foundIdSource = true;
                    ret = part.getSourceId().toString();
                    break;
                }
            }

            return ret;
        }
    }

}
