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
package org.ihtsdo.helper.transform;

// TODO: Auto-generated Javadoc
/**
 * The Class SctIdGenerator.
 */
public class SctIdGenerator {
    
   /**
    * The Enum TYPE.
    */
   public static enum TYPE {
        
        /** The concept. */
        CONCEPT("10"),
        
        /** The description. */
        DESCRIPTION("11"),
        
        /** The relationship. */
        RELATIONSHIP("12"),
        
        /** The subset. */
        SUBSET("13");
        
        /** The digits. */
        private String digits;

        /**
         * Instantiates a new type.
         *
         * @param digits the digits
         */
        private TYPE(String digits) {
            this.digits = digits;
        }

        /**
         * Gets the digits.
         *
         * @return the digits
         */
        public String getDigits() {
            return digits;
        }
    }

    /** The Fn f. */
    private static int[][] FnF =
            { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
             { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
             { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

    /** The Dihedral. */
    private static int[][] Dihedral =
            { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 },
             { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 },
             { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 }, { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 },
             { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

    /** The Inverse d5. */
    private static int[] InverseD5 = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

    static {
        for (int i = 2; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                FnF[i][j] = FnF[i - 1][FnF[1][j]];
            }
        }
    }
    
    /**
     * Generate.
     *
     * @param sequence the sequence
     * @param projectId the project id
     * @param namespaceId the namespace id
     * @param type the type
     * @return the string
     */
    public static String generate(long sequence, int projectId, int namespaceId, TYPE type) {

        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }

        String mergedid = Long.toString(sequence) + projectId + namespaceId + type.digits;

        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Generate.
     *
     * @param sequence the sequence
     * @param namespaceStr the namespace str
     * @param type the type
     * @return the string
     */
    public static String generate(long sequence, String namespaceStr, TYPE type) {
        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }
        String mergedid = Long.toString(sequence) + namespaceStr + type.digits;
        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Generate.
     *
     * @param sequence the sequence
     * @param namespaceId the namespace id
     * @param type the type
     * @return the string
     */
    public static String generate(long sequence, int namespaceId, TYPE type) {

        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }

        String mergedid = Long.toString(sequence) + namespaceId + type.digits;

        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Verhoeff check.
     *
     * @param idAsString the id as string
     * @return true, if successful
     */
    public static boolean verhoeffCheck(String idAsString) {
        int check = 0;

        for (int i = idAsString.length() - 1; i >= 0; i--) {
            check =
                    Dihedral[check][FnF[(idAsString.length() - i - 1) % 8][new Integer(new String(
                        new char[] { idAsString.charAt(i) }))]];
        }
        if (check != 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Verhoeff compute.
     *
     * @param idAsString the id as string
     * @return the long
     */
    public static long verhoeffCompute(String idAsString) {
        int check = 0;
        for (int i = idAsString.length() - 1; i >= 0; i--) {
            check =
                    Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[] { idAsString
                        .charAt(i) }))]];

        }
        return InverseD5[check];
    }
}
