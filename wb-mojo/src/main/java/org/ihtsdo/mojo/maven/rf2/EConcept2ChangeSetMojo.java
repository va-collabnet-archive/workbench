/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.rf2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 *
 * @author Marc Campbell
 *
 * @goal econcepts-to-eccs
 *
 * @phase process-resources
 */
public class EConcept2ChangeSetMojo extends AbstractMojo {

    private static final String LINE_TERMINATOR = "\n"; // \n == $0A

    AtomicInteger conceptsRead = new AtomicInteger();
    AtomicInteger conceptsProcessed = new AtomicInteger();

    ConcurrentSkipListSet<Object> watchSet = new ConcurrentSkipListSet<>();

    ArrayList<Long> dateBoundaryList;
    ArrayList<DataOutputStream> eccsOutput;

    /**
     * concepts file names.
     *
     * @parameter default-value="sctSiEConcepts.jbin"
     */
    private String econceptsFileName;

    /**
     * Generated resources directory.
     *
     * @parameter expression="${project.build.directory}/generated-resources"
     */
    private String generatedResources;

    /**
     * The changeset output directory
     *
     * @parameter default-value=
     * "${project.build.directory}/${project.build.finalName}/sct_changesets"
     */
    File changeSetDir;

    /**
     * Watch concepts that will be printed to log when encountered.
     *
     * @parameter
     */
    private String[] dateBoundaryStrings;

    /**
     * Watch concepts that will be printed to log when encountered.
     *
     * @parameter
     */
    private String[] watchConceptUuids;

    /**
     * The debug file output directory
     *
     * @parameter default-value= "${project.build.directory}"
     */
    File debugFileDir;
    BufferedWriter debugWriter;
    public static boolean debugWriteFileEnabled = false;
    public static long debugDumpCount;
    public static final long debugDumpLimit = 200;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        StringBuilder sb = new StringBuilder();
        sb.append("\nEConcept2ChangeSetMojo begins.. ");
        sb.append("\n  econceptsFileName  = ");
        sb.append(econceptsFileName);
        sb.append("\n  generatedResources = ");
        sb.append(generatedResources);
        sb.append("\n  changeSetDir       = ");
        sb.append(changeSetDir.toString());
        getLog().info(sb.toString());

        try {
            if (debugWriteFileEnabled) {
                debugDumpCount = 0;
                String s = debugFileDir.toString() + File.separator + "debug.txt";
                FileWriter fw = new FileWriter(s);
                debugWriter = new BufferedWriter(fw);
            }

            executeMojo(econceptsFileName, generatedResources, changeSetDir);
        } catch (FileNotFoundException ex) {
            throw new MojoExecutionException("econcepts in file error", ex);
        } catch (IOException | ClassNotFoundException ex) {
            throw new MojoExecutionException("error constructing econcept", ex);
        }
    }

    void executeMojo(String econceptsFileName, String generatedResources,
            File changeSetDir)
            throws MojoExecutionException, FileNotFoundException, IOException, ClassNotFoundException {
        if (watchConceptUuids != null) {
            for (String uuidStr : watchConceptUuids) {
                watchSet.add(UUID.fromString(uuidStr));
            }
        }

        // setup date ranges
        dateBoundaryList = new ArrayList<>();
        eccsOutput = new ArrayList<>();
        changeSetDir.mkdirs();
        try {
            StringBuilder sb = new StringBuilder();
            for (String dateStr : dateBoundaryStrings) {
                // convert date string
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                Date date = (Date) formatter.parse(dateStr);
                dateBoundaryList.add(date.getTime()); // milliseconds
                // open output files 
                String eccsOutputFileStr = changeSetDir.toString() + File.separator + "sctEccs_" + dateStr + ".eccs";
                File eccsOutputFile = new File(eccsOutputFileStr);
                FileOutputStream fos = new FileOutputStream(eccsOutputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                DataOutputStream out = new DataOutputStream(bos);
                eccsOutput.add(out);
                sb.append("\n  eccsOutputFileStr").append(eccsOutputFileStr);
            }
            getLog().info(sb.toString());

        } catch (ParseException ex) {
            throw new MojoExecutionException("date string conversion error", ex);
        }

        File conceptsFile = new File(generatedResources, econceptsFileName);
        getLog().info("Starting econcept load from: " + conceptsFile.getAbsolutePath());
        FileInputStream fis = new FileInputStream(conceptsFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        DataInputStream in = new DataInputStream(bis);

        try {
            while (true) {
                EConcept eConcept = new EConcept(in);
                int read = conceptsRead.incrementAndGet();

                if (read % 100 == 0) {
                    if (read % 8000 == 0) {
                        System.out.println('.');
                        System.out.print(read + "-");
                    } else {
                        System.out.print('.');
                    }
                }

                HashMap<Long, EConcept> eccsList = processEconcept2ChangeSet(eConcept);
                writeEccsList(eccsList);
                if (debugWriteFileEnabled && (debugDumpCount < debugDumpLimit)) {
                    debugWriter.append("\n#######\n");
                    debugWriter.append(eConcept.toString());
                    writeEccsListDebugDump(eccsList);
                    debugDumpCount++;
                }
            }
        } catch (EOFException e) {
            System.out.print("\n\n");
            in.close(); // normal end of file
        }

        for (DataOutputStream dos : eccsOutput) {
            dos.flush();
            dos.close();
        }
        if (debugWriteFileEnabled) {
            debugWriter.flush();
            debugWriter.close();
        }

    }

    private int getDateIndex(Long date) {
        int idx = 0;
        while (idx < dateBoundaryList.size()) {
            if (date <= dateBoundaryList.get(idx)) {
                return idx;
            }
            idx++;
        }
        if (idx >= dateBoundaryList.size()) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return idx;
    }

    private HashMap<Long, EConcept> processEconcept2ChangeSet(EConcept eConcept) {
        EConcept ec;
        HashMap<Long, EConcept> eccsMap = new HashMap<>();

        // ATTRIBUTES
        TkConceptAttributes attr = eConcept.conceptAttributes;
        ArrayList<TkIdentifier> caids = new ArrayList<>();
        if (attr.additionalIds != null) {
            caids.addAll(attr.additionalIds);
        }
        List<TkRefexAbstractMember<?>> cannotas = new ArrayList<>();
        if (attr.annotations != null) {
            cannotas.addAll(attr.annotations);
        }
        ec = makeEconcept(eConcept);
        ec.conceptAttributes = makeAttr(attr, caids, cannotas);
        eccsMap.put(attr.time, ec);
        if (attr.revisions != null) {
            for (TkConceptAttributesRevision attrRev : attr.revisions) {
                ec = makeEconcept(eConcept);
                ec.conceptAttributes = makeAttr(attr, attrRev, caids, cannotas);
                eccsMap.put(attrRev.time, ec);
            }
        }

        // DESCRIPTIONS
        if (eConcept.descriptions != null) {
            for (TkDescription tkd : eConcept.descriptions) {
                // additional ids
                ArrayList<TkIdentifier> ids = new ArrayList<>();
                if (tkd.additionalIds != null) {
                    ids.addAll(tkd.additionalIds);
                }
                // annotations
                ArrayList<TkRefexAbstractMember<?>> notas = new ArrayList<>();
                if (tkd.annotations != null) {
                    notas.addAll(tkd.annotations);
                }
                //
                ec = eccsMap.get(tkd.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                    eccsMap.put(tkd.time, ec);
                }
                if (ec.descriptions == null) {
                    ec.descriptions = new ArrayList<>();
                }
                ec.descriptions.add(makeDescr(tkd, ids, notas));
                if (tkd.revisions != null) {
                    for (TkDescriptionRevision tkdRev : tkd.revisions) {
                        ec = eccsMap.get(tkdRev.time);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                            eccsMap.put(tkdRev.time, ec);
                        }
                        if (ec.descriptions == null) {
                            ec.descriptions = new ArrayList<>();
                        }
                        ec.descriptions.add(makeDescr(tkd, tkdRev, ids, notas));
                    }
                }
                if (!ids.isEmpty() || !notas.isEmpty()) {
                    throw new UnsupportedOperationException("DESA. Not supported yet.");
                }
            }
        }

        // RELATIONSHIPS
        if (eConcept.relationships != null) {
            for (TkRelationship tkr : eConcept.relationships) {
                // additional ids
                ArrayList<TkIdentifier> ids = new ArrayList<>();
                if (tkr.additionalIds != null) {
                    ids.addAll(tkr.additionalIds);
                }
                // annotations
                ArrayList<TkRefexAbstractMember<?>> notas = new ArrayList<>();
                if (tkr.annotations != null) {
                    notas.addAll(tkr.annotations);
                }
                //
                ec = eccsMap.get(tkr.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                    eccsMap.put(tkr.time, ec);
                }
                if (ec.relationships == null) {
                    ec.relationships = new ArrayList<>();
                }
                ec.relationships.add(makeRel(tkr, ids, notas));
                if (tkr.revisions != null) {
                    for (TkRelationshipRevision tkrRev : tkr.revisions) {
                        ec = eccsMap.get(tkrRev.time);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                            eccsMap.put(tkrRev.time, ec);
                        }
                        if (ec.relationships == null) {
                            ec.relationships = new ArrayList<>();
                        }
                        ec.relationships.add(makeRel(tkr, tkrRev, ids, notas));
                    }
                }
                if (!ids.isEmpty() || !notas.isEmpty()) {
                    throw new UnsupportedOperationException("RELA. Not supported yet.");
                }
            }
        }

        // REFSET MEMBERS
        if (eConcept.refsetMembers != null) {
            for (TkRefexAbstractMember<?> tkram : eConcept.refsetMembers) {
                // additional ids
                ArrayList<TkIdentifier> ids = new ArrayList<>();
                if (tkram.additionalIds != null) {
                    ids.addAll(tkram.additionalIds);
                }
                // annotations
                ArrayList<TkRefexAbstractMember<?>> notas = new ArrayList<>();
                if (tkram.annotations != null) {
                    notas.addAll(tkram.annotations);
                }
                //
                ec = eccsMap.get(tkram.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                    eccsMap.put(tkram.time, ec);
                }
                if (ec.refsetMembers == null) {
                    ec.refsetMembers = new ArrayList<>();
                }
                ec.refsetMembers.add(makeMemb(tkram, ids, notas));
                List<?> tkramrev = tkram.revisions;
                if (tkram.revisions != null) {
                    for (Object o : tkramrev) {
                        long oTime;
                        if (TkRefexUuidRevision.class.isAssignableFrom(o.getClass())) {
                            oTime = ((TkRefexUuidRevision) o).time;
                        } else if (TkRefsetStrRevision.class.isAssignableFrom(o.getClass())) {
                            oTime = ((TkRefsetStrRevision) o).time;
                        } else {
                            throw new UnsupportedOperationException("Member Revision Type Not supported yet.");
                        }
                        ec = eccsMap.get(oTime);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                            eccsMap.put(oTime, ec);
                        }
                        if (ec.refsetMembers == null) {
                            ec.refsetMembers = new ArrayList<>();
                        }
                        ec.refsetMembers.add(makeMemb(tkram, o, ids, notas));
                    }
                }
            }
        }

        // MEDIA
        if (eConcept.media != null) {
            for (TkMedia tkm : eConcept.media) {
                if (tkm.additionalIds != null || tkm.annotations != null) {
                    throw new UnsupportedOperationException("MEDIA A. Not supported yet.");
                }
                ec = eccsMap.get(tkm.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                    eccsMap.put(tkm.time, ec);
                }
                if (ec.media == null) {
                    ec.media = new ArrayList<>();
                }
                ec.media.add(makeMedia(tkm));
                if (tkm.revisions != null) {
                    for (TkMediaRevision tkmRev : tkm.revisions) {
                        ec = eccsMap.get(tkmRev.time);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                            eccsMap.put(tkmRev.time, ec);
                        }
                        if (ec.media == null) {
                            ec.media = new ArrayList<>();
                        }
                        ec.media.add(makeMedia(tkm, tkmRev));
                    }
                }

            }
        }

        return eccsMap;
    }

    private void writeEccsList(HashMap<Long, EConcept> eccsMap)
            throws IOException {
        Long[] eccsDateKeys = eccsMap.keySet().toArray(new Long[eccsMap.size()]);

        // sort keys to be in date order
        Arrays.sort(eccsDateKeys);

        // 
        for (Long eccsDate : eccsDateKeys) {
            int idx = getDateIndex(eccsDate);
            eccsOutput.get(idx).writeLong(eccsDate);
            EConcept ec = eccsMap.get(eccsDate);
            if (ec == null) {
                throw new UnsupportedOperationException("Concept not in map. Not supported yet.");
            } else {
                DataOutputStream dos = eccsOutput.get(idx);
                if (dos == null) {
                    throw new UnsupportedOperationException("NULL DOS. Not supported yet.");
                } else {
                    ec.writeExternal(dos);
                }
            }
        }
    }

    private void writeEccsListDebugDump(HashMap<Long, EConcept> eccsMap)
            throws IOException {
        Long[] eccsDateKeys = eccsMap.keySet().toArray(new Long[eccsMap.size()]);

        // sort keys to be in date order
        Arrays.sort(eccsDateKeys);

        // 
        for (Long eccsDateMillis : eccsDateKeys) {
            int idx = getDateIndex(eccsDateMillis);

            Date dt = new Date(eccsDateMillis);
            String s = "yyyy-MM-dd HH:mm:ss";
            DateFormat fmt = new SimpleDateFormat(s);
            String s2 = fmt.format(dt);  // formatted oTime string

            debugWriter.append("\n---- ");
            debugWriter.append(String.valueOf(idx));
            debugWriter.append(" ---- ");
            debugWriter.append(eccsDateMillis.toString());
            debugWriter.append(" ---- ");
            debugWriter.append(s2);
            debugWriter.append(" ----\n");

            EConcept ec = eccsMap.get(eccsDateMillis);
            if (ec == null) {
                throw new UnsupportedOperationException("Concept_not_in_map. Not supported yet.");
            } else {
                debugWriter.append(ec.toString());
            }
        }
    }

    private void writeEccsListDebugString(HashMap<Long, EConcept> eccsMap)
            throws IOException {
        Long[] eccsDateKeys = eccsMap.keySet().toArray(new Long[eccsMap.size()]);

        // sort keys to be in date order
        Arrays.sort(eccsDateKeys);

        // 
        for (Long eccsDateMillis : eccsDateKeys) {
            int idx = getDateIndex(eccsDateMillis);

            Date dt = new Date(eccsDateMillis);
            String s = "yyyy-MM-dd HH:mm:ss";
            DateFormat fmt = new SimpleDateFormat(s);
            String s2 = fmt.format(dt);  // formatted oTime string

            System.out.print("\n---- ");
            System.out.print(String.valueOf(idx));
            System.out.print(" ---- ");
            System.out.print(eccsDateMillis.toString());
            System.out.print(" ---- ");
            System.out.print(s2);
            System.out.print(" ----\n");

            EConcept ec = eccsMap.get(eccsDateMillis);
            if (ec == null) {
                System.out.println(":DEBUG: concept not in map");
            } else {
                System.out.print(ec.toString());
            }
        }
    }

    private EConcept makeEconcept(EConcept ec) {
        EConcept ecTmp = new EConcept();
        ecTmp.primordialUuid = ec.primordialUuid;

        ecTmp.annotationIndexStyleRefex = ec.annotationIndexStyleRefex;
        ecTmp.annotationStyleRefex = ec.annotationStyleRefex;
        ecTmp.conceptAttributes = null;
        ecTmp.descriptions = null;
        ecTmp.relationships = null;
        ecTmp.refsetMembers = null;
        ecTmp.media = null;

        return ecTmp;
    }

    private TkConceptAttributes makeAttr(TkConceptAttributes attr,
            List<TkIdentifier> caids,
            List<TkRefexAbstractMember<?>> cannotas) {
        TkConceptAttributes attrTmp = new TkConceptAttributes();
        attrTmp.primordialUuid = attr.primordialUuid;

        attrTmp.additionalIds = null;
        for (TkIdentifier tki : caids) {
            if (tki.time == attr.time) {
                if (attrTmp.additionalIds == null) {
                    attrTmp.additionalIds = new ArrayList<>();
                }
                attrTmp.additionalIds.add(tki);
            }
        }
        caids.removeAll(attrTmp.additionalIds);

        attrTmp.annotations = null;
        for (TkRefexAbstractMember<?> tkram : cannotas) {
            if (tkram.time == attr.time) {
                if (attrTmp.annotations == null) {
                    attrTmp.annotations = new ArrayList<>();
                }
                attrTmp.annotations.add(tkram);
            }
        }
        cannotas.removeAll(attrTmp.annotations);

        attrTmp.revisions = null;

        attrTmp.defined = attr.defined;
        attrTmp.statusUuid = attr.statusUuid;
        attrTmp.time = attr.time;
        attrTmp.authorUuid = attr.authorUuid;
        attrTmp.moduleUuid = attr.moduleUuid;
        attrTmp.pathUuid = attr.pathUuid;

        return attrTmp;
    }

    private TkConceptAttributes makeAttr(TkConceptAttributes attr,
            TkConceptAttributesRevision attrRev,
            ArrayList<TkIdentifier> caids,
            List<TkRefexAbstractMember<?>> cannotas) {
        TkConceptAttributes attrTmp = new TkConceptAttributes();
        attrTmp.primordialUuid = attr.primordialUuid;

        attrTmp.additionalIds = null;
        for (TkIdentifier tki : caids) {
            if (tki.time == attrRev.time) {
                if (attrTmp.additionalIds == null) {
                    attrTmp.additionalIds = new ArrayList<>();
                }
                attrTmp.additionalIds.add(tki);
            }
        }
        caids.removeAll(attrTmp.additionalIds);

        attrTmp.annotations = null;
        for (TkRefexAbstractMember<?> tkram : cannotas) {
            if (tkram.time == attrRev.time) {
                if (attrTmp.annotations == null) {
                    attrTmp.annotations = new ArrayList<>();
                }
                attrTmp.annotations.add(tkram);
            }
        }
        cannotas.removeAll(attrTmp.annotations);

        attrTmp.revisions = null;

        attrTmp.defined = attrRev.defined;
        attrTmp.statusUuid = attrRev.statusUuid;
        attrTmp.time = attrRev.time;
        attrTmp.authorUuid = attrRev.authorUuid;
        attrTmp.moduleUuid = attrRev.moduleUuid;
        attrTmp.pathUuid = attrRev.pathUuid;

        return attrTmp;
    }

    private TkDescription makeDescr(TkDescription tkd,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {
        TkDescription tmpDesc = new TkDescription();
        tmpDesc.primordialUuid = tkd.primordialUuid;

        tmpDesc.additionalIds = null;
        for (TkIdentifier tki : ids) {
            if (tki.time == tkd.time) {
                if (tmpDesc.additionalIds == null) {
                    tmpDesc.additionalIds = new ArrayList<>();
                }
                tmpDesc.additionalIds.add(tki);
            }
        }
        ids.removeAll(tmpDesc.additionalIds);

        tmpDesc.annotations = null;
        for (TkRefexAbstractMember<?> tkram : notas) {
            if (tkram.time == tkd.time) {
                if (tmpDesc.annotations == null) {
                    tmpDesc.annotations = new ArrayList<>();
                }
                tmpDesc.annotations.add(tkram);
            }
        }
        notas.removeAll(tmpDesc.annotations);

        tmpDesc.revisions = null;

        tmpDesc.authorUuid = tkd.authorUuid;
        tmpDesc.conceptUuid = tkd.conceptUuid;
        tmpDesc.initialCaseSignificant = tkd.initialCaseSignificant;
        tmpDesc.lang = tkd.lang;
        tmpDesc.moduleUuid = tkd.moduleUuid;
        tmpDesc.pathUuid = tkd.pathUuid;
        tmpDesc.statusUuid = tkd.statusUuid;
        tmpDesc.text = tkd.text;
        tmpDesc.time = tkd.time;
        tmpDesc.typeUuid = tkd.typeUuid;

        return tmpDesc;
    }

    private TkDescription makeDescr(TkDescription tkd,
            TkDescriptionRevision tkdRev,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {
        TkDescription tmpDesc = new TkDescription();
        tmpDesc.primordialUuid = tkd.primordialUuid;

        tmpDesc.additionalIds = null;
        for (TkIdentifier tki : ids) {
            if (tki.time == tkdRev.time) {
                if (tmpDesc.additionalIds == null) {
                    tmpDesc.additionalIds = new ArrayList<>();
                }
                tmpDesc.additionalIds.add(tki);
            }
        }
        ids.removeAll(tmpDesc.additionalIds);

        tmpDesc.annotations = null;
        for (TkRefexAbstractMember<?> tkram : notas) {
            if (tkram.time == tkdRev.time) {
                if (tmpDesc.annotations == null) {
                    tmpDesc.annotations = new ArrayList<>();
                }
                tmpDesc.annotations.add(tkram);
            }
        }
        notas.removeAll(tmpDesc.annotations);

        tmpDesc.revisions = null;

        tmpDesc.authorUuid = tkdRev.authorUuid;
        tmpDesc.conceptUuid = tkd.conceptUuid; // immutable
        tmpDesc.initialCaseSignificant = tkdRev.initialCaseSignificant;
        tmpDesc.lang = tkdRev.lang;
        tmpDesc.moduleUuid = tkdRev.moduleUuid;
        tmpDesc.pathUuid = tkdRev.pathUuid;
        tmpDesc.statusUuid = tkdRev.statusUuid;
        tmpDesc.text = tkdRev.text;
        tmpDesc.time = tkdRev.time;
        tmpDesc.typeUuid = tkdRev.typeUuid;

        return tmpDesc;
    }

    private TkRelationship makeRel(TkRelationship tkr,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {
        TkRelationship tmpRel = new TkRelationship();
        tmpRel.primordialUuid = tkr.primordialUuid;

        tmpRel.additionalIds = null;
        for (TkIdentifier tki : ids) {
            if (tki.time == tkr.time) {
                if (tmpRel.additionalIds == null) {
                    tmpRel.additionalIds = new ArrayList<>();
                }
                tmpRel.additionalIds.add(tki);
            }
        }
        ids.removeAll(tmpRel.additionalIds);

        tmpRel.annotations = null;
        for (TkRefexAbstractMember<?> tkram : notas) {
            if (tkram.time == tkr.time) {
                if (tmpRel.annotations == null) {
                    tmpRel.annotations = new ArrayList<>();
                }
                tmpRel.annotations.add(tkram);
            }
        }
        notas.removeAll(tmpRel.annotations);

        tmpRel.revisions = null;

        tmpRel.authorUuid = tkr.authorUuid;
        tmpRel.c1Uuid = tkr.c1Uuid;
        tmpRel.c2Uuid = tkr.c2Uuid;
        tmpRel.characteristicUuid = tkr.characteristicUuid;
        tmpRel.moduleUuid = tkr.moduleUuid;
        tmpRel.pathUuid = tkr.pathUuid;
        tmpRel.refinabilityUuid = tkr.refinabilityUuid;
        tmpRel.relGroup = tkr.relGroup;
        tmpRel.statusUuid = tkr.statusUuid;
        tmpRel.time = tkr.time;
        tmpRel.typeUuid = tkr.typeUuid;

        return tmpRel;
    }

    private TkRelationship makeRel(TkRelationship tkr,
            TkRelationshipRevision tkrRev,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {
        TkRelationship tmpRel = new TkRelationship();
        tmpRel.primordialUuid = tkr.primordialUuid;

        tmpRel.additionalIds = null;
        for (TkIdentifier tki : ids) {
            if (tki.time == tkrRev.time) {
                if (tmpRel.additionalIds == null) {
                    tmpRel.additionalIds = new ArrayList<>();
                }
                tmpRel.additionalIds.add(tki);
            }
        }

        tmpRel.annotations = null;
        for (TkRefexAbstractMember<?> tkram : notas) {
            if (tkram.time == tkrRev.time) {
                if (tmpRel.annotations == null) {
                    tmpRel.annotations = new ArrayList<>();
                }
                tmpRel.annotations.add(tkram);
            }
        }
        notas.removeAll(tmpRel.annotations);

        tmpRel.revisions = null;

        tmpRel.authorUuid = tkrRev.authorUuid;
        tmpRel.c1Uuid = tkr.c1Uuid; // immutable
        tmpRel.c2Uuid = tkr.c2Uuid; // immutable
        tmpRel.characteristicUuid = tkrRev.characteristicUuid;
        tmpRel.moduleUuid = tkrRev.moduleUuid;
        tmpRel.pathUuid = tkrRev.pathUuid;
        tmpRel.refinabilityUuid = tkrRev.refinabilityUuid;
        tmpRel.relGroup = tkrRev.group;
        tmpRel.statusUuid = tkrRev.statusUuid;
        tmpRel.time = tkrRev.time;
        tmpRel.typeUuid = tkrRev.typeUuid;

        return tmpRel;
    }

    private TkRefexAbstractMember makeMemb(TkRefexAbstractMember<?> tkram,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {
        TkRefexAbstractMember tmpMemb = null;

        if (TkRefexBooleanMember.class.isAssignableFrom(tkram.getClass())) {
            // BOOLEAN
            // ((TkRefexBooleanMember) tkram).revisions;
            tmpMemb = new TkRefexBooleanMember();
            ((TkRefexBooleanMember) tmpMemb).boolean1 = ((TkRefexBooleanMember) tkram).boolean1;
        } else if (TkRefexUuidMember.class.isAssignableFrom(tkram.getClass())) {
            // CONCEPT ERefsetCidMember()
            tmpMemb = new TkRefexUuidMember();
            ((TkRefexUuidMember) tmpMemb).uuid1 = ((TkRefexUuidMember) tkram).uuid1;
        } else if (TkRefexIntMember.class.isAssignableFrom(tkram.getClass())) {
            // INTEGER
            tmpMemb = new TkRefexIntMember();
            ((TkRefexIntMember) tmpMemb).int1 = ((TkRefexIntMember) tkram).int1;
        } else if (TkRefsetStrMember.class.isAssignableFrom(tkram.getClass())) {
            // STRING
            tmpMemb = new TkRefsetStrMember();
            ((TkRefsetStrMember) tmpMemb).string1 = ((TkRefsetStrMember) tkram).string1;
        } else if (TkRefexUuidFloatMember.class.isAssignableFrom(tkram.getClass())) {
            // C_FLOAT CidFloatMember
            tmpMemb = new TkRefexUuidFloatMember();
            ((TkRefexUuidFloatMember) tmpMemb).float1 = ((TkRefexUuidFloatMember) tkram).float1;
            ((TkRefexUuidFloatMember) tmpMemb).uuid1 = ((TkRefexUuidFloatMember) tkram).uuid1;
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        tmpMemb.primordialUuid = tkram.primordialUuid;

        tmpMemb.additionalIds = null;
        for (TkIdentifier tki : ids) {
            if (tki.time == tkram.time) {
                if (tmpMemb.additionalIds == null) {
                    tmpMemb.additionalIds = new ArrayList<>();
                }
                tmpMemb.additionalIds.add(tki);
            }
        }
        ids.removeAll(tmpMemb.additionalIds);

        tmpMemb.annotations = null;
        for (TkRefexAbstractMember<?> annotation : notas) {
            if (annotation.time == tkram.time) {
                if (tmpMemb.annotations == null) {
                    tmpMemb.annotations = new ArrayList<>();
                }
                tmpMemb.annotations.add(annotation);
            }
        }
        notas.removeAll(tmpMemb.annotations);

        tmpMemb.revisions = null;

        tmpMemb.authorUuid = tkram.authorUuid;
        tmpMemb.componentUuid = tkram.componentUuid;
        tmpMemb.moduleUuid = tkram.moduleUuid;
        tmpMemb.pathUuid = tkram.pathUuid;
        tmpMemb.refsetUuid = tkram.refsetUuid;
        tmpMemb.statusUuid = tkram.statusUuid;
        tmpMemb.time = tkram.time;

        return tmpMemb;
    }

    private TkRefexAbstractMember makeMemb(TkRefexAbstractMember<?> tkram,
            Object o,
            ArrayList<TkIdentifier> ids,
            ArrayList<TkRefexAbstractMember<?>> notas) {

        if (TkRefexBooleanRevision.class.isAssignableFrom(o.getClass())) {
            // BOOLEAN
//            tmpMemb = new TkRefexBooleanMember();
//            ((TkRefexBooleanMember) tmpMemb).boolean1 = ((TkRefexBooleanMember) o).boolean1;
            throw new UnsupportedOperationException("Not supported yet.");
        } else if (TkRefexUuidRevision.class.isAssignableFrom(o.getClass())) {
            // CONCEPT ERefsetCidMember()
            TkRefexUuidMember tmpMemb = new TkRefexUuidMember();
            TkRefexUuidRevision membRev = (TkRefexUuidRevision) o;
            tmpMemb.uuid1 = membRev.uuid1;
            tmpMemb.primordialUuid = tkram.primordialUuid; // immutable

            tmpMemb.additionalIds = null;
            for (TkIdentifier tki : ids) {
                if (tki.time == membRev.time) {
                    if (tmpMemb.additionalIds == null) {
                        tmpMemb.additionalIds = new ArrayList<>();
                    }
                    tmpMemb.additionalIds.add(tki);
                }
            }
            ids.removeAll(tmpMemb.additionalIds);

            tmpMemb.annotations = null;
            for (TkRefexAbstractMember<?> annotation : notas) {
                if (annotation.time == membRev.time) {
                    if (tmpMemb.annotations == null) {
                        tmpMemb.annotations = new ArrayList<>();
                    }
                    tmpMemb.annotations.add(annotation);
                }
            }
            notas.removeAll(tmpMemb.annotations);

            tmpMemb.revisions = null;

            tmpMemb.authorUuid = membRev.authorUuid;
            tmpMemb.componentUuid = tkram.componentUuid; // immutable
            tmpMemb.moduleUuid = membRev.moduleUuid;
            tmpMemb.pathUuid = membRev.pathUuid;
            tmpMemb.refsetUuid = tkram.refsetUuid; // immutable
            tmpMemb.statusUuid = membRev.statusUuid;
            tmpMemb.time = membRev.time;

            return tmpMemb;
        } else if (TkRefexIntRevision.class.isAssignableFrom(o.getClass())) {
            // INTEGER
//            tmpMemb = new TkRefexIntMember();
//            ((TkRefexIntMember) tmpMemb).int1 = ((TkRefexIntMember) o).int1;
            throw new UnsupportedOperationException("Not supported yet.");
        } else if (TkRefsetStrRevision.class.isAssignableFrom(o.getClass())) {
            // STRING
            TkRefsetStrMember tmpMemb = new TkRefsetStrMember();
            TkRefsetStrRevision membRev = (TkRefsetStrRevision) o;
            tmpMemb.string1 = membRev.string1;

            tmpMemb.primordialUuid = tkram.primordialUuid; // immutable

            tmpMemb.additionalIds = null;
            for (TkIdentifier tki : ids) {
                if (tki.time == membRev.time) {
                    if (tmpMemb.additionalIds == null) {
                        tmpMemb.additionalIds = new ArrayList<>();
                    }
                    tmpMemb.additionalIds.add(tki);
                }
            }
            ids.removeAll(tmpMemb.additionalIds);

            tmpMemb.annotations = null;
            for (TkRefexAbstractMember<?> annotation : notas) {
                if (annotation.time == membRev.time) {
                    if (tmpMemb.annotations == null) {
                        tmpMemb.annotations = new ArrayList<>();
                    }
                    tmpMemb.annotations.add(annotation);
                }
            }
            notas.removeAll(tmpMemb.annotations);

            tmpMemb.revisions = null;

            tmpMemb.authorUuid = membRev.authorUuid;
            tmpMemb.componentUuid = tkram.componentUuid; // immutable
            tmpMemb.moduleUuid = membRev.moduleUuid;
            tmpMemb.pathUuid = membRev.pathUuid;
            tmpMemb.refsetUuid = tkram.refsetUuid; // immutable
            tmpMemb.statusUuid = membRev.statusUuid;
            tmpMemb.time = membRev.time;

            return tmpMemb;
        } else if (TkRefexUuidFloatRevision.class.isAssignableFrom(o.getClass())) {
            // C_FLOAT CidFloatMember
//            tmpMemb = new TkRefexUuidFloatMember();
//            ((TkRefexUuidFloatMember) tmpMemb).float1 = ((TkRefexUuidFloatMember) o).float1;
//            ((TkRefexUuidFloatMember) tmpMemb).uuid1 = ((TkRefexUuidFloatMember) o).uuid1;
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

    private TkMedia makeMedia(TkMedia tkm) {
        TkMedia tmpMedia = new TkMedia();
        tmpMedia.primordialUuid = tkm.primordialUuid;

        tmpMedia.additionalIds = null;
        tmpMedia.annotations = null;
        tmpMedia.revisions = null;

        tmpMedia.authorUuid = tkm.authorUuid;
        tmpMedia.conceptUuid = tkm.conceptUuid;
        tmpMedia.dataBytes = tkm.dataBytes;
        tmpMedia.format = tkm.format;
        tmpMedia.moduleUuid = tkm.moduleUuid;
        tmpMedia.pathUuid = tkm.pathUuid;
        tmpMedia.statusUuid = tkm.statusUuid;
        tmpMedia.textDescription = tkm.textDescription;
        tmpMedia.time = tkm.time;
        tmpMedia.typeUuid = tkm.typeUuid;

        return tmpMedia;
    }

    private TkMedia makeMedia(TkMedia tkm, TkMediaRevision tkmRev) {
        TkMedia tmpMedia = new TkMedia();
        tmpMedia.primordialUuid = tkm.primordialUuid;

        tmpMedia.additionalIds = null;
        tmpMedia.annotations = null;
        tmpMedia.revisions = null;

        tmpMedia.authorUuid = tkmRev.authorUuid;
        tmpMedia.conceptUuid = tkm.conceptUuid;
        tmpMedia.dataBytes = tkm.dataBytes;
        tmpMedia.format = tkm.format;
        tmpMedia.moduleUuid = tkmRev.moduleUuid;
        tmpMedia.pathUuid = tkmRev.pathUuid;
        tmpMedia.statusUuid = tkmRev.statusUuid;
        tmpMedia.textDescription = tkmRev.textDescription;
        tmpMedia.time = tkmRev.time;
        tmpMedia.typeUuid = tkmRev.typeUuid;

        return tmpMedia;
    }

}
