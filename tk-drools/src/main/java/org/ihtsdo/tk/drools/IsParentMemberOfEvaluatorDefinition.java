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
package org.ihtsdo.tk.drools;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.base.BaseEvaluator;
import org.drools.base.ValueType;
import org.drools.base.evaluators.EvaluatorDefinition;
import org.drools.base.evaluators.Operator;
import org.drools.common.InternalWorkingMemory;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.FieldValue;
import org.drools.spi.InternalReadAccessor;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

public class IsParentMemberOfEvaluatorDefinition implements EvaluatorDefinition {

    public static class IsParentMemberOfEvaluator extends BaseEvaluator {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private static final int dataVersion = 1;

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            init();
            super.readExternal(in);
            int objDataVersion = in.readInt();
            if (objDataVersion == dataVersion) {
                // Nothing to do
            } else {
                throw new IOException("Can't handle dataversion: " + objDataVersion);
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeInt(dataVersion);
        }

        public IsParentMemberOfEvaluator() {
            super();
            // No arg constructor for serialization. 
        }

        public IsParentMemberOfEvaluator(final ValueType type, final boolean isNegated) {
            super(type, isNegated ? IsParentMemberOfEvaluatorDefinition.NOT_IS_PARENT_MEMBER_OF : IsParentMemberOfEvaluatorDefinition.IS_PARENT_MEMBER_OF);
        }

        @Override
        public boolean evaluate(InternalWorkingMemory workingMemory,
                InternalReadAccessor extractor, Object object, FieldValue value) {
            return testMemberOf(object, value.getValue());
        }

        @Override
        public boolean evaluate(InternalWorkingMemory workingMemory,
                InternalReadAccessor leftExtractor, Object left,
                InternalReadAccessor rightExtractor, Object right) {
            final Object value1 = leftExtractor.getValue(workingMemory, left);
            final Object value2 = rightExtractor.getValue(workingMemory, right);

            return testMemberOf(value1, value2);
        }

        private boolean testMemberOf(final Object value1, final Object value2) {
            try {

                //value1 (concept): this could be concept VersionBI or conceptFact
                //value2 (refset): this will be put in Refset.java (tk-arena-rules) as a ConceptSpec
                ConceptSpec refexConcept = (ConceptSpec) value2;

                try {
                    if (!Ts.get().hasUuid(refexConcept.getLenient().getPrimUuid())) {
                        return false;
                    }
                } catch (ValidationException ex) {
                    //do nothing
                } catch (IOException ex) {
                    //do nothing
                }

                ConceptVersionBI possibleMember = null;
                if (ConceptVersionBI.class.isAssignableFrom(value1.getClass())) {
                    possibleMember = (ConceptVersionBI) value1;
                } else if (ConceptFact.class.isAssignableFrom(value1.getClass())) {
                    possibleMember = ((ConceptFact) value1).getConcept();
                } else {
                    throw new UnsupportedOperationException("Can't convert: " + value1);
                }
                ViewCoordinate vc = possibleMember.getViewCoordinate();
                ConceptVersionBI possibleRefsetCV = null;
                ConceptSpec possibleRefset = null;
                Collection<? extends ConceptVersionBI> parents = possibleMember.getRelationshipsSourceTargetConceptsActiveIsa();

                int evalRefsetNid = 0;

                if (ConceptVersionBI.class.isAssignableFrom(value2.getClass())) {
                    possibleRefsetCV = (ConceptVersionBI) value2;
                    evalRefsetNid = possibleRefsetCV.getNid();
                } else if (ConceptSpec.class.isAssignableFrom(value2.getClass())) {
                    possibleRefset = (ConceptSpec) value2;
                    try {
                        evalRefsetNid = possibleRefset.getStrict(vc).getNid();
                    } catch (ValidationException ve) {
                        return false;
                    }
                } else if (ConceptFact.class.isAssignableFrom(value2.getClass())) {
                    ConceptFact fact = (ConceptFact) value2;
                    possibleRefsetCV = (ConceptVersionBI) fact.getConcept();
                    evalRefsetNid = possibleRefsetCV.getNid();
                }

                return this.getOperator().isNegated() ^ (testParentOf(evalRefsetNid, parents, possibleMember));
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                return this.getOperator().isNegated() ^ (false);
            } catch (ContradictionException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                return this.getOperator().isNegated() ^ (false);
            }
        }

        private boolean testParentOf(int evalRefsetNid, Collection<? extends ConceptVersionBI> parents, ConceptVersionBI possibleMember) {
            boolean parentMember = false;
            try {
                for (ConceptVersionBI parent : parents) {
                    parentMember = parent.isMember(evalRefsetNid);
                    if (parentMember) {
                        break;
                    } else if (!parentMember && !parent.getRelationshipsSourceTargetConceptsActiveIsa().isEmpty()) {
                        parentMember = testParentOf(evalRefsetNid, parent.getRelationshipsSourceTargetConceptsActiveIsa(), possibleMember);
                    }
                }
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
                return parentMember;
            } catch (ContradictionException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
                return parentMember;
            }

            return parentMember;
        }

        @Override
        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                VariableContextEntry context, Object right) {
            return testMemberOf(((ObjectVariableContextEntry) context).left, right);
        }

        @Override
        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                VariableContextEntry context, Object left) {
            return testMemberOf(left, ((ObjectVariableContextEntry) context).right);
        }

        @Override
        public String toString() {
            return "IsMemberOf isMemberOf";
        }
    }
    public static Operator IS_PARENT_MEMBER_OF = null;
    public static Operator NOT_IS_PARENT_MEMBER_OF = null;
    private static String[] SUPPORTED_IDS = null;

    private static synchronized void init() {
        if (IS_PARENT_MEMBER_OF == null) {
            IS_PARENT_MEMBER_OF = Operator.addOperatorToRegistry("isParentMemberOf", false);
            NOT_IS_PARENT_MEMBER_OF = Operator.addOperatorToRegistry(IS_PARENT_MEMBER_OF.getOperatorString(), true);
            SUPPORTED_IDS = new String[]{IS_PARENT_MEMBER_OF.getOperatorString()};
        }
    }

    static {
        init();
    }
    private Evaluator[] evaluator;

    @Override
    public Evaluator getEvaluator(ValueType type, Operator operator) {
        return this.getEvaluator(type, operator.getOperatorString(), operator.isNegated(), null);
    }

    @Override
    public Evaluator getEvaluator(ValueType type, Operator operator,
            String parameterText) {
        return this.getEvaluator(type, operator.getOperatorString(), operator.isNegated(), parameterText);
    }

    @Override
    public Evaluator getEvaluator(ValueType type, String operatorId,
            boolean isNegated, String parameterText) {
        return getEvaluator(type, operatorId, isNegated, parameterText, Target.FACT, Target.FACT);
    }

    @Override
    public Evaluator getEvaluator(ValueType type, String operatorId,
            boolean isNegated, String parameterText, Target leftTarget,
            Target rightTarget) {
        if (evaluator == null) {
            evaluator = new Evaluator[2];
        }
        int index = isNegated ? 0 : 1;
        if (evaluator[index] == null) {
            evaluator[index] = new IsParentMemberOfEvaluator(type, isNegated);
        }
        return evaluator[index];
    }

    @Override
    public String[] getEvaluatorIds() {
        return SUPPORTED_IDS;
    }

    @Override
    public Target getTarget() {
        return Target.FACT;
    }

    @Override
    public boolean isNegatable() {
        return true;
    }

    @Override
    public boolean supportsType(ValueType type) {
        return true;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        evaluator = (Evaluator[]) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(evaluator);
    }
}
