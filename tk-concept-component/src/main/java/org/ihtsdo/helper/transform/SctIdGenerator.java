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
package org.ihtsdo.helper.transform;

/**
 * The Class SctIdGenerator generates a SNOMED ID (SCT ID). A unique integer
 * Identifier applied to each SNOMED CT component ( Concept, Description,
 * Relationship, Subset, etc.). The SCTID includes an item Identifier, a
 * check-digit and a partition identifier. Depending on the partition identifier
 * is may also include a namespace identifier. <p>This class should not be used
 * to generate SCT IDs unless the max sequence number of the previously assigned
 * SCT ID is known.
 *
 * @see UuidSnomedMapHandler
 * @see <a href="http://www.snomed.org/tig?t=trg2main_sctid">IHTSDO Technical
 * Implementation Guide - SCT ID</a>
 *
 */
public class SctIdGenerator {

    /**
     * The Enum TYPE listing the possible types of SCT IDs. The second and third
     * digits from the right of the string rendering of the SCTID. The value of
     * the partition-identifier indicates the type of component that the SCTID
     * identifies (e.g. Concept, Description, Relationship, etc) and also
     * indicates whether the SCTID contains a namespace identifier.
     *
     */
    public static enum TYPE {

        /**
         * Identifies the SCT ID as a concept.
         */
        CONCEPT("10"),
        /**
         * Identifies the SCT ID as a description.
         */
        DESCRIPTION("11"),
        /**
         * Identifies the SCT ID as a relationship.
         */
        RELATIONSHIP("12"),
        /**
         * Identifies the SCT ID as a refset.
         */
        SUBSET("13");
        private String digits;

        /**
         * Instantiates a new SCT ID type based on the
         * <code>digits</code>.
         *
         * @param digits the digits specifying the SCT ID type
         */
        private TYPE(String digits) {
            this.digits = digits;
        }

        /**
         * Gets the digits specifying the SCT ID type.
         *
         * @return the digits specifying the SCT ID type
         */
        public String getDigits() {
            return digits;
        }
    }
    private static int[][] FnF = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 5, 7, 6, 2, 8, 3, 0, 9, 4}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
    private static int[][] Dihedral = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, {1, 2, 3, 4, 0, 6, 7, 8, 9, 5}, {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
        {3, 4, 0, 1, 2, 8, 9, 5, 6, 7}, {4, 0, 1, 2, 3, 9, 5, 6, 7, 8}, {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
        {6, 5, 9, 8, 7, 1, 0, 4, 3, 2}, {7, 6, 5, 9, 8, 2, 1, 0, 4, 3}, {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
        {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}};
    private static int[] InverseD5 = {0, 4, 3, 2, 1, 5, 6, 7, 8, 9};

    static {
        for (int i = 2; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                FnF[i][j] = FnF[i - 1][FnF[1][j]];
            }
        }
    }

    /**
     *  Generates an SCT ID based on the given
     * <code>sequence</code>, <code>projectId</code>,
     * <code>namespaceId</code>, and
     * <code>type</code>.
     *
     * @param sequence the sequence to use for the item identifier
     * @param projectId the id of the mapping project
     * @param namespaceId the <code>int</code> representation of the namespace to use
     * @param type the SCT ID type
     * @return a string representation of the generated SCT ID
     */
    public static String generate(long sequence, int projectId, int namespaceId, TYPE type) {

        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }

        String mergedid = Long.toString(sequence) + projectId + namespaceId + type.digits;

        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Generates an SCT ID based on the given
     * <code>sequence</code>,
     * <code>namespaceString</code>, and
     * <code>type</code>.
     *
     * @param sequence the sequence to use for the item identifier
     * @param namespaceString the <code>String</code> representation of the namespace to use
     * @param type the SCT ID type
     * @return a string representation of the generated SCT ID
     */
    public static String generate(long sequence, String namespaceString, TYPE type) {
        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }
        String mergedid = Long.toString(sequence) + namespaceString + type.digits;
        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Generates an SCT ID based on the given
     * <code>sequence</code>,
     * <code>namespaceId</code>, and
     * <code>type</code>.
     *
     * @param sequence the sequence to use for the item identifier
     * @param namespaceId the <code>int</code> representation of the namespace to use
     * @param type the SCT ID type
     * @return a string representation of the generated SCT ID
     */
    public static String generate(long sequence, int namespaceId, TYPE type) {

        if (sequence <= 0) {
            throw new RuntimeException("sequence must be > 0");
        }

        String mergedid = Long.toString(sequence) + namespaceId + type.digits;

        return mergedid + verhoeffCompute(mergedid);
    }

    /**
     * Verifies the check digit.
     *
     * @param idAsString a String representation of the SCT ID
     * @return <code>true</code>, if the check results in 0
     * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO
     * Technical Implementation Guide - Verhoeff</a>
     */
    public static boolean verhoeffCheck(String idAsString) {
        int check = 0;

        for (int i = idAsString.length() - 1; i >= 0; i--) {
            check =
                    Dihedral[check][FnF[(idAsString.length() - i - 1) % 8][new Integer(new String(
                    new char[]{idAsString.charAt(i)}))]];
        }
        if (check != 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Computes the check digit. The SCTID (See Component features -
     * Identifiers) includes a check-digit, which is generated using Verhoeff's
     * dihedral check.
     *
     * @param idAsString a String representation of the SCT ID
     * @return the generated SCT ID
     * @see <a href="http://www.snomed.org/tig?t=trg_app_check_digit">IHTSDO
     * Technical Implementation Guide - Verhoeff</a>
     */
    public static long verhoeffCompute(String idAsString) {
        int check = 0;
        for (int i = idAsString.length() - 1; i >= 0; i--) {
            check =
                    Dihedral[check][FnF[((idAsString.length() - i) % 8)][new Integer(new String(new char[]{idAsString
                        .charAt(i)}))]];

        }
        return InverseD5[check];
    }
}
