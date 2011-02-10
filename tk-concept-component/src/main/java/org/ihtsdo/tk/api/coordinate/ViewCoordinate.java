package org.ihtsdo.tk.api.coordinate;

import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

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

    public LANGUAGE_SORT getLangSort() {
        return langSort;
    }

    public NidListBI getLangPrefList() {
        return langPrefList;
    }

    public PositionSetBI getPositionSet() {
        return positionSet;
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
        StringBuilder sb = new StringBuilder();
        sb.append("precedence: ").append(precedence);
        sb.append(" \npositions: ").append(positionSet);
        sb.append(" \nallowedStatus: ").append(allowedStatusNids);
        sb.append(" \nisaTypes: ").append(isaTypeNids);
        sb.append(" \ncontradiction: ").append(contradictionManager);
        sb.append(" \nlanguage: ").append(languageNid);
        sb.append(" \nclassifier: ").append(classifierNid);
        sb.append(" \nrelAssertionType: ").append(relAssertionType);

        return sb.toString();
    }
}
