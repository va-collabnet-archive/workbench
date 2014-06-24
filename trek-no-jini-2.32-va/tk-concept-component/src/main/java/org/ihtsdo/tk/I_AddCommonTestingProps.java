/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk;

/**
 * The Interface I_AddCommonTestingProps contains methods for interacting with
 * terminology generic test properties.
 */
public interface I_AddCommonTestingProps {

    /**
     * Tests if the component is set as published.
     *
     * @return <code>true</code> if the component is published
     */
    public Boolean isPublished();

    /**
     * Sets the component as published.
     *
     * @param published set to <code>true</code> to indicate the component is
     * published
     */
    public void setPublished(Boolean published);

    /**
     * Tests if the component is set as new.
     *
     * @return <code>true</code</code> if the component is new
     */
    public Boolean isNewComponent();

    /**
     * Sets the component as new.
     *
     * @param newComponent set to <code>true</code> to indicate the component is
     * new
     */
    public void setNewComponent(Boolean newComponent);

    /**
     * Tests if the component is set as changed.
     *
     * @return <code>true</code</code> if the component is changed
     */
    public Boolean isChangedComponent();

    /**
     * Sets the component as changed.
     *
     * @param changedComponent set to <code>true</code> to indicate the
     * component has changed
     */
    public void setChangedComponent(Boolean changedComponent);

    /**
     * Tests if the component is set as retired.
     *
     * @return <code>true</code</code> if the component is retired
     */
    public Boolean isRetired();

    /**
     * Sets the component as retired.
     *
     * @param retired set to <code>true</code> to indicate the component is
     * retired
     */
    public void setRetired(Boolean retired);
}
