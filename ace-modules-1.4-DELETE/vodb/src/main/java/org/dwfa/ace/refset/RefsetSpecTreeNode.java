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
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.types.ConceptBean;

public class RefsetSpecTreeNode extends DefaultMutableTreeNode implements Comparable<RefsetSpecTreeNode> {

    private I_ConfigAceFrame aceConfig;
    private I_ThinExtByRefTuple extension;
    private int truthId = Integer.MAX_VALUE;
    private String truthDesc;
    private String clauseDesc;
    private String constraintDesc;

    public String getConstraintDesc() {
        if (constraintDesc == null) {
            ConceptBean thisConstraint = ConceptBean.get(((I_ThinExtByRefPartConceptConceptConcept) getExtension().getPart()).getC3id());
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

    public String getClauseDesc() {
        if (clauseDesc == null) {
            ConceptBean thisClause = ConceptBean.get(((I_ThinExtByRefPartConceptConcept) getExtension().getPart()).getC2id());
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
            truthId = ((I_ThinExtByRefPartConceptConcept) getExtension().getPart()).getC1id();
        }
        return truthId;
    }

    public String getTruthDesc() {
        if (truthDesc == null) {
            ConceptBean thisTruth = ConceptBean.get(truthId);
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

    public I_ThinExtByRefTuple getExtension() {
        if (extension == null) {
            List<I_ThinExtByRefTuple> tupleList = ((I_ThinExtByRefVersioned) this.userObject).getTuples(
                aceConfig.getAllowedStatus(), aceConfig.getViewPositionSet(), true);
            if (tupleList.size() > 0) {
                extension = tupleList.get(tupleList.size() - 1);
            } else {
                tupleList = ((I_ThinExtByRefVersioned) this.userObject).getTuples(null, aceConfig.getViewPositionSet(),
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
            if (this.userObject instanceof I_ThinExtByRefVersioned && o.userObject instanceof I_ThinExtByRefVersioned) {
                I_ThinExtByRefTuple thisExt = getExtension();
                I_ThinExtByRefTuple otherExt = o.getExtension();
                if (thisExt == otherExt) {
                    return 0;
                }
                if (thisExt == null) {
                    return 1;
                }
                if (otherExt == null) {
                    return -1;
                }
                switch (ThinExtBinder.getExtensionType(thisExt.getCore())) {
                case CONCEPT_CONCEPT:

                    switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
                    case CONCEPT_CONCEPT:
                        int comparison = compareTruth(o);
                        if (comparison != 0) {
                            return comparison;
                        }
                        return compareClause(o);
                    case CONCEPT_CONCEPT_CONCEPT:
                        return 1;
                    case CONCEPT_CONCEPT_STRING:
                        return 1;
                    default:
                        break;
                    }

                    break;

                case CONCEPT_CONCEPT_CONCEPT:
                    switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
                    case CONCEPT_CONCEPT:
                        return -1;
                    case CONCEPT_CONCEPT_CONCEPT:
                        int comparison = compareTruth(o);
                        if (comparison != 0) {
                            return comparison;
                        }
                        comparison = compareClause(o);
                        if (comparison != 0) {
                            return comparison;
                        }
                        return compareConstraint(thisExt, otherExt);
                    case CONCEPT_CONCEPT_STRING:
                        return -1;
                    default:
                        break;
                    }

                    break;
                case CONCEPT_CONCEPT_STRING:
                    switch (ThinExtBinder.getExtensionType(otherExt.getCore())) {
                    case CONCEPT_CONCEPT:
                        return -1;
                    case CONCEPT_CONCEPT_CONCEPT:
                        return 1;
                    case CONCEPT_CONCEPT_STRING:
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

            }
            return this.userObject.toString().compareTo(o.userObject.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int compareString(I_ThinExtByRefTuple thisExt, I_ThinExtByRefTuple otherExt) throws IOException {
        String thisExtStr = ((I_ThinExtByRefPartConceptConceptString) thisExt.getPart()).getStr();
        String otherExtStr = ((I_ThinExtByRefPartConceptConceptString) otherExt.getPart()).getStr();
        return thisExtStr.toLowerCase().compareTo(otherExtStr.toLowerCase());
    }

    private int compareConstraint(I_ThinExtByRefTuple thisExt, I_ThinExtByRefTuple otherExt) throws IOException {
        ConceptBean thisClause = ConceptBean.get(((I_ThinExtByRefPartConceptConceptConcept) thisExt.getPart()).getC3id());
        ConceptBean otherClause = ConceptBean.get(((I_ThinExtByRefPartConceptConceptConcept) otherExt.getPart()).getC3id());
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

    private int compareClause(RefsetSpecTreeNode o) throws IOException {
        return getClauseDesc().compareTo(o.getClauseDesc());
    }

    private int compareTruth(RefsetSpecTreeNode o) throws IOException {
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
