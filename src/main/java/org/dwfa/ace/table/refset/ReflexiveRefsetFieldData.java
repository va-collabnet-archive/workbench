package org.dwfa.ace.table.refset;

import java.lang.reflect.Method;
import java.util.List;

public class ReflexiveRefsetFieldData {
	
	public enum REFSET_FIELD_TYPE {
		CONCEPT_IDENTIFIER,
		COMPONENT_IDENTIFIER,
		STRING,
		VERSION
	}
	
	public enum INVOKE_ON_OBJECT_TYPE {
		IMMUTABLE, PART, COMPONENT, CONCEPT, CONCEPT_COMPONENT;
	}

	protected REFSET_FIELD_TYPE type;
	protected String columnName;
	protected int min;
	protected int pref;
	protected int max;
	protected boolean creationEditable;
	protected boolean updateEditable;
	protected Class<?> fieldClass;
	protected INVOKE_ON_OBJECT_TYPE invokeOnObjectType;
	protected Method readMethod;
	protected Method writeMethod;
	protected List<Object> readParamaters;

	public ReflexiveRefsetFieldData() {
	}

	public INVOKE_ON_OBJECT_TYPE getInvokeOnObjectType() {
		return invokeOnObjectType;
	}

	public void setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE invokeOnObjectType) {
		this.invokeOnObjectType = invokeOnObjectType;
	}

	public REFSET_FIELD_TYPE getType() {
		return type;
	}

	public void setType(REFSET_FIELD_TYPE type) {
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getPref() {
		return pref;
	}

	public void setPref(int pref) {
		this.pref = pref;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public boolean isCreationEditable() {
		return creationEditable;
	}

	public void setCreationEditable(boolean creationEditable) {
		this.creationEditable = creationEditable;
	}

	public boolean isUpdateEditable() {
		return updateEditable;
	}

	public void setUpdateEditable(boolean updateEditable) {
		this.updateEditable = updateEditable;
	}

	public Class<?> getFieldClass() {
		return fieldClass;
	}

	public void setFieldClass(Class<?> fieldClass) {
		this.fieldClass = fieldClass;
	}

	public Method getReadMethod() {
		return readMethod;
	}

	public void setReadMethod(Method readMethod) {
		this.readMethod = readMethod;
	}

	public Method getWriteMethod() {
		return writeMethod;
	}

	public void setWriteMethod(Method writeMethod) {
		this.writeMethod = writeMethod;
	}

	public List<Object> getReadParamaters() {
		return readParamaters;
	}

	public void setReadParamaters(List<Object> paramaters) {
		this.readParamaters = paramaters;
	}
}