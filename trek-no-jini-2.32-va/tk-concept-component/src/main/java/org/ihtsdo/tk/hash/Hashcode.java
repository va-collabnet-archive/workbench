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
package org.ihtsdo.tk.hash;

/**
 * The Class Hashcode contains methods for computing a hashcode based on a given set of parts.
 * @see Hashcode
 */
public class Hashcode {

    /**
     * Computes a hashcode based on the given integers, <code>parts</code>.
     *
     * @param parts the integers from which the hashcode should be computed
     * @return the int value representing the hashcode
     */
    public static int compute(int... parts) {
        int hash = 0;
        int len = parts.length;

        for (int i = 0; i < len; i++) {
            hash <<= 1;

            if (hash < 0) {
                hash |= 1;
            }

            hash ^= parts[i];
        }

        return hash;
    }

    /**
     * Computes a hashcode based on the given longs, <code>parts</code>.
     *
     * @param parts the longs from which the hashcode should be computed
     * @return the long value representing the hashcode
     */
    public static int computeLong(long... parts) {
        int[] intParts = new int[parts.length * 2];

        for (int i = 0; i < parts.length; i++) {
            intParts[i * 2] = (int) parts[i];
            intParts[i * 2 + 1] = (int) (parts[i] >>> 32);
        }

        return compute(intParts);
    }

    /**
     * Computes a hashcode based on the given shorts, <code>parts</code>.
     *
     * @param parts the shorts from which the hashcode should be computed
     * @return the short value representing the hashcode
     */
    public static short compute(short... parts) {
        short hash = 0;
        for (int i = 0; i < parts.length; i++) {
            hash <<= 1;
            if (hash < 0) {
                hash |= 1;
            }
            hash ^= parts[i];
        }
        return hash;
    }

    /**
     * Converts an <code>int</code> hashcode to a <code>short</code> hashcode.
     *
     * @param hash the int hashcode to convert
     * @return a short hashcode
     */
    public static short intHashToShortHash(int hash) {
        short[] parts = new short[2];
        parts[0] = (short) hash; // low order short
        parts[1] = (short) (hash >> 16); // high order short
        return compute(parts);
    }
}
