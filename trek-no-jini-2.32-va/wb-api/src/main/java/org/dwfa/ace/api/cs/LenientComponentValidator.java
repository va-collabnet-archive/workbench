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
package org.dwfa.ace.api.cs;

/**
 * Validates a component. Warns if the component parts are different but not
 * part of the change,
 * fails if parts are different that are part of the change.
 * 
 * @author Dion McMurtrie
 * 
 */
public class LenientComponentValidator extends ComponentValidator {

    public LenientComponentValidator() {
        super();
        super.setStrictMode(false);
        super.setTimeLenient(true);
    }
}
