/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk;

// TODO: Auto-generated Javadoc
/**
 * The Interface I_AddCommonTestingProps.
 */
public interface I_AddCommonTestingProps {
	
	/**
	 * Is published.
	 *
	 * @return the boolean
	 */
	public Boolean isPublished();
	
	/**
	 * Sets the published.
	 *
	 * @param published the new published
	 */
	public void setPublished(Boolean published);
	
	/**
	 * Is new component.
	 *
	 * @return the boolean
	 */
	public Boolean isNewComponent();
	
	/**
	 * Sets the new component.
	 *
	 * @param newComponent the new new component
	 */
	public void setNewComponent(Boolean newComponent);
	
	/**
	 * Is changed component.
	 *
	 * @return the boolean
	 */
	public Boolean isChangedComponent();
	
	/**
	 * Sets the changed component.
	 *
	 * @param changedComponent the new changed component
	 */
	public void setChangedComponent(Boolean changedComponent);

	/**
	 * Is retired.
	 *
	 * @return the boolean
	 */
	public Boolean isRetired();
	
	/**
	 * Sets the retired.
	 *
	 * @param retired the new retired
	 */
	public void setRetired(Boolean retired);

}
