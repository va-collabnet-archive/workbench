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
package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;

public class RefsetSpecTreeNode extends DefaultMutableTreeNode implements Comparable<RefsetSpecTreeNode> {

    private I_ConfigAceFrame aceConfig;
    private I_ExtendByRefVersion extension;
    private int truthId = Integer.MAX_VALUE;
    private String truthDesc;
    private String clauseDesc;
    private String constraintDesc;
    private boolean inactive = false;
    private boolean uncommitted = false;

    public boolean isUncommitted() {
        return uncommitted;
    }

    public void setUncommitted(boolean uncommitted) {
        this.uncommitted = uncommitted;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public String getConstraintDesc() throws TerminologyException, IOException {

        if (constraintDesc == null) {
            int constraintNid = ((I_ExtendByRefPartCidCidCid) getExtension().getMutablePart()).getC3id();
            Object component = Terms.get().getComponent(constraintNid);
            if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                I_GetConceptData thisConstraint = (I_GetConceptData) component;
                try {
                    I_DescriptionTuple thisConstraintDesc =
                            thisConstraint.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
                    if (thisConstraintDesc.getText() == null) {
                        constraintDesc = thisConstraint.toString();
                    } else {
                        constraintDesc = thisConstraintDesc.getText().toLowerCase();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                I_DescriptionVersioned thisConstraint = (I_DescriptionVersioned) component;
                try {
                    List<I_DescriptionTuple> matchingTuples = new ArrayList<I_DescriptionTuple>();
                    thisConstraint.addTuples(aceConfig.getAllowedStatus(), 
                                null, aceConfig.getViewPositionSetReadOnly(), matchingTuples, 
                                aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                            
                    if (matchingTuples.size() == 0) {
                        constraintDesc = thisConstraint.toString();
                    } else {
                        constraintDesc = matchingTuples.get(0).getText().toLowerCase();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return constraintDesc;
    }

    public String getClauseDesc() throws TerminologyException, IOException {
        if (clauseDesc == null) {
        	I_GetConceptData thisClause = Terms.get().getConcept(((I_ExtendByRefPartCidCid) getExtension().getMutablePart()).getC2id());
            try {
                I_DescriptionTuple thisClauseDesc =
                        thisClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
                clauseDesc = thisClauseDesc.getText().toLowerCase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return clauseDesc;
    }

    public int getTruthId() {
        if (truthId == Integer.MAX_VALUE) {
            truthId = ((I_ExtendByRefPartCidCid) getExtension().getMutablePart()).getC1id();
        }
        return truthId;
    }

    public String getTruthDesc() throws TerminologyException, IOException {
        if (truthDesc == null) {
        	I_GetConceptData thisTruth = Terms.get().getConcept(truthId);
            I_DescriptionTuple thisTruthDesc;
            try {
                thisTruthDesc = thisTruth.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            truthDesc = thisTruthDesc.getText().toLowerCase();
        }
        return truthDesc;
    }

    @SuppressWarnings("unchecked")
    public I_ExtendByRefVersion getExtension() {
        try {
            if (extension == null) {
                List<I_ExtendByRefVersion> tupleList = (List<I_ExtendByRefVersion>) ((I_ExtendByRef) this.userObject).getTuples(
                    aceConfig.getAllowedStatus(), aceConfig.getViewPositionSetReadOnly(), 
                    aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                if (tupleList.size() > 0) {
                    extension = tupleList.get(tupleList.size() - 1);
                } else {
                    tupleList = (List<I_ExtendByRefVersion>) ((I_ExtendByRef) this.userObject).getTuples(null, 
                        aceConfig.getViewPositionSetReadOnly(),
                        aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                    if (tupleList.size() > 0) {
                        extension = tupleList.get(tupleList.size() - 1);
                    }
                }
            }
            return extension;
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public int compareTo(RefsetSpecTreeNode o) {
        try {
            if (this.userObject instanceof I_ExtendByRef && o.userObject instanceof I_ExtendByRef) {
                I_ExtendByRefVersion thisExt = getExtension();
                I_ExtendByRefVersion otherExt = o.getExtension();
                if (thisExt == otherExt) {
                    return 0;
                }
                if (thisExt == null) {
                    return 1;
                }
                if (otherExt == null) {
                    return -1;
                }
                try {
					switch (EConcept.REFSET_TYPES.nidToType(thisExt.getCore().getTypeId())) {
					case CID_CID:

					    switch (EConcept.REFSET_TYPES.nidToType(otherExt.getCore().getTypeId())) {
					    case CID_CID:
					        int comparison = compareTruth(o);
					        if (comparison != 0) {
					            return comparison;
					        }
					        return compareClause(o);
					    case CID_CID_CID:
					        return 1;
					    case CID_CID_STR:
					        return 1;
					    default:
					        break;
					    }

					    break;

					case CID_CID_CID:
					    switch (EConcept.REFSET_TYPES.nidToType(otherExt.getCore().getTypeId())) {
					    case CID_CID:
					        return -1;
					    case CID_CID_CID:
					        int comparison = compareTruth(o);
					        if (comparison != 0) {
					            return comparison;
					        }
					        comparison = compareClause(o);
					        if (comparison != 0) {
					            return comparison;
					        }
					        return this.getConstraintDesc().compareTo(o.getClauseDesc());
					    case CID_CID_STR:
					        return -1;
					    default:
					        break;
					    }

					    break;
					case CID_CID_STR:
					    switch (EConcept.REFSET_TYPES.nidToType(otherExt.getCore().getTypeId())) {
					    case CID_CID:
					        return -1;
					    case CID_CID_CID:
					        return 1;
					    case CID_CID_STR:
					        int comparison = compareTruth(o);
					        if (comparison != 0) {
					            return comparison;
					        }
					        comparison = compareClause(o);
					        if (comparison != 0) {
					            return comparison;
					        }
					        return compareString(thisExt, otherExt);
					    default:
					        break;
					    }

					    break;
					default:
					    break;
					}
				} catch (TerminologyException e) {
					throw new RuntimeException(e);
				}

            }
            return this.userObject.toString().compareTo(o.userObject.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int compareString(I_ExtendByRefVersion thisExt, I_ExtendByRefVersion otherExt) throws IOException {
        String thisExtStr = ((I_ExtendByRefPartCidCidString) thisExt.getMutablePart()).getStringValue();
        String otherExtStr = ((I_ExtendByRefPartCidCidString) otherExt.getMutablePart()).getStringValue();
        return thisExtStr.toLowerCase().compareTo(otherExtStr.toLowerCase());
    }


    private int compareClause(RefsetSpecTreeNode o) throws IOException, TerminologyException {
        return getClauseDesc().compareTo(o.getClauseDesc());
    }

    private int compareTruth(RefsetSpecTreeNode o) throws IOException, TerminologyException {
        if (getTruthId() != o.getTruthId()) {
            if (getTruthDesc().contains("true") && o.getTruthDesc().contains("true") == false) {
                return -1;
            } else if (getTruthDesc().contains("false") && o.getTruthDesc().contains("false") == false) {
                return 1;
            }
            return getTruthDesc().compareTo(o.getTruthDesc());
        }
        return 0;
    }

    public RefsetSpecTreeNode(I_ConfigAceFrame aceConfig) {
        super();
        this.aceConfig = aceConfig;
    }

    public RefsetSpecTreeNode(Object userObject, boolean allowsChildren, I_ConfigAceFrame aceConfig) {
        super(userObject, allowsChildren);
        this.aceConfig = aceConfig;
    }

    public RefsetSpecTreeNode(Object userObject, I_ConfigAceFrame aceConfig) {
        super(userObject);
        this.aceConfig = aceConfig;
    }

    @SuppressWarnings("unchecked")
    public boolean sortChildren() {
        if (children != null) {
            Collections.sort(children);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public Vector<RefsetSpecTreeNode> getChildren() {
        return children;
    }

    private String htmlRendering;
    public String getHtmlRendering() {
        return htmlRendering;
    }
    
    public void setHtmlRendering(String htmlRendering) {
        this.htmlRendering = htmlRendering;
    }

    public boolean isInactive() {
        return inactive;
    }
}
