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
package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;

public class LocalVersionedTerminology {

    /**
     * @deprecated user Terms instead
     */
	public static I_TermFactory get() {
        return Terms.get();
    }

    /**
     * @deprecated user Terms instead
     */
    public static void close(I_TermFactory factory) {
        Terms.close(factory);
    }

    /**
     * @deprecated user Terms instead
     */
    public static void set(I_TermFactory factory) {
        Terms.set(factory);
    }

    /**
     * @deprecated user Terms instead
     */
     public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, IOException {
        Terms.open(factoryClass, envHome, readOnly, cacheSize);
    }

     /**
      * @deprecated user Terms instead
      */
    public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly,
            Long cacheSize, DatabaseSetupConfig databaseSetupConfig) throws InstantiationException,
            IllegalAccessException, IOException {
        Terms.open(factoryClass, envHome, readOnly, cacheSize);
    }

    /**
     * @deprecated user Terms instead
     */
    public static void openDefaultFactory(File envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        Terms.openDefaultFactory(envHome, readOnly, cacheSize);
    }

    /**
     * @deprecated user Terms instead
     */
    public static void createFactory(File envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        Terms.createFactory(envHome, readOnly, cacheSize, dbSetupConfig);
    }

    /**
     * @deprecated user Terms instead
     */
    public static Object getHome() {
        return Terms.getHome();
    }

    /**
     * @deprecated user Terms instead
     */
    public static I_TermFactory getStealthfactory() {
        return Terms.getStealthfactory();
    }

    /**
     * @deprecated user Terms instead
     */
    public static void setStealthfactory(I_TermFactory stealthfactory) {
        Terms.setStealthfactory(stealthfactory);
    }

}
