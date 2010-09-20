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
package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.text.StringToWord;
import org.dwfa.vodb.types.IntSet;

/**
 * Uses lucene search api to search versioned database
 * 
 * @author Susan Castillo
 * 
 */
/**
 * 
 * @goal vodb-find-duplicates
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbFindDuplicates extends AbstractMojo {

    private enum DupFinderType {
        TermFactory, Fuzzy, Snowball
    }

    /**
     * Type of search
     * 
     * @parameter
     * @required
     */
    private String searchTypeStr;
    /**
     * This will search for duplicates within all the children of the search
     * root. If search root is null the entire database is searched.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor searchRootDescriptor;

    /**
     * Location of the lucene directory.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/berkeley-db/lucene"
     * @required
     */
    private File luceneDir;

    /**
     * Location to write the potential match results.
     * 
     * @parameter
     * @required
     */
    private File dupPotMatchResults;

    /**
     * Threshold for including search results as a potiential duplicate.
     * 
     * @parameter
     * @required
     */
    private String thresholdStr;

    /**
     * Add scoring explanation to html output file
     * 
     * @parameter
     * @required
     */
    private boolean explanationRequested;
    /**
     * Location to write the details file for each match.
     * 
     * @parameter
     * @required
     */
    private File detailsDir;

    /**
     * Only consider matches that are part of the root hierarchy
     * 
     * @parameter
     * @required
     */
    private boolean onlyMatchInRoot;

    private I_GetConceptData xhtmlFullySpecifed;

    private I_TermFactory termFactory;

    private I_GetConceptData xhtmlPreferred;

    private I_GetConceptData preferred;

    private I_GetConceptData fullySpecified;

    private I_GetConceptData synonym;

    private I_GetConceptData xhtmlsynonym;

    private I_GetConceptData isPotDupRelType;

    private I_GetConceptData isActualDupRel;

    private I_GetConceptData isNotDupRel;

    private I_GetConceptData flaggedPotDup;

    private I_GetConceptData temporaryStatus;

    private I_GetConceptData currentStatus;

    private I_GetConceptData charAdditional;

    private I_GetConceptData notRefinable;

    private I_IntSet descTypeSet;

    private I_IntSet dupRelTypeSet;

    private I_GetConceptData rootConcept;

    private BufferedWriter htmlReportWriter;

    private BufferedWriter dwfaDupDataWriter;
    private BufferedWriter dwfaNotDupDataWriter;

    private Set<Collection<UUID>> dupUuidCollectionSet = new HashSet<Collection<UUID>>();
    private Set<Collection<UUID>> notDupSet = new HashSet<Collection<UUID>>();

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + luceneDir.getCanonicalPath()
                + searchRootDescriptor.getDescription(), this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        try {
            double threshold = Double.parseDouble(thresholdStr);
            DupFinderType finderType = DupFinderType.valueOf(searchTypeStr);
            detailsDir.mkdirs();

            dupPotMatchResults.mkdirs(); // create directory

            File dwfaDupDataFile = new File(dupPotMatchResults, "dwfaDups.txt");
            FileWriter ddfw = new FileWriter(dwfaDupDataFile);
            dwfaDupDataWriter = new BufferedWriter(ddfw);

            File dwfaNotDupDataFile = new File(dupPotMatchResults, "dwfaNotDups.txt");
            FileWriter ddndfw = new FileWriter(dwfaNotDupDataFile);
            dwfaNotDupDataWriter = new BufferedWriter(ddndfw);

            File htmlReportFile = new File(dupPotMatchResults, "potDupsAll.html");
            FileWriter hrfw = new FileWriter(htmlReportFile);
            htmlReportWriter = new BufferedWriter(hrfw);
            htmlReportWriter.append("<html>");

            termFactory = LocalVersionedTerminology.get(); // gives me access
            // to concepts
            // (terminology
            // server object)

            xhtmlFullySpecifed = termFactory.getConcept(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.getUids());
            xhtmlPreferred = termFactory.getConcept(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids());
            preferred = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
            fullySpecified = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
            synonym = termFactory.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
            xhtmlsynonym = termFactory.getConcept(ArchitectonicAuxiliary.Concept.XHTML_SYNONYM_DESC_TYPE.getUids());

            flaggedPotDup = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FLAGGED_POTENTIAL_DUPLICATE.getUids());

            isPotDupRelType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_POT_DUP_REL.getUids());

            isActualDupRel = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.getUids());

            isNotDupRel = termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_NOT_A_DUP_REL.getUids());

            charAdditional = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids());

            notRefinable = termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids());

            temporaryStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT_TEMP_INTERNAL_USE.getUids());

            currentStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
            System.out.println("test");

            descTypeSet = termFactory.newIntSet();
            descTypeSet.add(xhtmlFullySpecifed.getConceptId());
            descTypeSet.add(xhtmlPreferred.getConceptId());
            descTypeSet.add(preferred.getConceptId());
            descTypeSet.add(fullySpecified.getConceptId());
            descTypeSet.add(synonym.getConceptId());
            descTypeSet.add(xhtmlsynonym.getConceptId());

            dupRelTypeSet = termFactory.newIntSet();
            dupRelTypeSet.add(isPotDupRelType.getConceptId());
            dupRelTypeSet.add(isActualDupRel.getConceptId());
            dupRelTypeSet.add(isNotDupRel.getConceptId());

            rootConcept = searchRootDescriptor.getVerifiedConcept();

            switch (finderType) {
            case TermFactory:
                findDupsUsingTermFactory(threshold);
                break;
            case Fuzzy:
                findDupsUsingFuzzy(threshold, htmlReportFile);
                break;
            case Snowball:
                findDupsUsingSnowball(threshold);
                break;
            }

            htmlReportWriter.append("</html>");
            htmlReportWriter.close();
            dwfaDupDataWriter.close();
            notDupSet.removeAll(dupUuidCollectionSet);
            for (Collection<UUID> ids : notDupSet) {
                boolean first = true;
                for (UUID uid : ids) {
                    if (first) {
                        first = false;
                    } else {
                        dwfaNotDupDataWriter.append("\t");
                    }
                    dwfaNotDupDataWriter.append(uid.toString());
                }
                dwfaNotDupDataWriter.append("\n");

            }
            dwfaNotDupDataWriter.close();

        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private void findDupsUsingFuzzy(double threshold, File htmlReportFile) throws MojoExecutionException {

        HashSet<I_DescriptionVersioned> potDupSet = new HashSet<I_DescriptionVersioned>();
        Map<I_DescriptionVersioned, List<String>> reasonMap = new HashMap<I_DescriptionVersioned, List<String>>();
        getLog().info("Using fuzzy search");
        try {
            if (searchRootDescriptor == null) {
                throw new UnsupportedOperationException("Plugin cannot yet handle a null root specification");
            }
            IndexSearcher luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
            I_GetConceptData rootConcept = searchRootDescriptor.getVerifiedConcept();
            I_IntSet allowedStatus = termFactory.getActiveAceFrameConfig().getAllowedStatus();
            I_IntSet allowedRelTypes = termFactory.getActiveAceFrameConfig().getDestRelTypes();
            Set<I_Position> positions = termFactory.getActiveAceFrameConfig().getViewPositionSet();

            setupTable();

            // searches in children of a the root concept
            Set<I_GetConceptData> rootChildren = rootConcept.getDestRelOrigins(allowedStatus, allowedRelTypes,
                positions, false);

            for (I_GetConceptData rootChild : rootChildren) {
                // print out rootChild description here...
                boolean needToWriteRootChild = true;
                boolean needToWriteDetailHtml = false;
                boolean rootChildStatusNotChanged = true;
                notDupSet.add(rootChild.getUids());

                // getLog().info("Checking for dups on: " +
                // rootChild.toString());

                I_IntSet allowedTypes = descTypeSet;
                for (I_DescriptionTuple childDesc : rootChild.getDescriptionTuples(allowedStatus, allowedTypes,
                    positions)) {
                    BooleanQuery apiQuery = new BooleanQuery();

                    for (String word : StringToWord.get(childDesc.getText())) {
                        if (word.length() > 2) {
                            if ((word.equals("AU") == false) && (word.equals("substance") == false)
                                && (word.equals("Substance") == false)) {
                                FuzzyQuery wq = new FuzzyQuery(new Term("desc", word));
                                apiQuery.add(wq, BooleanClause.Occur.MUST);
                            }
                        }
                    }

                    I_GetConceptData queryDescType = termFactory.getConcept(childDesc.getTypeId());

                    Hits hits = luceneSearcher.search(apiQuery);
                    // getLog().info("query is: " + apiQuery);

                    for (int i = 0; i < hits.length(); i++) {
                        Document d = hits.doc(i);
                        float score = hits.score(i);
                        if (score < threshold) {
                            break;
                        }

                        int cnid = Integer.parseInt(d.get("cnid"));
                        if (rootChild.getConceptId() == cnid) {
                            // ignore because it is a match of the current
                            // rootChild
                        } else {
                            // test if actual dup or pot dup or not a dup
                            // relationship exists...
                            I_GetConceptData hitConcept = termFactory.getConcept(cnid);
                            boolean doNotProcess = false;
                            if (seeIfDupRelExists(rootChild.getSourceRels(), hitConcept)
                                || seeIfDupRelExists(rootChild.getDestRels(), hitConcept)
                                || seeIfDupRelExists(rootChild.getUncommittedSourceRels(), hitConcept)
                                || seeIfDupRelExists(hitConcept.getUncommittedSourceRels(), rootChild)) {
                                doNotProcess = true;
                                break;
                            }

                            if (onlyMatchInRoot && doNotProcess == false) {
                                allowedStatus.add(currentStatus.getConceptId());
                                I_IntSet allowedIsaTypes = new IntSet();
                                allowedIsaTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
                                boolean addUncommitted = false;
                                if (rootConcept.isParentOf(hitConcept, allowedStatus, allowedIsaTypes, positions,
                                    addUncommitted)) {
                                    ;
                                } else {
                                    doNotProcess = true;
                                }
                            }

                            if (doNotProcess == false) {
                                int dnid = Integer.parseInt(d.get("dnid"));
                                I_DescriptionVersioned potDup = termFactory.getDescription(dnid, cnid);

                                if (descTypeSet.contains(potDup.getFirstTuple().getTypeId())) {

                                    I_GetConceptData potDupConcept = termFactory.getConcept(potDup.getConceptId());
                                    if (rootChildStatusNotChanged) {
                                        rootChildStatusNotChanged = false;
                                        setStatusToCurrentUnreviewed(allowedStatus, positions, rootChild);
                                    }
                                    setStatusToCurrentUnreviewed(allowedStatus, positions, potDupConcept);

                                    // create new relationship w/ relationship
                                    // type is potential
                                    // duplicate and the concept status is
                                    // flagged as Current

                                    termFactory.newRelationship(UUID.randomUUID(), rootChild, isPotDupRelType,
                                        potDupConcept, charAdditional, notRefinable, temporaryStatus, 0,
                                        termFactory.getActiveAceFrameConfig());

                                    /*
                                     * //integer.max-value is uncommitted
                                     * Set<I_Position> positionsForEdit = new
                                     * HashSet<I_Position>();
                                     * 
                                     * for (I_Path editPath:
                                     * termFactory.getActiveAceFrameConfig
                                     * ().getEditingPathSet()) {
                                     * positionsForEdit.add(
                                     * LocalVersionedTerminology
                                     * .get().newPosition(editPath,
                                     * Integer.MAX_VALUE));
                                     * }
                                     * //change concept status to be flagged as
                                     * potential duplicate
                                     * Set<I_ConceptAttributePart> partsToAdd =
                                     * new HashSet<I_ConceptAttributePart>();
                                     * for (I_Path editPath:
                                     * termFactory.getActiveAceFrameConfig
                                     * ().getEditingPathSet()) {
                                     * List<I_ConceptAttributeTuple> tuples =
                                     * rootChild.getConceptAttributeTuples(
                                     * null, positionsForEdit);
                                     * for (I_ConceptAttributeTuple t: tuples) {
                                     * if (t.getConceptStatus() !=
                                     * flaggedPotDup.getConceptId()) {
                                     * I_ConceptAttributePart newPart =
                                     * t.duplicatePart();
                                     * 
                                     * newPart.setPathId(editPath.getConceptId())
                                     * ;
                                     * newPart.setVersion(Integer.MAX_VALUE);
                                     * newPart.setConceptStatus(flaggedPotDup.
                                     * getConceptId());
                                     * partsToAdd.add(newPart);
                                     * }
                                     * }
                                     * }
                                     * for (I_ConceptAttributePart p:
                                     * partsToAdd) {
                                     * 
                                     * rootChild.getConceptAttributes().addVersion
                                     * (p);
                                     * }
                                     */
                                    LocalVersionedTerminology.get().addUncommitted(rootChild);
                                    LocalVersionedTerminology.get().addUncommitted(potDupConcept);

                                    // writing search UUIDs to file for
                                    // assignment
                                    if (needToWriteRootChild) {
                                        boolean first = true;
                                        dupUuidCollectionSet.add(rootChild.getUids());
                                        for (UUID uid : rootChild.getUids()) {
                                            if (first) {
                                                first = false;
                                            } else {
                                                dwfaDupDataWriter.append("\t");
                                            }
                                            dwfaDupDataWriter.append(uid.toString());
                                        }
                                        dwfaDupDataWriter.append("\n");
                                        writeSearchConceptToHtmlTableRow(rootChild);
                                        needToWriteRootChild = false;
                                        needToWriteDetailHtml = true;
                                        dwfaDupDataWriter.flush();
                                    }

                                    potDupSet.add(potDup);
                                    dupUuidCollectionSet.add(LocalVersionedTerminology.get().getUids(
                                        potDup.getConceptId()));
                                    if (reasonMap.containsKey(potDup) == false) {
                                        reasonMap.put(potDup, new ArrayList<String>());
                                    }
                                    StringBuffer reasonBuf = new StringBuffer();
                                    reasonBuf.append(childDesc.getText() + " (" + queryDescType + ")         score: "
                                        + score);

                                    if (explanationRequested) {
                                        reasonBuf.append("<br>");
                                        reasonBuf.append(luceneSearcher.explain(apiQuery, hits.id(i)));
                                        reasonBuf.append("<br>");
                                    }
                                    reasonMap.get(potDup).add(reasonBuf.toString());

                                }
                            }
                        }
                    }
                }
                if (needToWriteDetailHtml) {
                    for (I_DescriptionVersioned desc : potDupSet) {
                        writePotDupRowToHtmlTable(reasonMap.get(desc), desc);
                    }
                    writeToDetailHTMLFile(potDupSet, rootChild, reasonMap, htmlReportFile);
                }
                potDupSet.clear();

            }
            endHtmlTable();
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private List<I_ConceptAttributeTuple> setStatusToCurrentUnreviewed(I_IntSet allowedStatus,
            Set<I_Position> positions, I_GetConceptData potDupConcept) throws IOException, TerminologyException {
        List<I_ConceptAttributeTuple> attributeTuples = potDupConcept.getConceptAttributeTuples(allowedStatus,
            positions);
        for (I_Position pos : positions) {
            I_ConceptAttributePart attributePart = attributeTuples.get(0).duplicatePart();
            attributePart.setConceptStatus(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.localize().getNid());
            attributePart.setVersion(Integer.MAX_VALUE);
            attributePart.setPathId(pos.getPath().getConceptId());
            potDupConcept.getConceptAttributes().addVersion(attributePart);
        }
        return attributeTuples;
    }

    private boolean seeIfDupRelExists(List<? extends I_RelVersioned> list, I_GetConceptData destConcept) {

        for (I_RelVersioned rel : list) {
            for (I_RelTuple rt : rel.getTuples()) {
                if ((rt.getC2Id() == destConcept.getConceptId()) || (rt.getC1Id() == destConcept.getConceptId())) {
                    if (dupRelTypeSet.contains(rt.getRelTypeId())) {
                        // Already done...
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void writeToDetailHTMLFile(HashSet<I_DescriptionVersioned> potDupSet, I_GetConceptData rootChild,
            Map<I_DescriptionVersioned, List<String>> reasonMap, File htmlReportFile) throws IOException,
            TerminologyException {

        File detailHtmlFile = new File(detailsDir, rootChild.getUids().get(0) + ".html");
        FileWriter detailWriter = new FileWriter(detailHtmlFile);
        BufferedWriter htmlReportDetailWriter = new BufferedWriter(detailWriter);
        htmlReportDetailWriter.append("<html>");

        htmlReportDetailWriter.append("<table border=1 cellspacing=0>");
        htmlReportDetailWriter.append("<tr>");
        htmlReportDetailWriter.append("<th align=center width=300>Concept Description</th>");
        htmlReportDetailWriter.append("<th bgcolor=#99CCCC align=center width=300>Potential Match Concept(s) </th>");
        htmlReportDetailWriter.append("<th bgcolor=#99CCCC align=center width=50>Concept ID </th>");
        htmlReportDetailWriter.append("<th bgcolor=#99CCCC align=center width=50>Description ID </th>");

        htmlReportDetailWriter.append("<th align=center width=400>Potential Dup Reason </th>");
        // write search concept
        htmlReportDetailWriter.append("<tr>");
        htmlReportDetailWriter.append("<td colspan=2 >");
        htmlReportDetailWriter.append(rootChild.toString() + "&nbsp;&nbsp;&nbsp;&nbsp; ");
        // htmlReportDetailWriter.append("<a href=\"" +".." + File.separator +
        // htmlReportFile.getName() +"\">All Pot Dups</a>");
        htmlReportDetailWriter.append("</td>\n");
        htmlReportDetailWriter.append("<td align=right>");
        htmlReportDetailWriter.append(Integer.toString(rootChild.getConceptId()));

        htmlReportDetailWriter.append("</td>\n");
        htmlReportDetailWriter.append("</tr>\n");

        for (I_DescriptionVersioned desc : potDupSet) {
            // write potential duplicates
            htmlReportDetailWriter.append("<tr >");
            htmlReportDetailWriter.append("<td bgcolor=#99CCCC colspan=2 align=right width=40%>");
            htmlReportDetailWriter.append(desc.getFirstTuple().getText());
            I_GetConceptData potDupType = termFactory.getConcept(desc.getFirstTuple().getTypeId());
            htmlReportDetailWriter.append(" --" + potDupType + "--");
            htmlReportDetailWriter.append("</td>");

            htmlReportDetailWriter.append("<td bgcolor=#99CCCC align=right>");
            htmlReportDetailWriter.append(Integer.toString(desc.getConceptId()));

            htmlReportDetailWriter.append("</td>");

            htmlReportDetailWriter.append("<td bgcolor=#99CCCC align=right>");
            htmlReportDetailWriter.append(Integer.toString(desc.getDescId()));
            htmlReportDetailWriter.append("</td>");

            htmlReportDetailWriter.append("<td width=40%>");
            for (String reason : reasonMap.get(desc)) {
                htmlReportDetailWriter.append(reason + "<br>");
            }
            htmlReportDetailWriter.append("</td>");
        }

        htmlReportDetailWriter.append("</tr>\n");
        htmlReportDetailWriter.append("</tr>");

        htmlReportDetailWriter.append("</table>");
        htmlReportDetailWriter.append("</html>");
        htmlReportDetailWriter.close();

    }

    private void writePotDupRowToHtmlTable(List<String> reasonList, I_DescriptionVersioned desc) throws IOException,
            TerminologyException {
        htmlReportWriter.append("<tr >");
        htmlReportWriter.append("<td bgcolor=#99CCCC colspan=2 align=right width=40%>");
        htmlReportWriter.append(desc.getFirstTuple().getText());
        I_GetConceptData potDupType = termFactory.getConcept(desc.getFirstTuple().getTypeId());
        htmlReportWriter.append(" --" + potDupType + "--");
        htmlReportWriter.append("</td>");

        htmlReportWriter.append("<td bgcolor=#99CCCC align=right>");
        htmlReportWriter.append(Integer.toString(desc.getConceptId()));

        htmlReportWriter.append("</td>");
        htmlReportWriter.append("<td bgcolor=#99CCCC align=right>");

        htmlReportWriter.append(Integer.toString(desc.getDescId()));
        htmlReportWriter.append("</td>");

        htmlReportWriter.append("<td width=40%>");
        for (String reason : reasonList) {
            htmlReportWriter.append(reason + "<br>");
        }
        htmlReportWriter.append("</td>");

        htmlReportWriter.append("</tr>\n");
        htmlReportWriter.flush();

    }

    private void endHtmlTable() throws IOException {
        htmlReportWriter.append("</table>");
    }

    private File writeSearchConceptToHtmlTableRow(I_GetConceptData rootChild) throws IOException {
        // insert new table row with search description
        htmlReportWriter.append("<tr>");
        htmlReportWriter.append("<td colspan=2 >");
        htmlReportWriter.append(rootChild.toString() + "&nbsp;&nbsp;&nbsp;&nbsp; ");
        File detailHtmlFile = new File(detailsDir, rootChild.getUids().get(0).toString() + ".html");
        htmlReportWriter.append("<a href=\"" + detailsDir.getName() + File.separator + detailHtmlFile.getName()
            + "\">Potential Dup Details</a>");
        // getLog().info("href: " + rootDir.getName() + File.separator +
        // detailHtmlFile.getName() );
        htmlReportWriter.append("</td>\n");
        htmlReportWriter.append("<td align=right>");
        htmlReportWriter.append(Integer.toString(rootChild.getConceptId()));
        htmlReportWriter.append("</td>\n");
        htmlReportWriter.append("</tr>\n");
        return detailHtmlFile;

    }

    private void setupTable() throws IOException {
        htmlReportWriter.append("<table border=1 cellspacing=0>");
        htmlReportWriter.append("<tr>");
        htmlReportWriter.append("<th align=center width=300>Concept Description</th>");
        htmlReportWriter.append("<th bgcolor=#99CCCC align=center width=300>Potential Match Concept(s) </th>");
        htmlReportWriter.append("<th bgcolor=#99CCCC align=center width=50>Concept ID </th>");
        htmlReportWriter.append("<th bgcolor=#99CCCC align=center width=50>Description ID </th>");

        htmlReportWriter.append("<th align=center width=400>Potential Dup Reason </th>");
        htmlReportWriter.append("</tr>");
        htmlReportWriter.append("\n");
    }

    private void findDupsUsingSnowball(double threshold) throws MojoExecutionException {

        HashSet<I_DescriptionVersioned> potDupSet = new HashSet<I_DescriptionVersioned>();
        Map<I_DescriptionVersioned, List<String>> reasonMap = new HashMap<I_DescriptionVersioned, List<String>>();
        getLog().info("Using SNOWBALL search");
        try {
            if (searchRootDescriptor == null) {
                throw new UnsupportedOperationException("Plugin cannot yet handle a null root specification");
            }
            IndexSearcher luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
            I_GetConceptData rootConcept = searchRootDescriptor.getVerifiedConcept(); //
            I_IntSet allowedStatus = null;
            I_IntSet allowedRelTypes = null;
            Set<I_Position> positions = null;

            setupTable();

            // searches in children of a the root concept
            Set<I_GetConceptData> rootChildren = rootConcept.getDestRelOrigins(allowedStatus, allowedRelTypes,
                positions, false);
            for (I_GetConceptData rootChild : rootChildren) {
                // print out rootChild description here...
                // getLog().info("Checking for dups on: " +
                // rootChild.toString());

                boolean needToWriterootChild = true;

                I_IntSet allowedTypes = descTypeSet;
                for (I_DescriptionTuple childDesc : rootChild.getDescriptionTuples(allowedStatus, allowedTypes,
                    positions)) {
                    BooleanQuery apiQuery = new BooleanQuery();

                    for (String word : StringToWord.get(childDesc.getText())) {
                        if (word.length() > 2) {
                            if ((word.equals("AU") == false) && (word.equals("substance") == false)
                                && (word.equals("Substance") == false)) {
                                FuzzyQuery wq = new FuzzyQuery(new Term("desc", word));
                                apiQuery.add(wq, BooleanClause.Occur.MUST);
                            }
                        }
                    }

                    I_GetConceptData queryDescType = termFactory.getConcept(childDesc.getTypeId());

                    Hits hits = luceneSearcher.search(apiQuery);
                    getLog().info("query is: " + apiQuery);

                    for (int i = 0; i < hits.length(); i++) {
                        Document d = hits.doc(i);
                        float score = hits.score(i);
                        if (score < threshold) {
                            break;
                        }

                        int cnid = Integer.parseInt(d.get("cnid"));
                        if (rootChild.getConceptId() == cnid) {
                            // ignore because it is a match of the current
                            // rootChild
                        } else {
                            // do something useful here
                            int dnid = Integer.parseInt(d.get("dnid"));
                            I_DescriptionVersioned potDup = termFactory.getDescription(dnid, cnid);
                            if (potDup.getFirstTuple().getTypeId() == xhtmlFullySpecifed.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == xhtmlPreferred.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == preferred.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == fullySpecified.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == synonym.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == xhtmlsynonym.getConceptId()) {
                                getLog().info(
                                    "dnid: " + dnid + "  potential duplicate: " + potDup.getFirstTuple().getText()
                                        + " score: " + score);

                                if (needToWriterootChild) {
                                    dwfaDupDataWriter.append(rootChild.toString());
                                    dwfaDupDataWriter.append("\n");
                                    writeSearchConceptToHtmlTableRow(rootChild);
                                    needToWriterootChild = false;
                                }

                                potDupSet.add(potDup);
                                if (reasonMap.containsKey(potDup) == false) {
                                    reasonMap.put(potDup, new ArrayList<String>());
                                }
                                StringBuffer reasonBuf = new StringBuffer();
                                reasonBuf.append(childDesc.getText() + " (" + queryDescType + ")         score: "
                                    + score);

                                if (explanationRequested) {
                                    reasonBuf.append("<br>");
                                    reasonBuf.append(luceneSearcher.explain(apiQuery, hits.id(i)));
                                    reasonBuf.append("<br>");
                                }
                                reasonMap.get(potDup).add(reasonBuf.toString());
                            }
                        }

                    }
                }
                for (I_DescriptionVersioned desc : potDupSet) {
                    writePotDupRowToHtmlTable(reasonMap.get(desc), desc);
                }
                potDupSet.clear();

            }
            endHtmlTable();
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    private void findDupsUsingTermFactory(double threshold) throws MojoExecutionException {
        I_TermFactory termFactory = LocalVersionedTerminology.get(); // gives
        // me
        // access
        // to
        // concepts
        // (terminology
        // server
        // object)

        try {
            if (searchRootDescriptor == null) {
                throw new UnsupportedOperationException("Plugin cannot yet handle a null root specification");
            }

            I_IntSet allowedStatus = null;
            I_IntSet allowedRelTypes = null;
            Set<I_Position> positions = null;
            Set<I_GetConceptData> rootChildren = rootConcept.getDestRelOrigins(allowedStatus, allowedRelTypes,
                positions, false);
            for (I_GetConceptData rootChild : rootChildren) {
                // print out rootChild description here...
                // getLog().info("Checking for dups on: " +
                // rootChild.toString());
                I_IntSet allowedTypes = descTypeSet;
                for (I_DescriptionTuple childDesc : rootChild.getDescriptionTuples(allowedStatus, allowedTypes,
                    positions)) {
                    Hits hits = termFactory.doLuceneSearch(childDesc.getText());
                    for (int i = 0; i < hits.length(); i++) {
                        Document d = hits.doc(i);
                        float score = hits.score(i);
                        if (score < threshold) {
                            break;
                        }
                        int cnid = Integer.parseInt(d.get("cnid"));
                        if (rootChild.getConceptId() == cnid) {
                            // ignore because it is a match of the current
                            // rootChild
                        } else {
                            // do something useful here
                            int dnid = Integer.parseInt(d.get("dnid"));
                            // I_GetConceptData potDupConcept =
                            // termFactory.getConcept(cnid);
                            I_DescriptionVersioned potDup = termFactory.getDescription(dnid, cnid);
                            if (potDup.getFirstTuple().getTypeId() == xhtmlFullySpecifed.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == xhtmlPreferred.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == preferred.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == fullySpecified.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == synonym.getConceptId()
                                || potDup.getFirstTuple().getTypeId() == xhtmlsynonym.getConceptId()) {
                                // getLog().info("  potential duplicate: " +
                                // potDup.getFirstTuple()
                                // .getText() + " score: " + score);
                            }
                        }

                    }
                }
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (ParseException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public String getThresholdStr() {
        return thresholdStr;
    }

    public void setThresholdStr(String thresholdStr) {
        this.thresholdStr = thresholdStr;
    }

    public boolean isExplanationRequested() {
        return explanationRequested;
    }

    public void setExplanationRequested(boolean explanationRequested) {
        this.explanationRequested = explanationRequested;
    }

    public String getSearchTypeStr() {
        return searchTypeStr;
    }

    public void setSearchTypeStr(String searchTypeStr) {
        this.searchTypeStr = searchTypeStr;
    }

    public File getRootDir() {
        return detailsDir;
    }

    public void setRootDir(File rootDir) {
        this.detailsDir = rootDir;
    }

    public ConceptDescriptor getSearchRootDescriptor() {
        return searchRootDescriptor;
    }

    public void setSearchRootDescriptor(ConceptDescriptor searchRootDescriptor) {
        this.searchRootDescriptor = searchRootDescriptor;
    }

    public boolean isOnlyMatchInRoot() {
        return onlyMatchInRoot;
    }

    public void setOnlyMatchInRoot(boolean onlyMatchInRoot) {
        this.onlyMatchInRoot = onlyMatchInRoot;
    }

}
