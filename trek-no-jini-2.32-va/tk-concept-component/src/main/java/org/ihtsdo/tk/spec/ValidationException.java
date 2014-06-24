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
package org.ihtsdo.tk.spec;

import java.io.IOException;

/**
 * The Class ValidationException represents an exception in validating a type of
 * terminology spec. Validation involves check the database for the specified
 * uuids and verify that the elements of the specification match the
 * concept/component found.
 */
public class ValidationException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new validation exception.
     */
    public ValidationException() {
    }

    /**
     * Instantiates a new validation exception.
     *
     * @param message the message to display
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new validation exception.
     *
     * @param throwable the throwable
     */
    public ValidationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new validation exception.
     *
     * @param message the message to display
     * @param throwable the throwable
     */
    public ValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
