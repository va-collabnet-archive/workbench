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
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

public class IsMemberOfWithTypeEvaluatorDefinition implements EvaluatorDefinition {

    public static class IsMemberOfWithTypeEvaluator extends BaseEvaluator {

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

        public IsMemberOfWithTypeEvaluator() {
            super();
            // No arg constructor for serialization. 
        }

        public IsMemberOfWithTypeEvaluator(final ValueType type, final boolean isNegated) {
            super(type, isNegated ? IsMemberOfWithTypeEvaluatorDefinition.NOT_IS_MEMBER_OF_WITH_TYPE : IsMemberOfWithTypeEvaluatorDefinition.IS_MEMBER_OF_WITH_TYPE);
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

            boolean isMember = false;
            /*
             * value1 (concept): this could be concept VersionBI, ConceptFact, or DescriptionFact
             * value2 (refset, type): this is an array of concept specs 
             *  where ConceptSpec[0] = the concept spec of the refex
             *  and ConceptSpec[1] = the concept spec of the type being tested
             */
            
            Object[] refexTypeArray = (Object[]) value2;
            Object refexThing = refexTypeArray[0];
            ConceptSpec refexConceptSpec = null;
            if(ConceptSpec.class.isAssignableFrom(refexThing.getClass())){
                refexConceptSpec = (ConceptSpec) refexTypeArray[0];
            }else{
                throw new UnsupportedOperationException("The first object in the array must be a ConceptSpec.");
            }
            Object typeThing = refexTypeArray[1];
            
            try {
                if (!Ts.get().hasUuid(refexConceptSpec.getLenient().getPrimUuid())) {
                    return false;
                }
            } catch (ValidationException ex) {
                //do nothing
            } catch (IOException ex) {
                //do nothing
            }

            if (ConceptFact.class.isAssignableFrom(value1.getClass())) {
                isMember = testConcept((ConceptFact) value1, refexConceptSpec, typeThing);
            } else if (DescFact.class.isAssignableFrom(value1.getClass())) {
                DescFact dFact = (DescFact) value1;
                isMember = testDesc(dFact, refexConceptSpec, typeThing);
            } else {
                throw new UnsupportedOperationException("Can't convert: " + value1);
            }

            return this.getOperator().isNegated() ^ (isMember);

        }

        private boolean testDesc(DescFact dFact, ConceptSpec refexConceptSpec, final Object typeThing) {
            boolean member = false;
            
            try {
                DescriptionVersionBI desc = dFact.getComponent();
                ViewCoordinate vc = dFact.getVc();

                int evalRefsetNid = refexConceptSpec.getStrict(vc).getNid();
                
                Collection<? extends RefexVersionBI<?>> refexes = 
                        desc.getRefexMembersActive(vc, evalRefsetNid);

                if (refexes != null && refexes.size() > 0) {
                    member = true;
                }
                
                if(member){

                    if(ConceptSpec.class.isAssignableFrom(typeThing.getClass())){
                        ConceptSpec conceptSpecType = (ConceptSpec) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexNidVersionBI conceptRefex = (RefexNidVersionBI) refex;
                            int conceptNid = conceptSpecType.getStrict(vc).getConceptNid();
                            int nid = conceptSpecType.getStrict(vc).getNid();
                            ConceptChronicleBI concept = Ts.get().getConcept(conceptRefex.getNid1());
                            if(conceptRefex.getNid1() == conceptSpecType.getStrict(vc).getConceptNid()){
                                return true;
                            }
                        }
                    }else if(String.class.isAssignableFrom(typeThing.getClass())){
                        String stringType = (String) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexStringVersionBI conceptRefex = (RefexStringVersionBI) refex;
                            if(conceptRefex.getString1().equals(stringType)){
                                return true;
                            }
                        }
                    }else if(Integer.class.isAssignableFrom(typeThing.getClass())){
                        Integer integerType = (Integer) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexIntVersionBI conceptRefex = (RefexIntVersionBI) refex;
                            if(conceptRefex.getInt1() == integerType.intValue()){
                                return true;
                            }
                        }
                    }else if(Boolean.class.isAssignableFrom(typeThing.getClass())){
                         Boolean booleanType = (Boolean) typeThing;
                         for(RefexVersionBI refex : refexes){
                            RefexBooleanVersionBI conceptRefex = (RefexBooleanVersionBI) refex;
                            if(conceptRefex.getBoolean1() == booleanType.booleanValue()){
                                return true;
                            }
                        }
                    }else{
                        throw new UnsupportedOperationException("Can only handle refex type of concept, string, integer, and boolean. "
                                 + "Given type did not match any of these");
                    }
                }
                
                
                return false;
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
                return member;
            }
        }

        private boolean testConcept(ConceptFact conceptFact, ConceptSpec refexConceptSpec, final Object typeThing) {
            boolean member = false;
            
            try {
                ConceptVersionBI conceptVersion = conceptFact.getConcept();
                ViewCoordinate vc = conceptFact.getVc();

                int evalRefsetNid =  refexConceptSpec.getStrict(vc).getNid();
                
                Collection<? extends RefexVersionBI<?>> refexes =
                        conceptVersion.getRefexMembersActive(vc, evalRefsetNid);

                if (refexes != null) {
                    member = true;
                }
                
                if(member){

                    if(ConceptSpec.class.isAssignableFrom(typeThing.getClass())){
                        ConceptSpec conceptSpecType = (ConceptSpec) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexNidVersionBI conceptRefex = (RefexNidVersionBI) refex;
                            if(conceptRefex.getNid1() == conceptSpecType.getStrict(vc).getConceptNid()){
                                return true;
                            }
                        }
                    }else if(String.class.isAssignableFrom(typeThing.getClass())){
                        String stringType = (String) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexStringVersionBI conceptRefex = (RefexStringVersionBI) refex;
                            if(conceptRefex.getString1().equals(stringType)){
                                return true;
                            }
                        }
                    }else if(Integer.class.isAssignableFrom(typeThing.getClass())){
                        Integer integerType = (Integer) typeThing;
                        for(RefexVersionBI refex : refexes){
                            RefexIntVersionBI conceptRefex = (RefexIntVersionBI) refex;
                            if(conceptRefex.getInt1() == integerType.intValue()){
                                return true;
                            }
                        }
                    }else if(Boolean.class.isAssignableFrom(typeThing.getClass())){
                         Boolean booleanType = (Boolean) typeThing;
                         for(RefexVersionBI refex : refexes){
                            RefexBooleanVersionBI conceptRefex = (RefexBooleanVersionBI) refex;
                            if(conceptRefex.getBoolean1() == booleanType.booleanValue()){
                                return true;
                            }
                        }
                    }else{
                        throw new UnsupportedOperationException("Can only handle refex type of concept, string, integer, and boolean. "
                                 + "Given type did not match any of these");
                    }
                }
                
                
                return false;
            } catch (IOException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "refset concept not found", e);
                return member;
            }
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
            return "IsMemberOfWithType isMemberOfWithType";
        }
    }
    public static Operator IS_MEMBER_OF_WITH_TYPE = null;
    public static Operator NOT_IS_MEMBER_OF_WITH_TYPE = null;
    private static String[] SUPPORTED_IDS = null;

    private static synchronized void init() {
        if (IS_MEMBER_OF_WITH_TYPE == null) {
            IS_MEMBER_OF_WITH_TYPE = Operator.addOperatorToRegistry("isMemberOfWithType", false);
            NOT_IS_MEMBER_OF_WITH_TYPE = Operator.addOperatorToRegistry(IS_MEMBER_OF_WITH_TYPE.getOperatorString(), true);
            SUPPORTED_IDS = new String[]{IS_MEMBER_OF_WITH_TYPE.getOperatorString()};
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
            evaluator[index] = new IsMemberOfWithTypeEvaluator(type, isNegated);
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
