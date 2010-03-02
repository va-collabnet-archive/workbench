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
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;

public class RefsetSpecTreeNode extends DefaultMutableTreeNode implements Comparable<RefsetSpecTreeNode> {

    private I_ConfigAceFrame aceConfig;
    private I_ExtendByRefVersion extension;
    private int truthId = Integer.MAX_VALUE;
    private String truthDesc;
    private String clauseDesc;
    private String constraintDesc;

    public String getConstraintDesc() throws TerminologyException, IOException {
        if (constraintDesc == null) {
            I_GetConceptData thisConstraint = Terms.get().getConcept(((I_ExtendRefPartCidCidCid) getExtension().getMutablePart()).getC3id());
            try {
                I_DescriptionTuple thisConstraintDesc = thisConstraint.getDescTuple(
                    aceConfig.getTreeDescPreferenceList(), aceConfig);
                constraintDesc = thisConstraintDesc.getText().toLowerCase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return constraintDesc;
    }

    public String getClauseDesc() throws TerminologyException, IOException {
        if (clauseDesc == null) {
        	I_GetConceptData thisClause = Terms.get().getConcept(((I_ExtendByRefPartCidCid) getExtension().getMutablePart()).getC2id());
            try {
                I_DescriptionTuple thisClauseDesc = thisClause.getDescTuple(aceConfig.getTreeDescPreferenceList(),
                    aceConfig);
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

    public I_ExtendByRefVersion getExtension() {
        if (extension == null) {
            List<I_ExtendByRefVersion> tupleList = (List<I_ExtendByRefVersion>) ((I_ExtendByRef) this.userObject).getTuples(
                aceConfig.getAllowedStatus(), aceConfig.getViewPositionSetReadOnly(), true);
            if (tupleList.size() > 0) {
                extension = tupleList.get(tupleList.size() - 1);
            } else {
                tupleList = (List<I_ExtendByRefVersion>) ((I_ExtendByRef) this.userObject).getTuples(null, aceConfig.getViewPositionSetReadOnly(),
                    true);
                if (tupleList.size() > 0) {
                    extension = tupleList.get(tupleList.size() - 1);
                }
            }
        }
        return extension;
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
					        return compareConstraint(thisExt, otherExt);
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

    private int compareConstraint(I_ExtendByRefVersion thisExt, I_ExtendByRefVersion otherExt) throws IOException, TerminologyException {
    	I_GetConceptData thisClause = Terms.get().getConcept(((I_ExtendRefPartCidCidCid) thisExt.getMutablePart()).getC3id());
    	I_GetConceptData otherClause = Terms.get().getConcept(((I_ExtendRefPartCidCidCid) otherExt.getMutablePart()).getC3id());
        I_DescriptionTuple thisClauseDesc = thisClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
        I_DescriptionTuple otherClauseDesc = otherClause.getDescTuple(aceConfig.getTreeDescPreferenceList(), aceConfig);
        if (thisClauseDesc == null || otherClauseDesc == null) {
            if (thisClauseDesc == otherClauseDesc) {
                return thisClause.toString().compareTo(otherClause.toString());
            }
            if (thisClauseDesc == null) {
                return 1;
            } else {
                return -1;
            }
        }
        return thisClauseDesc.getText().toLowerCase().compareTo(otherClauseDesc.getText().toLowerCase());
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

    public boolean sortChildren() {
        if (children != null) {
            Collections.sort(children);
            return true;
        }
        return false;
    }

    public Vector<RefsetSpecTreeNode> getChildren() {
        return children;
    }

}
