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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.media.TkMediaRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_float.TkRefexUuidFloatMember;
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

    private static final String FILE_SEPARATOR = File.separator;
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
     * @parameter default-value= "${project.build.directory}/${project.build.finalName}/sct_changesets"
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
            }
        } catch (EOFException e) {
            System.out.print("\n\n");
            in.close(); // normal end of file
        }

        for (DataOutputStream dos : eccsOutput) {
            dos.flush();
            dos.close();
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
            System.out.println(":DEBUG:!!!: idx out of range");
        }
        return idx;
    }

    private HashMap<Long, EConcept> processEconcept2ChangeSet(EConcept eConcept) {
        EConcept ec;
        HashMap<Long, EConcept> eccsMap = new HashMap<>();

        // ATTRIBUTES
        TkConceptAttributes attr = eConcept.conceptAttributes;
        ec = makeEconcept(eConcept);
        ec.conceptAttributes = makeAttr(attr);
        eccsMap.put(attr.time, ec);
        if (attr.revisions != null) {
            for (TkConceptAttributesRevision attrRev : attr.revisions) {
                ec = makeEconcept(eConcept);
                ec.conceptAttributes = makeAttr(attr, attrRev);
                eccsMap.put(attrRev.time, ec);
            }
        }

        // DESCRIPTIONS
        if (eConcept.descriptions != null) {
            for (TkDescription tkd : eConcept.descriptions) {
                ec = eccsMap.get(tkd.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                }
                if (ec.descriptions == null) {
                    ec.descriptions = new ArrayList<>();
                }
                ec.descriptions.add(makeDescr(tkd));
                if (tkd.revisions != null) {
                    for (TkDescriptionRevision tkdRev : tkd.revisions) {
                        ec = eccsMap.get(tkdRev.time);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                        }
                        if (ec.descriptions == null) {
                            ec.descriptions = new ArrayList<>();
                        }
                        ec.descriptions.add(makeDescr(tkd, tkdRev));
                    }
                }
            }
        }

        // RELATIONSHIPS
        if (eConcept.relationships != null) {
            for (TkRelationship tkr : eConcept.relationships) {
                ec = eccsMap.get(tkr.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                }
                if (ec.relationships == null) {
                    ec.relationships = new ArrayList<>();
                }
                ec.relationships.add(makeRel(tkr));
                if (tkr.revisions != null) {
                    for (TkRelationshipRevision tkrRev : tkr.revisions) {
                        ec = eccsMap.get(tkrRev.time);
                        if (ec == null) {
                            ec = makeEconcept(eConcept);
                        }
                        if (ec.relationships == null) {
                            ec.relationships = new ArrayList<>();
                        }
                        ec.relationships.add(makeRel(tkr, tkrRev));
                    }
                }
            }
        }

        // REFSET MEMBERS
        if (eConcept.refsetMembers != null) {
            for (TkRefexAbstractMember<?> tkram : eConcept.refsetMembers) {
                ec = eccsMap.get(tkram.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
                }
                if (ec.refsetMembers == null) {
                    ec.refsetMembers = new ArrayList<>();
                }
                ec.refsetMembers.add(makeMemb(tkram));
                List<?> tkramrev = tkram.revisions;
                if (tkram.revisions != null) {
                    for (Object o : tkramrev) {
                        // makeMemb(tkram, o);
                        System.out.println(":!!!:???: is this a non-issue? ");
                    }
                }
            }
        }

        // MEDIA
        if (eConcept.media != null) {
            for (TkMedia tkm : eConcept.media) {
                ec = eccsMap.get(tkm.time);
                if (ec == null) {
                    ec = makeEconcept(eConcept);
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
                System.out.println(":DEBUG:!!!: concept not in map");
            } else {
                DataOutputStream dos = eccsOutput.get(idx);
                if (dos == null) {
                    System.out.println(":DEBUG:!!!: null dos");
                } else {
                    ec.writeExternal(dos);
                }
            }
        }
    }

    private EConcept makeEconcept(EConcept ec) {
        EConcept ecTmp = new EConcept();
        ecTmp.primordialUuid = ec.primordialUuid;
        
        ecTmp.annotationIndexStyleRefex = ec.annotationIndexStyleRefex;
        ecTmp.annotationStyleRefex = ec.annotationStyleRefex;
        ec.conceptAttributes = null;
        ec.descriptions = null;
        ec.relationships = null;
        ec.refsetMembers = null;
        ec.media = null;
        return ecTmp;
    }

    private TkConceptAttributes makeAttr(TkConceptAttributes attr) {
        TkConceptAttributes attrTmp = new TkConceptAttributes();
        attrTmp.primordialUuid = attr.primordialUuid;
        attrTmp.revisions = null;

        attrTmp.additionalIds = attr.additionalIds;
        attrTmp.defined = attr.defined;
        attrTmp.statusUuid = attr.statusUuid;
        attrTmp.time = attr.time;
        attrTmp.authorUuid = attr.authorUuid;
        attrTmp.moduleUuid = attr.moduleUuid;
        attrTmp.pathUuid = attr.pathUuid;
        // :!!!:
        // attrTmp.annotations = attr.annotations;
        return attrTmp;
    }

    private TkConceptAttributes makeAttr(TkConceptAttributes attr,
            TkConceptAttributesRevision attrRev) {
        TkConceptAttributes attrTmp = new TkConceptAttributes();
        attrTmp.primordialUuid = attr.primordialUuid;
        attrTmp.additionalIds = null;
        // attrTmp.annotations = attr.annotations;

        attrTmp.defined = attrRev.defined;
        attrTmp.statusUuid = attrRev.statusUuid;
        attrTmp.time = attrRev.time;
        attrTmp.authorUuid = attrRev.authorUuid;
        attrTmp.moduleUuid = attrRev.moduleUuid;
        attrTmp.pathUuid = attrRev.pathUuid;
        attrTmp.revisions = null;
        return attrTmp;
    }

    private TkDescription makeDescr(TkDescription tkd) {
        TkDescription tmpDesc = new TkDescription();
        tmpDesc.primordialUuid = tkd.primordialUuid;
        tmpDesc.annotations = null;
        tmpDesc.revisions = null;

        tmpDesc.additionalIds = tkd.additionalIds;
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

    private TkDescription makeDescr(TkDescription tkd, TkDescriptionRevision tkdRev) {
        TkDescription tmpDesc = new TkDescription();
        tmpDesc.primordialUuid = tkd.primordialUuid;
        tmpDesc.revisions = null;

        tmpDesc.authorUuid = tkdRev.authorUuid;
        tmpDesc.initialCaseSignificant = tkdRev.initialCaseSignificant;
        tmpDesc.lang = tkdRev.lang;
        tmpDesc.moduleUuid = tkdRev.moduleUuid;
        tmpDesc.pathUuid = tkdRev.pathUuid;
        tmpDesc.pathUuid = tkdRev.statusUuid;
        tmpDesc.text = tkdRev.text;
        tmpDesc.time = tkdRev.time;
        tmpDesc.typeUuid = tkdRev.typeUuid;

        return tmpDesc;
    }

    private TkRelationship makeRel(TkRelationship tkr) {
        TkRelationship tmpRel = new TkRelationship();
        tmpRel.primordialUuid = tkr.primordialUuid;
        tmpRel.revisions = null;
        tmpRel.annotations = null;

        tmpRel.additionalIds = tkr.additionalIds;
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

    private TkRelationship makeRel(TkRelationship tkr, TkRelationshipRevision tkrRev) {
        TkRelationship tmpRel = new TkRelationship();
        tmpRel.primordialUuid = tkr.primordialUuid;
        tmpRel.revisions = null;
        tmpRel.annotations = null;

        // tmpRel.additionalIds = tkr.additionalIds; :!!!: 
        tmpRel.authorUuid = tkrRev.authorUuid;
        // tmpRel.c1Uuid = tkr.c1Uuid; // :!!!: what to do with immutiable
        // tmpRel.c2Uuid = tkr.c2Uuid;
        tmpRel.characteristicUuid = tkrRev.characteristicUuid;
        tmpRel.moduleUuid = tkrRev.moduleUuid;
        tmpRel.pathUuid = tkrRev.pathUuid;
        tmpRel.refinabilityUuid = tkrRev.refinabilityUuid;
        // tmpRel.relGroup = tkr.relGroup;
        tmpRel.statusUuid = tkrRev.statusUuid;
        tmpRel.time = tkrRev.time;
        tmpRel.typeUuid = tkrRev.typeUuid;

        return tmpRel;
    }

    private TkRefexAbstractMember makeMemb(TkRefexAbstractMember<?> tkram) {
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
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        tmpMemb.primordialUuid = tkram.primordialUuid;
        tmpMemb.revisions = null;
        tmpMemb.annotations = null;

        tmpMemb.additionalIds = tkram.additionalIds;
        tmpMemb.authorUuid = tkram.authorUuid;
        tmpMemb.componentUuid = tkram.componentUuid;
        tmpMemb.moduleUuid = tkram.moduleUuid;
        tmpMemb.pathUuid = tkram.pathUuid;
        tmpMemb.refsetUuid = tkram.refsetUuid;
        tmpMemb.statusUuid = tkram.statusUuid;
        tmpMemb.time = tkram.time;

        return tkram; // :!!!:
    }

    private TkMedia makeMedia(TkMedia tkm) {
        TkMedia tmpMedia = new TkMedia();
        tmpMedia.primordialUuid = tkm.primordialUuid;
        tmpMedia.revisions = null;
        tmpMedia.annotations = null;

        tmpMedia.additionalIds = tkm.additionalIds;
        tmpMedia.authorUuid = tkm.authorUuid;
        tmpMedia.conceptUuid = tkm.conceptUuid;
        tmpMedia.dataBytes = tkm.dataBytes;
        tmpMedia.conceptUuid = tkm.conceptUuid;
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
        tmpMedia.revisions = null;
        tmpMedia.annotations = null;
        // tmpMedia.annotations = tkmRev.annotations;

        // tmpMedia.additionalIds = tkmRev.additionalIds;
        tmpMedia.authorUuid = tkmRev.authorUuid;
        // tmpMedia.conceptUuid = tkmRev.conceptUuid;
        // tmpMedia.dataBytes= tkmRev.dataBytes;
        // tmpMedia.conceptUuid = tkmRev.conceptUuid;
        // tmpMedia.format = tkmRev.format;
        tmpMedia.moduleUuid = tkmRev.moduleUuid;
        tmpMedia.pathUuid = tkmRev.pathUuid;
        tmpMedia.statusUuid = tkmRev.statusUuid;
        tmpMedia.textDescription = tkmRev.textDescription;
        tmpMedia.time = tkmRev.time;
        tmpMedia.typeUuid = tkmRev.typeUuid;

        return tmpMedia;
    }

}
