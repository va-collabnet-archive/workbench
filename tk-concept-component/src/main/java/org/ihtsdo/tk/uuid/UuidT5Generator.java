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
package org.ihtsdo.tk.uuid;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * The Class UuidT3Generator generates a type 5 UUID object. A type 5 UUID is
 * name based and uses SHA-1 hashing to create the uuid from the given name.
 * This is the preferred type of UUID to generate.
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Universally_unique_identifier">http://en.wikipedia.org/wiki/Universally_unique_identifier</a>
 */
public class UuidT5Generator {

    /**
     * The Constant encoding.
     */
    public static final String encoding = "8859_1";
    /**
     * The author time id.
     */
    public static UUID AUTHOR_TIME_ID = UUID.fromString("c6915290-30fc-11e1-b86c-0800200c9a66");

    /**
     * Generates a type 5 UUID based on the given
     * <code>name</code> and the
     * <code>namespace</code> to which the id belongs.
     *
     * @param namespace the uuid associated with the namespace
     * @param name the name to generate the uuid from
     * @return the generated uuid
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     */
    public static UUID get(UUID namespace, String name) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha1Algorithm = MessageDigest.getInstance("SHA-1");

        // Generate the digest.
        sha1Algorithm.reset();
        if (namespace != null) {
            sha1Algorithm.update(getRawBytes(namespace));
        }
        sha1Algorithm.update(name.getBytes(encoding));
        byte[] sha1digest = sha1Algorithm.digest();

        sha1digest[6] &= 0x0f; /* clear version */
        sha1digest[6] |= 0x50; /* set to version 5 */
        sha1digest[8] &= 0x3f; /* clear variant */
        sha1digest[8] |= 0x80; /* set to IETF variant */

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (sha1digest[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (sha1digest[i] & 0xff);
        }

        return new UUID(msb, lsb);
    }

    /**
     * Generates a type 5 UUID based on the given
     * <code>name</code>.
     *
     * @param name the name to generate the uuid from
     * @return the generated uuid
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     */
    public static UUID get(String name) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return get(null, name);
    }

    /**
     * Generates a type 5 UUID based for a description based on the given
     * description text, original language, and original concept.
     *
     * @param text a String representation of the description text
     * @param langPrimoridalUuid the uuids representing the language of the
     * primordial version of the description
     * @param conceptPrimordialUuid the uuids representing the concept
     * associated with the primordial version of the description
     * @return the generated uuid
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @throws UnsupportedEncodingException indicates an unsupported encoding exception has occurred
     */
    public static UUID getDescUuid(String text,
            UUID langPrimoridalUuid,
            UUID conceptPrimordialUuid) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return get(langPrimoridalUuid, text + conceptPrimordialUuid.toString());
    }

    /**
     * Gets the byte from a given
     * <code>uuid</code>. This routine adapted from org.safehaus.uuid.UUID,
     * which is licensed under Apache 2.
     *
     * @param uuid the uuid to find the bytes for
     * @return the raw bytes found from the specified uuid
     */
    public static byte[] getRawBytes(UUID uuid) {
        String id = uuid.toString();
        if (id.length() != 36) {
            throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
        }
        byte[] rawBytes = new byte[16];

        for (int i = 0, j = 0; i < 36; ++j) {
            // Need to bypass hyphens:
            switch (i) {
                case 8:
                case 13:
                case 18:
                case 23:
                    if (id.charAt(i) != '-') {
                        throw new NumberFormatException("UUID has to be represented by the standard 36-char representation");
                    }
                    ++i;
            }
            char c = id.charAt(i);

            if (c >= '0' && c <= '9') {
                rawBytes[j] = (byte) ((c - '0') << 4);
            } else if (c >= 'a' && c <= 'f') {
                rawBytes[j] = (byte) ((c - 'a' + 10) << 4);
            } else if (c >= 'A' && c <= 'F') {
                rawBytes[j] = (byte) ((c - 'A' + 10) << 4);
            } else {
                throw new NumberFormatException("Non-hex character '" + c + "'");
            }

            c = id.charAt(++i);

            if (c >= '0' && c <= '9') {
                rawBytes[j] |= (byte) (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                rawBytes[j] |= (byte) (c - 'a' + 10);
            } else if (c >= 'A' && c <= 'F') {
                rawBytes[j] |= (byte) (c - 'A' + 10);
            } else {
                throw new NumberFormatException("Non-hex character '" + c + "'");
            }
            ++i;
        }
        return rawBytes;
    }

    /**
     * Generates a uuid from the given <code>byteArray</code>.
     *
     * @param byteArray the bytes to use for generating the uuid
     * @return the generated uuid
     */
    public static UUID getUuidFromRawBytes(byte[] byteArray) {
        if (byteArray.length != 16) {
            throw new NumberFormatException("UUID must be 16 bytes");
        }

        ByteBuffer raw = ByteBuffer.wrap(byteArray);

        return new UUID(raw.getLong(raw.position()), raw.getLong(raw.position() + 8));
    }
}
