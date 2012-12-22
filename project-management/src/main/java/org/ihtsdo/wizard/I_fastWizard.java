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
package org.ihtsdo.wizard;

import java.util.HashMap;

/**
 * The Interface I_fastWizard.
 */
public interface I_fastWizard {

    /**
     * Gets the data.
     *
     * @return the data
     * @throws Exception the exception
     */
    HashMap<String, Object> getData() throws Exception;

    /**
     * Sets the key.
     *
     * @param key the new key
     */
    void setKey(String key);
}
