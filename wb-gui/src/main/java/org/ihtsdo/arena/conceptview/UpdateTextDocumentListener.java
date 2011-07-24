/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.example.binding.WbDescType;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.example.binding.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @author kec
 */
public class UpdateTextDocumentListener implements DocumentListener, ActionListener {

    FixedWidthJEditorPane editorPane;
    DescriptionAnalogBI desc;
    Timer t;
    I_GetConceptData c;
    boolean update = false;
    I_ConfigAceFrame config;
    Collection<? extends RefexChronicleBI<?>> refexes;
    ConceptChronicleBI gbConcept;
    ConceptChronicleBI usConcept;
    int prefNid;
    int acceptNid;
    TerminologyConstructorBI tc;

    public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane,
            DescriptionAnalogBI desc) throws TerminologyException, IOException {
        super();
        this.editorPane = editorPane;
        this.desc = desc;
        t = new Timer(5000, this);
        c = Terms.get().getConcept(desc.getConceptNid());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        try {
            desc.setText(editorPane.extractText());
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        try {
            desc.setText(editorPane.extractText());
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        try {
            desc.setText(editorPane.extractText());
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            config = Terms.get().getActiveAceFrameConfig();
            tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                    config.getViewCoordinate());
            if (update) { //create new

                update = false;

                refexes = desc.getCurrentAnnotations(config.getViewCoordinate());
                int type = desc.getTypeNid();
                int fsn = 0;

                //get rf1/rf2 concept
                if (Ts.get().hasUuid(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getPrimUuid())) {
                    fsn = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
                } else {
                    fsn = WbDescType.FULLY_SPECIFIED.getLenient().getNid();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    gbConcept = SnomedMetadataRf2.GB_ENGLISH_REFSET_RF2.getLenient();
                } else {
                    gbConcept = SnomedMetadataRf1.GB_LANGUAGE_REFSET_RF1.getLenient();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient().getPrimUuid())) {
                    usConcept = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getLenient();
                } else {
                    usConcept = SnomedMetadataRf1.US_LANGUAGE_REFSET_RF1.getLenient();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getPrimUuid())) {
                    prefNid = SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();
                } else {
                    prefNid = SnomedMetadataRf1.PREFERRED_TERM_DESCRIPTION_TYPE_RF1.getLenient().getNid();
                }
                if (Ts.get().hasUuid(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getPrimUuid())) {
                    acceptNid = SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid();
                } else {
                    acceptNid = SnomedMetadataRf1.ACCEPTABLE_DESCRIPTION_TYPE_RF1.getLenient().getNid();
                }

                //set initial word case sensitivity
                String descText = editorPane.extractText();
                String initialWord = null;
                if (descText.indexOf(" ") != -1) {
                    initialWord = descText.substring(0, descText.indexOf(" "));
                } else {
                    initialWord = descText;
                }
                if (CsWordsHelper.isIcTypeSignificant(initialWord, CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()) == true
                        && desc.isInitialCaseSignificant() == false) {
                    desc.setInitialCaseSignificant(true);
                } else if (CsWordsHelper.isIcTypeSignificant(initialWord, CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()) == false
                        && desc.isInitialCaseSignificant() == true) {
                    desc.setInitialCaseSignificant(false);
                } else if (CsWordsHelper.isIcTypeSignificant(initialWord, CaseSensitive.MAYBE_IC_SIGNIFICANT.getLenient().getNid()) == true
                        && desc.isInitialCaseSignificant() == false) {
                    desc.setInitialCaseSignificant(false);
                }

                if (refexes.isEmpty()) { //check for previous changes
                    if (type == fsn) {
                        doFsnUpdate();
                    } else {
                        doSynUpdate();
                    }

                } else { //TODO modify uncomitted version
                    RefexCnidAnalogBI gbRefex = null;
                    RefexCnidAnalogBI usRefex = null;
                    for (RefexChronicleBI<?> descRefex : refexes) {
                        if (descRefex.isUncommitted()) {
                            if (descRefex.getCollectionNid() == gbConcept.getNid()) {
                                gbRefex = (RefexCnidAnalogBI) descRefex;;
                            } else if (descRefex.getCollectionNid() == usConcept.getNid()) {
                                usRefex = (RefexCnidAnalogBI) descRefex;
                            }
                        }
                    }
                    if (type == fsn) {
                        doFsnUpdate(gbRefex, usRefex);
                    } else {
                        doSynUpdate(gbRefex, usRefex);
                    }
                }
                I_GetConceptData concept = Terms.get().getConceptForNid(desc.getNid());
                Terms.get().addUncommitted(concept);

            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void doFsnUpdate() throws PropertyVetoException, IOException, InvalidCAB {
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());

        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) {//acceptable in both
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        usConcept.getNid());
                refexSpecUs.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        gbConcept.getNid());
                refexSpecGb.put(RefexProperty.CNID1, prefNid);
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        usConcept.getNid());
                refexSpecUs.put(RefexProperty.CNID1, prefNid);
                RefexChronicleBI<?> newRefex = tc.construct(refexSpecUs);
                I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                Ts.get().addUncommitted(refex);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        gbConcept.getNid());
                refexSpecGb.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefex = tc.construct(refexSpecGb);
                I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                Ts.get().addUncommitted(refex);
            }
        }
    }

    private void doSynUpdate() throws PropertyVetoException, IOException, InvalidCAB {
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());

        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in both 
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        usConcept.getNid());
                refexSpecUs.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        gbConcept.getNid());
                refexSpecGb.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        usConcept.getNid());
                refexSpecUs.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                /* REMOVED FOR RF2
                RefexCAB refexSpecGb = new RefexCAB(
                TK_REFSET_TYPE.CID,
                desc.getNid(),
                gbConcept.getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);
                
                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
                Ts.get().addUncommitted(refexGb); */

                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                /* REMOVED FOR RF2
                RefexCAB refexSpecUs = new RefexCAB(
                TK_REFSET_TYPE.CID,
                desc.getNid(),
                Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
                Ts.get().addUncommitted(refexUs); */

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        gbConcept.getNid());
                refexSpecGb.put(RefexProperty.CNID1, acceptNid);
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
                Ts.get().addUncommitted(refexGb);

            }
        }
    }

    private void doFsnUpdate(RefexCnidAnalogBI gbRefex, RefexCnidAnalogBI usRefex)
            throws PropertyVetoException, IOException, InvalidCAB {
        TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                config.getViewCoordinate());
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());



        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) {//acceptable in both
                usRefex.setCnid1(prefNid);
                gbRefex.setCnid1(prefNid);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                if (usRefex == null) {
                    doFsnUpdate();
                } else {
                    usRefex.setCnid1(prefNid);
                }
                //forget GB
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == gbConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                if (gbRefex == null) {
                    doFsnUpdate();
                } else {
                    gbRefex.setCnid1(prefNid);
                }
                //forget US
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == usConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
            }
        }
    }

    private void doSynUpdate(RefexCnidAnalogBI gbRefex, RefexCnidAnalogBI usRefex) throws PropertyVetoException, IOException, InvalidCAB {
        TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                config.getViewCoordinate());
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());

        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) {//acceptable in both
                if (usRefex == null) {
                    //forget GB
                    List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                    for (I_ExtendByRef ext : extensions) {
                        if (ext.getRefsetId() == gbConcept.getNid()) {
                            Terms.get().forget(ext);
                        }
                    }
                    doSynUpdate();
                } else {
                    usRefex.setCnid1(acceptNid);
                }
                if (gbRefex == null) {
                    //forget US
                    List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                    for (I_ExtendByRef ext : extensions) {
                        if (ext.getRefsetId() == usConcept.getNid()) {
                            Terms.get().forget(ext);
                        }
                    }
                    doSynUpdate();
                } else {
                    gbRefex.setCnid1(acceptNid);
                }

            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                if (usRefex == null) {
                    doSynUpdate();
                } else {
                    usRefex.setCnid1(acceptNid);
                }
                //forget GB
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == gbConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                if (gbRefex == null) {
                    doSynUpdate();
                } else {
                    gbRefex.setCnid1(acceptNid);
                }
                //forget US
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == usConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
            }
        }
    }
}