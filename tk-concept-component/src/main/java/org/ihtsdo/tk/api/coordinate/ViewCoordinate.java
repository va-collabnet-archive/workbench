package org.ihtsdo.tk.api.coordinate;

import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.RelAssertionType;

public class ViewCoordinate {

    private Precedence precedence;
    private PositionSetBI positionSet;
    private NidSetBI allowedStatusNids;
    private NidSetBI isaTypeNids;
    private ContradictionManagerBI contradictionManager;
    private int languageNid;
    private int classifierNid;
    private RelAssertionType relAssertionType;

    public ViewCoordinate(Precedence precedence, PositionSetBI positionSet,
            NidSetBI allowedStatusNids, NidSetBI isaTypeNids,
            ContradictionManagerBI contradictionManager,
            int languageNid,
            int classifierNid,
            RelAssertionType relAssertionType) {
        super();
        assert precedence != null;
        assert positionSet != null;
        assert allowedStatusNids != null;
        assert isaTypeNids != null;
        assert contradictionManager != null;
        this.precedence = precedence;
        this.positionSet = positionSet;
        this.allowedStatusNids = allowedStatusNids;
        this.isaTypeNids = isaTypeNids;
        this.contradictionManager = contradictionManager;
        this.languageNid = languageNid;
        this.classifierNid = classifierNid;
        this.relAssertionType = relAssertionType;
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
}
