package org.ihtsdo.tk.api.coordinate;

import java.io.IOException;
import java.util.Arrays;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.hash.Hashcode;

public class ViewCoordinate {

    public enum LANGUAGE_SORT {

        LANG_BEFORE_TYPE("language before type"),
        TYPE_BEFORE_LANG("type before language"),
        LANG_REFEX("use language refex");
        private String desc;

        private LANGUAGE_SORT(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return desc;
        }
    }
    private Precedence precedence;
    private PositionSetBI positionSet;
    private NidSetBI allowedStatusNids;
    private NidSetBI isaTypeNids;
    private ContradictionManagerBI contradictionManager;
    private int languageNid;
    private int classifierNid;
    private RelAssertionType relAssertionType;
    private NidListBI langPrefList;
    private LANGUAGE_SORT langSort;

    public ViewCoordinate(Precedence precedence, PositionSetBI positionSet,
            NidSetBI allowedStatusNids, NidSetBI isaTypeNids,
            ContradictionManagerBI contradictionManager,
            int languageNid,
            int classifierNid,
            RelAssertionType relAssertionType,
            NidListBI langPrefList,
            LANGUAGE_SORT langSort) {
        super();
        assert precedence != null;
        assert contradictionManager != null;
        this.precedence = precedence;
        this.positionSet = positionSet;
        this.allowedStatusNids = allowedStatusNids;
        this.isaTypeNids = isaTypeNids;
        this.contradictionManager = contradictionManager;
        this.languageNid = languageNid;
        this.classifierNid = classifierNid;
        this.relAssertionType = relAssertionType;
        this.langPrefList = langPrefList;
        this.langSort = langSort;
    }

    public ViewCoordinate(ViewCoordinate another) {
        super();
        this.precedence = another.precedence;
        this.positionSet = another.positionSet;
        this.allowedStatusNids = another.allowedStatusNids;
        this.isaTypeNids = another.isaTypeNids;
        this.contradictionManager = another.contradictionManager;
        this.languageNid = another.languageNid;
        this.classifierNid = another.classifierNid;
        this.relAssertionType = another.relAssertionType;
        this.langPrefList = another.langPrefList;
        this.langSort = another.langSort;
    }

    public LANGUAGE_SORT getLangSort() {
        return langSort;
    }

    public NidListBI getLangPrefList() {
        return langPrefList;
    }

    public PositionSetBI getPositionSet() {
        return positionSet;
    }

    public void setPositionSet(PositionSetBI positionSet) {
        this.positionSet = positionSet;
    }

    public NidSetBI getAllowedStatusNids() {
        return allowedStatusNids;
    }

    public Precedence getPrecedence() {
        return precedence;
    }

    public NidSetBI getIsaTypeNids() {
        return isaTypeNids;
    }

    public ContradictionManagerBI getContradictionManager() {
        return contradictionManager;
    }

    public int getLanguageNid() {
        return languageNid;
    }

    public int getClassifierNid() {
        return classifierNid;
    }

    public void setClassifierNid(int classifierNid) {
        this.classifierNid = classifierNid;
    }

    public RelAssertionType getRelAssertionType() {
        return relAssertionType;
    }

    public void setRelAssertionType(RelAssertionType relAssertionType) {
        this.relAssertionType = relAssertionType;
    }

    @Override
    public String toString() {
        TerminologySnapshotDI snap = Ts.get().getSnapshot(this);
        StringBuilder sb = new StringBuilder();
        sb.append("precedence: ").append(precedence);
        sb.append(" \npositions: ").append(positionSet);
        String statusStr = allowedStatusNids.toString();
        sb.append(" \nallowedStatus: ");
        if (statusStr.length() < 50) {
            sb.append(statusStr);
        } else {
            sb.append(statusStr.substring(0, 50)).append("...");
        }
        sb.append(" \nisaTypes: ").append(isaTypeNids);
        sb.append(" \ncontradiction: ").append(contradictionManager);
        getConceptText(sb.append(" \nlanguage: "), snap, languageNid);
        getConceptText(sb.append(" \nclassifier: "), snap, classifierNid);
        sb.append(" \nrelAssertionType: ").append(relAssertionType);

        return sb.toString();
    }

    private void getConceptText(StringBuilder sb, TerminologySnapshotDI snap, int nid) {
        if (nid == Integer.MAX_VALUE) {
            sb.append("Integer.MAX_VALUE");
            return;
        }
        if (nid == Integer.MIN_VALUE) {
            sb.append("Integer.MIN_VALUE");
            return;
        }
        
        try {
            if (snap.getConceptVersion(nid) != null
                    && snap.getConceptVersion(nid).getPreferredDescription() != null) {
                sb.append(snap.getConceptVersion(nid).getPreferredDescription().getText());
            } else {
                sb.append(Integer.toString(nid));
            }
        } catch (IOException ex) {
            sb.append(ex.getLocalizedMessage());
        } catch (ContraditionException ex) {
            sb.append(ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof ViewCoordinate) {
            ViewCoordinate another = (ViewCoordinate) o;
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
            if (!testEquals(languageNid, another.languageNid)) {
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

    private static boolean testEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == o2) {
            return true;
        }
        if (o1 instanceof NidSetBI) {
            NidSetBI ns1 = (NidSetBI) o1;
            NidSetBI ns2 = (NidSetBI) o2;
            return Arrays.equals(ns1.getSetValues(), ns2.getSetValues());

        }
        if (o1 instanceof NidListBI) {
            NidListBI ns1 = (NidListBI) o1;
            NidListBI ns2 = (NidListBI) o2;
            return Arrays.equals(ns1.getListArray(), ns2.getListArray());

        }
        if (o1 instanceof PositionSetBI) {
            PositionSetBI ns1 = (PositionSetBI) o1;
            PositionSetBI ns2 = (PositionSetBI) o2;
            if (ns1.size() == 1) {
                if (ns2.size() == 1) {
                    return ns1.getPositionArray()[0].equals(ns2.getPositionArray()[0]);
                }
            }
            return Arrays.equals(ns1.getPositionArray(), ns2.getPositionArray());
        }
        if (o1.equals(o2)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (PositionBI pos : positionSet.getPositionArray()) {
            hashCode = Hashcode.compute(new int[]{hashCode,
                        pos.getPath().getConceptNid(),
                        pos.getVersion()});
        }
        return hashCode;
    }

    public IsaCoordinate getIsaCoordinate() {
        IsaCoordinate isaCoordinate = new IsaCoordinate(positionSet.iterator().next(), 
                allowedStatusNids, isaTypeNids,
                precedence, contradictionManager, classifierNid, relAssertionType);
        return isaCoordinate;
    }

    public void setAllowedStatusNids(NidSetBI allowedStatusNids) {
        this.allowedStatusNids = allowedStatusNids;
    }
}
