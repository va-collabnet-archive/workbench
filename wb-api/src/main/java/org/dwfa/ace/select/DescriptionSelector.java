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
package org.dwfa.ace.select;

import org.dwfa.ace.api.I_DescriptionTuple;

/**
 * Chooses the most appropriate description to be used.
 * 
 * Multiple preference strategies can be used; the strategies themselves being
 * in a preferential order.
 * 
 * This currently uses a predetermined preferential order however the intention
 * is that this
 * will evolve to use the user's config preferences.
 * 
 * @see Selector#getPreferred(java.util.List)
 */
public class DescriptionSelector extends Selector<I_DescriptionTuple> {

    /**
     * @param <T> A SelectionStrategy implementation
     * @param preferences The preferential order of strategies to select a
     *            preferred description.
     *            Each strategy will define a preferential order within itself.
     */
    public <T extends SelectionStrategy<?, I_DescriptionTuple>> DescriptionSelector(T... preferences) {
        super(preferences);
    }

    /**
     * Select a preferred description by ordering by the description's language
     * ("en-GB", "en-US", etc)
     */
    public static class LanguagePreference extends SelectionStrategy<String, I_DescriptionTuple> {

        /**
         * @param languages The preferential order of languages (text)
         */
        public LanguagePreference(String... languages) {
            super(languages);
        }

        @Override
        protected String getComparableValue(I_DescriptionTuple tuple) {
            return tuple.getLang();
        }
    }

    /**
     * Select a preferred description by ordering by the description's status
     * (current, retired, etc)
     */
    public static class StatusPreference extends SelectionStrategy<Integer, I_DescriptionTuple> {

        /**
         * @param statuses The preferential order of statuses (native status id)
         */
        public StatusPreference(Integer... statuses) {
            super(statuses);
        }

        @Override
        protected Integer getComparableValue(I_DescriptionTuple tuple) {
            return tuple.getStatusId();
        }
    }

    /**
     * Select a preferred description by ordering by the description's type (PT,
     * FSN, etc)
     */
    public static class TypePreference extends SelectionStrategy<Integer, I_DescriptionTuple> {

        /**
         * @param types The preferential order of description types
         */
        public TypePreference(Integer... types) {
            super(types);
        }

        @Override
        protected Integer getComparableValue(I_DescriptionTuple tuple) {
            return tuple.getTypeId();
        }
    }

}
