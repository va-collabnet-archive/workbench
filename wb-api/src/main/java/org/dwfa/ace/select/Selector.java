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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A generic class for selecting the preferred element from a collection.
 * 
 * The implementing class will define both:
 * <ul>
 * <li>1. The type of object (the collection type) that it is made to process
 * <li>2. Preferences classes (which implement {@link SelectionStrategy}) that
 * influence it's behaviour <br>
 * 
 * @see org.dwfa.ace.select.DescriptionSelector
 * @param <E> The type of the objects to be selected from
 */
public class Selector<E> {

    private SelectionStrategy<?, E>[] preferences;

    public <V extends SelectionStrategy<?, E>> Selector(V... preferences) {
        this.preferences = preferences;
    }

    /**
     * Get the most preferred item from a collection
     */
    public E getPreferred(List<E> items) {

        Collections.sort(items, new Comparator<E>() {
            public int compare(E o1, E o2) {
                for (SelectionStrategy<?, E> stategy : preferences) {
                    int result = stategy.compare(o1, o2);
                    if (result != 0)
                        return result;
                }
                return 0;
            }
        });

        return items.get(0);
    }

    /**
     * A template for strategies that may be provided to a {@link Selector} as
     * an ordering/ranking preference.
     * 
     * @param <T> The type of configuration parameters that may be passed to the
     *            implementing class.
     *            This is the type of the comparable attribute from the
     *            object(s) being compared.
     * @param <O> The type of the objects to be compared. Should matched generic
     *            type E from top-level {@link Selector} class.
     */
    protected static abstract class SelectionStrategy<T, O> implements Comparator<O> {

        protected T[] values;

        public SelectionStrategy(T... values) {
            this.values = values;
        }

        public int compare(O o1, O o2) {
            for (T value : values) {
                if (value.equals(getComparableValue(o1)) && !value.equals(getComparableValue(o2))) {
                    return -1;
                } else if (value.equals(getComparableValue(o2)) && !value.equals(getComparableValue(o1))) {
                    return 1;
                }
            }
            return 0;
        }

        /**
         * Concrete implementations to override and obtain the appropriate value
         * from a specific type of object.
         */
        protected abstract T getComparableValue(O object);
    }

}
