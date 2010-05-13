/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.builder;

/**
 * {@code Builder<T>} and it's implementations are designed to encapsulate and
 * abstract steps of construction of objects so that different implementations
 * {@code Builder<T>} can construct different representations of objects.
 * <p>
 * For Example to build an Instance of
 * {@link org.dwfa.ace.api.LocalVersionedTerminology} using the
 * {@link org.dwfa.builder.itermfactory.LocalVersionedTerminologyBuilder} implementation of {@code Builder}
 * <pre>
 * {@code Builder<I_TermFactory> builder = new LocalVersionedTerminologyBuilder(vodbDirectory, readOnly, cacheSize,
 *                                          useExistingDb, dbSetupConfig);
 *
 * I_TermFactory localVersionedTerminology = builder.build();}
 * </pre>
 * </p>
 * @param <T> the Generic Type of the Builder Implementation.
 * @see org.dwfa.builder.itermfactory.LocalVersionedTerminologyBuilder
 * @author Matthew Edwards
 */
public interface Builder<T> {

    /**
     * Encapsulates the logic for building objects of generic type {@code T}.
     * @see Builder
     * @return T
     * @throws BuilderException if there is an exception whilst building object
     * of generic type {@code T}.
     */
    T build() throws BuilderException;
}
