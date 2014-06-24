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
package org.ihtsdo.tk.dto.concept.component.identifier;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

/**
 * The Enum IDENTIFIER_PART_TYPES lists the potential types of identifiers.
 */
public enum IDENTIFIER_PART_TYPES {

    /**
     * A long type of identifier.
     */
    LONG(1),
    /**
     * A String type of identifier.
     */
    STRING(2),
    /**
     * A UUID type of identifier.
     */
    UUID(3);
    /**
     * The int value representing the identifier type.
     */
    private int externalPartTypeToken;

    /**
     * Instantiates a new type of identifier.
     *
     * @param externalPartTypeToken int value representing the identifier type
     */
    IDENTIFIER_PART_TYPES(int externalPartTypeToken) {
        this.externalPartTypeToken = externalPartTypeToken;
    }

    /**
     * Writes identifier specified in the
     * <code>externalPartTypeToken</code> to the external source.
     *
     * @param output the data output object that writes to the external source
     * @throws IOException signals that an I/O exception has occurred
     */
    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalPartTypeToken);
    }

    /**
     * Gets the type of identifier based on the class assignable from
     * <code>c</code>. Supports
     * <code>long</code, <code>String</code>, and
     * <code>UUID</code>
     *
     * @param c the object from which to find the type
     * @return the IDENTIFIER_PART_TYPES representing the type of the specified
     * object
     */
    public static IDENTIFIER_PART_TYPES getType(Class<?> c) {
        if (Long.class.isAssignableFrom(c)) {
            return LONG;
        } else if (String.class.isAssignableFrom(c)) {
            return STRING;
        } else if (UUID.class.isAssignableFrom(c)) {
            return UUID;
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Read type based on the <code>input</code>. Supports
     * <code>long</code, <code>String</code>, and
     * <code>UUID</code>
     *
     * @param input the data input specifying the identifier from which to find the type
     * @return the IDENTIFIER_PART_TYPES representing the type of the specified
     * object
     * @throws IOException signals that an I/O exception has occurred
     */
    public static IDENTIFIER_PART_TYPES readType(DataInput input) throws IOException {
        byte typeByte = input.readByte();
        switch (typeByte) {
            case 1:
                return LONG;
            case 2:
                return STRING;
            case 3:
                return UUID;
        }
        throw new UnsupportedOperationException("Can't find byte: " + typeByte);
    }
}