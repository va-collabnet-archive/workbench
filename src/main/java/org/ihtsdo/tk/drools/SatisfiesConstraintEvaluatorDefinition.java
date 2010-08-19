package org.ihtsdo.tk.drools;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.constraint.ConstraintBI;
import org.ihtsdo.tk.api.constraint.ConstraintCheckType;

public class SatisfiesConstraintEvaluatorDefinition implements EvaluatorDefinition {
	
	private static final String DEFAULT_PARAMETERS = "x,e,e";

	public static class SatisfiesConstraintEvaluator extends BaseEvaluator {
		ConstraintCheckType subjectCheck;
		ConstraintCheckType propertyCheck;
		ConstraintCheckType valueCheck;
		
		public SatisfiesConstraintEvaluator(final ValueType type, final boolean isNegated, String parameterText) {
			super(type, isNegated ? IsKindOfEvaluatorDefinition.NOT_IS_KIND_OF : IsKindOfEvaluatorDefinition.IS_KIND_OF);
			if (parameterText == null) {
				parameterText = DEFAULT_PARAMETERS;
			}
		      String [] params = parameterText.toLowerCase().replace(',', ' ').split("\\s+");
		      subjectCheck = ConstraintCheckType.get(params[0]);
		      propertyCheck = ConstraintCheckType.get(params[1]);
		      valueCheck = ConstraintCheckType.get(params[2]);
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor extractor, Object object, FieldValue value) {
			return testSatisfiesConstraint(object, value.getValue());
		}

		@Override
		public boolean evaluate(InternalWorkingMemory workingMemory,
				InternalReadAccessor leftExtractor, Object left,
				InternalReadAccessor rightExtractor, Object right) {
			final Object value1 = leftExtractor.getValue(workingMemory, left);
			final Object value2 = rightExtractor.getValue(workingMemory, right);

			return testSatisfiesConstraint(value1, value2);
		}

		private boolean testSatisfiesConstraint(final Object value1, final Object value2) {
			try {
				ConceptVersionBI conceptVersion = (ConceptVersionBI) value1;
				ConstraintBI constraint = (ConstraintBI) value2;				
				return this.getOperator().isNegated() ^ (conceptVersion.satisfies(constraint, subjectCheck, propertyCheck, valueCheck));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
				VariableContextEntry context, Object right) {
			return testSatisfiesConstraint(((ObjectVariableContextEntry) context).left, right);
		}

		@Override
		public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
				VariableContextEntry context, Object left) {
			return testSatisfiesConstraint(left, ((ObjectVariableContextEntry) context).right);
		}

		@Override
		public String toString() {
			return "SatisfiesConstraint satisfiesConstraint";
		}

	}

	public static final Operator SATISFIES_CONSTRAINT = Operator.addOperatorToRegistry("satisfiesConstraint", false);
	public static final Operator NOT_SATISFIES_CONSTRAINT = Operator.addOperatorToRegistry(SATISFIES_CONSTRAINT.getOperatorString(), true);
	private static final String[] SUPPORTED_IDS = { SATISFIES_CONSTRAINT.getOperatorString() };

	@Override
	public Evaluator getEvaluator(ValueType type, Operator operator) {
		return this.getEvaluator(type, operator.getOperatorString(), operator.isNegated(), DEFAULT_PARAMETERS);
	}

	@Override
	public Evaluator getEvaluator(ValueType type, Operator operator, String parameterText) {
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
		return new SatisfiesConstraintEvaluator(type, isNegated, parameterText);
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
		// Nothing to do...;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// Nothing to do...;
	}
	
}
