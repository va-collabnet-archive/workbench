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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.AcceptabilityType;
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
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.example.binding.CaseSensitive;

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
            if (update) { //create new

                update = false;

                refexes = desc.getCurrentAnnotations(config.getViewCoordinate());
                //check for FSN or Pref term
                int type = desc.getTypeNid();
                int fsn = WbDescType.FULLY_SPECIFIED.getLenient().getNid();

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
                            if (descRefex.getCollectionNid() == Refsets.EN_GB_LANG.getLenient().getNid()) {
                                gbRefex = (RefexCnidAnalogBI) descRefex;;
                            } else if (descRefex.getCollectionNid() == Refsets.EN_US_LANG.getLenient().getNid()) {
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
        TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                config.getViewCoordinate());
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());

        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) {//acceptable in both
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_US_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_US_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefex = tc.construct(refexSpecUs);
                I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                Ts.get().addUncommitted(refex);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefex = tc.construct(refexSpecGb);
                I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
                Ts.get().addUncommitted(refex);
            }
        }
    }

    private void doSynUpdate() throws PropertyVetoException, IOException, InvalidCAB {
        TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                config.getViewCoordinate());
        TerminologyHelperDrools th = new TerminologyHelperDrools();

        desc.setText(editorPane.extractText());

        if (th.loadProperties()) {
            if (th.checkTermSpelling(editorPane.extractText(), "en-gb")
                    && th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in both 
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_US_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_US_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
                Ts.get().addUncommitted(refexUs);
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                RefexCAB refexSpecUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_GB_LANG.getLenient().getNid());
                refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

                RefexCAB refexSpecGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        desc.getNid(),
                        Refsets.EN_US_LANG.getLenient().getNid());
                refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
                RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

                I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
                Ts.get().addUncommitted(refexGb);
                I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
                Ts.get().addUncommitted(refexUs);
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
                usRefex.setCnid1(AcceptabilityType.PREF.getLenient().getNid());
                gbRefex.setCnid1(AcceptabilityType.PREF.getLenient().getNid());
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                if (usRefex == null) {
                    doFsnUpdate();
                } else {
                    usRefex.setCnid1(AcceptabilityType.PREF.getLenient().getNid());
                }
                //forget GB
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == Refsets.EN_GB_LANG.getLenient().getNid()) {
                        Terms.get().forget(ext);
                    }
                }
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                if (gbRefex == null) {
                    doFsnUpdate();
                } else {
                    gbRefex.setCnid1(AcceptabilityType.PREF.getLenient().getNid());
                }
                //forget US
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == Refsets.EN_US_LANG.getLenient().getNid()) {
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
                usRefex.setCnid1(AcceptabilityType.ACCEPTABLE.getLenient().getNid());
                gbRefex.setCnid1(AcceptabilityType.ACCEPTABLE.getLenient().getNid());
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-us")) { //acceptable in US
                usRefex.setCnid1(AcceptabilityType.ACCEPTABLE.getLenient().getNid());
                gbRefex.setCnid1(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getNid());
            } else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) { //acceptable in GB
                usRefex.setCnid1(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getNid());
                gbRefex.setCnid1(AcceptabilityType.ACCEPTABLE.getLenient().getNid());
            }
        }
    }
}