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
package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Interface WfSearchFilterBI.
 */
public interface WfSearchFilterBI {
	
	/**
	 * Filter.
	 *
	 * @param instance the instance
	 * @return true, if successful
	 */
	public boolean filter(WfInstance instance);
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType();
}
