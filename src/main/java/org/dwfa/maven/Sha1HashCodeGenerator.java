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
package org.dwfa.maven;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Generates SHA-1 hash codes.
 * 
 * @author Chrissy Hill
 */
public class Sha1HashCodeGenerator {

    /**
     * Secure one-way hash function for SHA-1 algorithm.
     */
    MessageDigest sha1Algorithm;

    /**
     * Creates a new instance of the hash function.
     * 
     * @throws NoSuchAlgorithmException Encountered if an unsupported
     *             algorithm is used.
     */
    public Sha1HashCodeGenerator() throws NoSuchAlgorithmException {
        sha1Algorithm = MessageDigest.getInstance("SHA-1");
        sha1Algorithm.reset();
    }

    /**
     * Adds the string representation of an object to the hash function.
     * 
     * @param obj The object to be processed into the hash function.
     */
    public void add(Object obj) {
        String objString = obj.toString();
        byte[] byteArray = new byte[objString.length()];
        for (int i = 0; i < objString.length(); i++) {
            byteArray[i] = (byte) objString.charAt(i);
        }
        sha1Algorithm.update(byteArray);
    }

    /**
     * Completes the hash function and gets the generated hash code.
     * 
     * @return The generated hash code.
     */
    public String getHashCode() {
        byte[] digest = sha1Algorithm.digest();
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result = result + digest[i];
        }

        return result;
    }
}
