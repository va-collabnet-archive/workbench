/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.spec.ConceptSpec;
 
/**
 *
 * @author kec
 */
public class IsMissingDescForDialectEvaluatorDefinition implements EvaluatorDefinition {

   public static class IsMissingDescForEvaluator extends BaseEvaluator {

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

      public IsMissingDescForEvaluator() {
         super();
         // No arg constructor for serialization. 
      }

      public IsMissingDescForEvaluator(final ValueType type, final boolean isNegated) {
         super(type, isNegated ? IsMissingDescForDialectEvaluatorDefinition.NOT_IS_MISSING_DESC_FOR
                 : IsMissingDescForDialectEvaluatorDefinition.IS_MISSING_DESC_FOR);
      }

      @Override
      public boolean evaluate(InternalWorkingMemory workingMemory,
              InternalReadAccessor extractor, Object object, FieldValue value) {
         return testMissingDescForDialect(object, value.getValue());
      }

      @Override
      public boolean evaluate(InternalWorkingMemory workingMemory,
              InternalReadAccessor leftExtractor, Object left,
              InternalReadAccessor rightExtractor, Object right) {
         final Object value1 = leftExtractor.getValue(workingMemory, left);
         final Object value2 = rightExtractor.getValue(workingMemory, right);

         return testMissingDescForDialect(value1, value2);
      }

      /**
       * 
       * @param value1 Concept to test descriptions for dialects
       * @param value2 Concept representing the dialect to check
       * @return 
       */
      private boolean testMissingDescForDialect(final Object value1, final Object value2) {

         //value1 this could be concept VersionBI or conceptFact
         //value2 this could be concept VersionBI or conceptFact

         ConceptVersionBI conceptToTest = null;
         if (ConceptVersionBI.class.isAssignableFrom(value1.getClass())) {
            conceptToTest = (ConceptVersionBI) value1;
         } else if (ConceptFact.class.isAssignableFrom(value1.getClass())) {
            conceptToTest = ((ConceptFact) value1).getConcept();
         } else {
            throw new UnsupportedOperationException("Can't convert: " + value1);
         }
         ViewCoordinate coordinate = conceptToTest.getViewCoordinate();

         ConceptVersionBI dialectCV = null;
 

         if (ConceptVersionBI.class.isAssignableFrom(value2.getClass())) {
            dialectCV = (ConceptVersionBI) value2;
         } else if (ConceptSpec.class.isAssignableFrom(value2.getClass())) {
            try {
               dialectCV = ((ConceptSpec) value2).get(coordinate);
            } catch (IOException ex) {
               Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
               return false;
            }
         } else if (ConceptFact.class.isAssignableFrom(value2.getClass())) {
            ConceptFact fact = (ConceptFact) value2;
            dialectCV = (ConceptVersionBI) fact.getConcept();
         }
         int dialectNid = dialectCV.getNid();
         try {
            boolean missingDescForDialect = false;
            for (DescriptionVersionBI desc : conceptToTest.getDescsActive()) {
               if (DialectHelper.isMissingDescForDialect(desc, dialectNid, coordinate)) {
                  missingDescForDialect = true;
                  break;
               }
            }
            return this.getOperator().isNegated() ^ (missingDescForDialect);
         } catch (UnsupportedDialectOrLanguage ex) {
            Logger.getLogger(
                    IsMissingDescForDialectEvaluatorDefinition.class.getName()).
                    log(Level.SEVERE, "Cannot test secondary to "
                    + "unsupported dialect: " + dialectCV, ex);
            return false;
         } catch (ContraditionException ex) {
            Logger.getLogger(
                    IsMissingDescForDialectEvaluatorDefinition.class.getName()).
                    log(Level.SEVERE, "Cannot test secondary to contradiction", ex);
            return false;
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      @Override
      public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
              VariableContextEntry context, Object right) {
         return testMissingDescForDialect(((ObjectVariableContextEntry) context).left, right);
      }

      @Override
      public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
              VariableContextEntry context, Object left) {
         return testMissingDescForDialect(left, ((ObjectVariableContextEntry) context).right);
      }

      @Override
      public String toString() {
         return "IsMissingDescForDialect isMissingDescForDialect";
      }
   }
   public static Operator IS_MISSING_DESC_FOR = null;
   public static Operator NOT_IS_MISSING_DESC_FOR = null;
   private static String[] SUPPORTED_IDS = null;

   private static void init() {
      if (IS_MISSING_DESC_FOR == null) {
         IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry("isMissingDescFor", false);
         NOT_IS_MISSING_DESC_FOR = Operator.addOperatorToRegistry(IS_MISSING_DESC_FOR.getOperatorString(), true);
         SUPPORTED_IDS = new String[]{IS_MISSING_DESC_FOR.getOperatorString()};
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
         evaluator[index] = new IsMissingDescForEvaluator(type, isNegated);
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
