/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.document.task;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * The Class ProjectReportsBeanInfo.
 */
public class WorklistMembersXlsReportBeanInfo extends SimpleBeanInfo {

	public WorklistMembersXlsReportBeanInfo() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
	 */
	public PropertyDescriptor[] getPropertyDescriptors() {

		PropertyDescriptor rv[] =
		{};
		return rv;

	}        

	/* (non-Javadoc)
	 * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
	 */
	public BeanDescriptor getBeanDescriptor() {
		BeanDescriptor bd = new BeanDescriptor(WorklistMembersXlsReport.class);
		bd.setDisplayName("<html><font color='green'><center>Worklist member by status<BR>Excel report");
		return bd;
	}

}
