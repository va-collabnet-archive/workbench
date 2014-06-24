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
package org.ihtsdo.tk.api.blueprint;


/**
 * The Class InvalidCAB represents an exception that occurs when a field of the blueprint
 * contains an invalid value.
 *
 */
public class InvalidCAB extends Exception {

    
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new invalid cab.
     *
     * @param throwable the throwable
     */
    public InvalidCAB(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new invalid cab.
     *
     * @param string the message associated with the exception
     * @param throwable the throwable
     */
    public InvalidCAB(String string, Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Instantiates a new invalid cab.
     *
     * @param string the message associated with the exception
     */
    public InvalidCAB(String string) {
        super(string);
    }

    /**
     * Instantiates a new invalid cab.
     */
    public InvalidCAB() {
    }
}
