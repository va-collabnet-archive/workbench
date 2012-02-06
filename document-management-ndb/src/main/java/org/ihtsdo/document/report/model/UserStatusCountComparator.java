/*
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
package org.ihtsdo.document.report.model;

import java.util.Comparator;

/**
 * The Class UserStatusCountComparator.
 */
public class UserStatusCountComparator implements Comparator<UserStatusCount> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(UserStatusCount e1, UserStatusCount e2) {
		int result = 0;
		result = e1.getDate().compareTo(e2.getDate());
		if (result != 0) {
			return result;
		}else{
			result = e1.getUserName().compareTo(e2.getUserName());
			if(result != 0){
				return result;
			}else{
				result = e1.getStatus().compareTo(e2.getStatus());
			}
		}
		return result;
	}

}
