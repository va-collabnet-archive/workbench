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
package org.ihtsdo.tk.dto.concept.component.identifier;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Enum IDENTIFIER_PART_TYPES.
 */
public enum IDENTIFIER_PART_TYPES {
    
    /** The long. */
    LONG(1), 
 /** The string. */
 STRING(2), 
 /** The uuid. */
 UUID(3);

    /** The external part type token. */
    private int externalPartTypeToken;

    /**
     * Instantiates a new identifier part types.
     *
     * @param externalPartTypeToken the external part type token
     */
    IDENTIFIER_PART_TYPES(int externalPartTypeToken) {
        this.externalPartTypeToken = externalPartTypeToken;
    }

    /**
     * Write type.
     *
     * @param output the output
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void writeType(DataOutput output) throws IOException {
        output.writeByte(externalPartTypeToken);
    }

    /**
     * Gets the type.
     *
     * @param c the c
     * @return the type
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
     * Read type.
     *
     * @param input the input
     * @return the identifier part types
     * @throws IOException Signals that an I/O exception has occurred.
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