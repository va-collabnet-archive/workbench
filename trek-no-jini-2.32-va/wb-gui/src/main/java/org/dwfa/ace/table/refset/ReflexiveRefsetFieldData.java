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
package org.dwfa.ace.table.refset;

import java.lang.reflect.Method;
import java.util.List;

public class ReflexiveRefsetFieldData {
    
    public static ReflexiveRefsetFieldData getRowColumn() {
        ReflexiveRefsetFieldData rowColumn = new ReflexiveRefsetFieldData();
        rowColumn.setColumnName("row");
        rowColumn.setCreationEditable(false);
        rowColumn.setUpdateEditable(false);
        rowColumn.setFieldClass(Number.class);
        rowColumn.setMin(5);
        rowColumn.setPref(15);
        rowColumn.setMax(60);
        rowColumn.setType(REFSET_FIELD_TYPE.ROW);
        return rowColumn;
    }


    public enum REFSET_FIELD_TYPE {
        ROW,
        CONCEPT_IDENTIFIER,
        COMPONENT_IDENTIFIER,
        STRING,
        TIME,
        CHECK_BOX;
    }

    public enum INVOKE_ON_OBJECT_TYPE {
        IMMUTABLE,
        PART,
        COMPONENT,
        CONCEPT,
        CONCEPT_COMPONENT,
        PROMOTION_REFSET_PART,
        CHECK_BOX;
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
    protected Object[] readParamaters;

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

    public Object[] getReadParamaters() {
        return readParamaters;
    }

    public void setReadParamaters(List<Object> paramaters) {
        this.readParamaters = paramaters.toArray();
    }

    public void setReadParamaters(Object[] paramaters) {
        this.readParamaters = paramaters;
    }
}
