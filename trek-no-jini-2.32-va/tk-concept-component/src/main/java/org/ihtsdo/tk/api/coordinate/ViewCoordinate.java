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
package org.ihtsdo.tk.api.coordinate;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.Serializable;

import java.util.Arrays;

/**
 * The Class ViewCoordinate contains specific information which filters a
 * component and allows for different versions of components based on a
 * particular view.
 */
public class ViewCoordinate implements Serializable {

    private long lastModSequence = Long.MIN_VALUE;
    private NidSetBI allowedStatusNids;
    private int classifierNid;
    private ContradictionManagerBI contradictionManager;
    private NidSetBI isaTypeNids;
    private NidListBI langPrefList;
    private LANGUAGE_SORT langSort;
    private int languageRefexNid;
    private PositionSetBI positionSet;
    private Precedence precedence;
    private RelAssertionType relAssertionType;
    private ViewCoordinate vcWithAllStatusValues;

    //~--- constructors --------------------------------------------------------
    /**
     * Creates a new view coordinate based on another
     * <code>ViewCoordinate</code>.
     *
     * @param another the other <code>ViewCoordinate</code>
     */
    public ViewCoordinate(ViewCoordinate another) {
        super();
        this.precedence = another.precedence;

        if (another.positionSet != null) {
            this.positionSet = new PositionSet(another.positionSet);
        }

        if (another.allowedStatusNids != null) {
            this.allowedStatusNids = new NidSet(another.allowedStatusNids.getSetValues());
        }

        if (another.isaTypeNids != null) {
            this.isaTypeNids = new NidSet(another.isaTypeNids.getSetValues());
        }

        this.contradictionManager = another.contradictionManager;
        this.languageRefexNid = another.languageRefexNid;
        this.classifierNid = another.classifierNid;
        this.relAssertionType = another.relAssertionType;

        if (another.langPrefList != null) {
            this.langPrefList = new NidList(another.langPrefList.getListArray());
        }

        this.langSort = another.langSort;
        this.lastModSequence = another.lastModSequence;
    }

    /**
     * Creates a new view coordinate based on the given fields.
     *
     * @param precedence the precedence
     * @param positionSet the position set
     * @param allowedStatusNids the allowed status nids
     * @param isaTypeNids the isa type nids
     * @param contradictionManager the contradiction manager
     * @param languageRefexNid the language refex nid
     * @param classifierNid the classifier nid
     * @param relAssertionType the relationship assertion type
     * @param langPrefListNids the language preference list nids
     * @param langSort the language sort
     */
    public ViewCoordinate(Precedence precedence, PositionSetBI positionSet, NidSetBI allowedStatusNids,
            NidSetBI isaTypeNids, ContradictionManagerBI contradictionManager, int languageRefexNid,
            int classifierNid, RelAssertionType relAssertionType, NidListBI langPrefListNids,
            LANGUAGE_SORT langSort) {
        super();
        assert precedence != null;
        assert contradictionManager != null;
        this.precedence = precedence;

        if (positionSet != null) {
            this.positionSet = new PositionSet(positionSet);
        }

        if (allowedStatusNids != null) {
            this.allowedStatusNids = new NidSet(allowedStatusNids.getSetValues());
        }

        if (isaTypeNids != null) {
            this.isaTypeNids = new NidSet(isaTypeNids.getSetValues());
        }

        this.contradictionManager = contradictionManager;
        this.languageRefexNid = languageRefexNid;
        this.classifierNid = classifierNid;
        this.relAssertionType = relAssertionType;

        if (langPrefListNids != null) {
            this.langPrefList = new NidList(langPrefListNids.getListArray());
        }

        this.langSort = langSort;
    }

    //~--- enums ---------------------------------------------------------------
    /**
     * The Enumeration LANGUAGE_SORT specifies the a preference for viewing
     * descriptions.
     */
    public enum LANGUAGE_SORT {

        /**
         * The language before description type.
         */
        LANG_BEFORE_TYPE("language before type"),
        /**
         * The description type before language.
         */
        TYPE_BEFORE_LANG("type before language"),
        /**
         * The language refex.
         */
        LANG_REFEX("use language refex"),
        /**
         * The RF2 language refex.
         */
        RF2_LANG_REFEX("use RF2 language refex");
        ;

      private String desc;

        //~--- constructors -----------------------------------------------------
        /**
         * Instantiates a new language sort.
         *
         * @param sortDescription the sort description
         */
        private LANGUAGE_SORT(String sortDescription) {
            this.desc = sortDescription;
        }

        //~--- methods ----------------------------------------------------------
        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return desc;
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ViewCoordinate) {
            ViewCoordinate another = (ViewCoordinate) obj;

            if (!testEquals(precedence, another.precedence)) {
                return false;
            }

            if (!testEquals(positionSet, another.positionSet)) {
                return false;
            }

            if (!testEquals(allowedStatusNids, another.allowedStatusNids)) {
                return false;
            }

            if (!testEquals(isaTypeNids, another.isaTypeNids)) {
                return false;
            }

            if (!testEquals(contradictionManager, another.contradictionManager)) {
                return false;
            }

            if (!testEquals(languageRefexNid, another.languageRefexNid)) {
                return false;
            }

            if (!testEquals(classifierNid, another.classifierNid)) {
                return false;
            }

            if (!testEquals(relAssertionType, another.relAssertionType)) {
                return false;
            }

            if (!testEquals(langPrefList, another.langPrefList)) {
                return false;
            }

            if (!testEquals(langSort, another.langSort)) {
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hashCode = 0;

        for (PositionBI pos : positionSet.getPositionArray()) {
            hashCode = Hashcode.compute(new int[]{hashCode, pos.getPath().getConceptNid(), pos.getVersion()});
        }

        return hashCode;
    }

    /**
     * Test equals.
     *
     * @param obj1 the obj1
     * @param obj2 the obj2
     * @return <code>true</code>, if equal
     */
    private static boolean testEquals(Object obj1, Object obj2) {
        if ((obj1 == null) && (obj2 == null)) {
            return true;
        }

        if (obj1 == obj2) {
            return true;
        }

        if (obj1 instanceof NidSetBI) {
            NidSetBI ns1 = (NidSetBI) obj1;
            NidSetBI ns2 = (NidSetBI) obj2;

            return Arrays.equals(ns1.getSetValues(), ns2.getSetValues());
        }

        if (obj1 instanceof NidListBI) {
            NidListBI ns1 = (NidListBI) obj1;
            NidListBI ns2 = (NidListBI) obj2;

            return Arrays.equals(ns1.getListArray(), ns2.getListArray());
        }

        if (obj1 instanceof PositionSetBI) {
            PositionSetBI ns1 = (PositionSetBI) obj1;
            PositionSetBI ns2 = (PositionSetBI) obj2;

            if (ns1.size() == 1) {
                if (ns2.size() == 1) {
                    return ns1.getPositionArray()[0].equals(ns2.getPositionArray()[0]);
                }
            }

            return Arrays.equals(ns1.getPositionArray(), ns2.getPositionArray());
        }

        if (obj1.equals(obj2)) {
            return true;
        }

        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        TerminologySnapshotDI snap = Ts.get().getSnapshot(this);
        StringBuilder sb = new StringBuilder();

        sb.append("precedence: ").append(precedence);
        sb.append(" \npositions: ").append(positionSet);

        String statusStr = "all";

        if (allowedStatusNids != null) {
            statusStr = allowedStatusNids.toString();
        }

        sb.append(" \nallowedStatus: ");

        if (statusStr.length() < 50) {
            sb.append(statusStr);
        } else {
            sb.append(statusStr.substring(0, 50)).append("...");
        }

        sb.append(" \nisaTypes: ").append(isaTypeNids);
        sb.append(" \ncontradiction: ").append(contradictionManager);
        getConceptText(sb.append(" \nlanguage: "), snap, languageRefexNid);
        getConceptText(sb.append(" \nclassifier: "), snap, classifierNid);
        sb.append(" \nrelAssertionType: ").append(relAssertionType);

        return sb.toString();
    }

    //~--- get methods ---------------------------------------------------------
    /**
     * Gets the allowed status nids.
     *
     * @return the allowed status nids
     */
    public NidSetBI getAllowedStatusNids() {
        return allowedStatusNids;
    }

    /**
     * Gets the classifier nid. Allows for multiple classifiers, specifies which
     * classifier should be used for viewing the inferred view.
     *
     * @return the classifier nid
     */
    public int getClassifierNid() {
        return classifierNid;
    }

    /**
     * Gets the concept text.
     *
     * @param stringBuilder the string builder
     * @param terminologySnapshot the terminology snapshot
     * @param conceptNid the concept nid
     * @return the concept text
     */
    private void getConceptText(StringBuilder stringBuilder,
            TerminologySnapshotDI terminologySnapshot, int conceptNid) {
        if (conceptNid == Integer.MAX_VALUE) {
            stringBuilder.append("Integer.MAX_VALUE");

            return;
        }

        if (conceptNid == Integer.MIN_VALUE) {
            stringBuilder.append("Integer.MIN_VALUE");

            return;
        }

        try {
            if ((terminologySnapshot.getConceptVersion(conceptNid) != null)
                    && (terminologySnapshot.getConceptVersion(conceptNid).getDescriptionPreferred() != null)) {
                stringBuilder.append(terminologySnapshot.getConceptVersion(conceptNid).getDescriptionPreferred().getText());
            } else {
                stringBuilder.append(Integer.toString(conceptNid));
            }
        } catch (IOException ex) {
            stringBuilder.append(ex.getLocalizedMessage());
        } catch (ContradictionException ex) {
            stringBuilder.append(ex.getLocalizedMessage());
        }
    }

    /**
     * Gets the contradiction manager. Specifies how a contradiction should be
     * managed if the data for view contains a contradiction.
     *
     * @return the contradiction manager
     */
    public ContradictionManagerBI getContradictionManager() {
        return contradictionManager;
    }

    /**
     * Gets the isa type nids. Used to determine which isa types should be
     * displayed.
     *
     * @return the isa type nids
     */
    public NidSetBI getIsaTypeNids() {
        return isaTypeNids;
    }

    /**
     * Gets a list of preferred language nids. Used to determine which language
     * to use for displaying descriptions.
     *
     * @return the language preference list
     */
    public NidListBI getLangPrefList() {
        return langPrefList;
    }

    /**
     * Gets the language sort preference.
     *
     * @return the language sorting preference
     */
    public LANGUAGE_SORT getLangSort() {
        return langSort;
    }

    /**
     * Gets the nid of the language refex.
     *
     * @return the language refex nid
     */
    public int getLanguageRefexNid() {
        return languageRefexNid;
    }

    /**
     * Gets the last modification sequence. Specifies the last time the view
     * coordinate changed.
     *
     * @return the last modification sequence
     */
    public long getLastModSequence() {
        return lastModSequence;
    }

    /**
     * Gets a set of positions, path plus a point in time. Used to compute which
     * components are either active or retired.
     *
     * @return the position set
     */
    public PositionSetBI getPositionSet() {
        return positionSet;
    }

    /**
     * Gets the precedence to be used in determining which version to display.
     *
     * @return the precedence
     */
    public Precedence getPrecedence() {
        return precedence;
    }

    /**
     * Gets the relationship assertion type of the data to display. Used
     * primarily by taxonomies.
     *
     * @return the relationship assertion type
     */
    public RelAssertionType getRelationshipAssertionType() {
        return relAssertionType;
    }

    /**
     * Gets the view coordinate with all status values.
     *
     * @return the view coordinate with all status values
     */
    public ViewCoordinate getViewCoordinateWithAllStatusValues() {
        if (vcWithAllStatusValues == null) {
            vcWithAllStatusValues = new ViewCoordinate(this);
            vcWithAllStatusValues.allowedStatusNids = null;
        }

        return vcWithAllStatusValues;
    }

    //~--- set methods ---------------------------------------------------------
    /**
     * Sets the allowed status nids.
     *
     * @param allowedStatusNids the new allowed status nids
     */
    public void setAllowedStatusNids(NidSetBI allowedStatusNids) {
        this.lastModSequence = Ts.get().getSequence();
        this.allowedStatusNids = allowedStatusNids;
    }

    /**
     * Sets the classifier nid.
     *
     * @param classifierNid the new classifier nid
     */
    public void setClassifierNid(int classifierNid) {
        this.lastModSequence = Ts.get().getSequence();
        this.vcWithAllStatusValues = null;
        this.classifierNid = classifierNid;
    }

    /**
     * Sets the contradiction manager.
     *
     * @param contradictionManager the new contradiction manager
     */
    public void setContradictionManager(ContradictionManagerBI contradictionManager) {
        this.lastModSequence = Ts.get().getSequence();
        this.contradictionManager = contradictionManager;
    }

    /**
     * Sets the position set.
     *
     * @param positionSet the new position set
     */
    public void setPositionSet(PositionSetBI positionSet) {
        this.lastModSequence = Ts.get().getSequence();
        this.vcWithAllStatusValues = null;
        this.positionSet = positionSet;
    }

    /**
     * Sets the precedence.
     *
     * @param precedence the new precedence
     */
    public void setPrecedence(Precedence precedence) {
        this.precedence = precedence;
    }

    /**
     * Sets the relationship assertion type.
     *
     * @param relAssertionType the new relationship assertion type
     */
    public void setRelationshipAssertionType(RelAssertionType relAssertionType) {
        this.lastModSequence = Ts.get().getSequence();
        this.vcWithAllStatusValues = null;
        this.relAssertionType = relAssertionType;
    }

    /**
     * Sets the isa type nids.
     *
     * @param isaTypeNids the new isa type nids
     */
    public void setIsaTypeNids(NidSetBI isaTypeNids) {
        this.isaTypeNids = isaTypeNids;
    }
}
