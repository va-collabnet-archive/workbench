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
package org.ihtsdo.tk;

import java.lang.reflect.Method;
import org.ihtsdo.tk.api.TerminologyStoreDI;


/**
 * The Class Ts provides methods for setting up and accessing the Terminolgoy Store. Ts is an abbreviation for Terminology store.
 *
 */
public class Ts {

    private static TerminologyStoreDI store;

    /**
     * Sets the terminology store.
     *
     * @param terminologyStore the terminology store
     */
    public static void set(TerminologyStoreDI terminologyStore) {
        Ts.store = terminologyStore;
    }

    /**
     * Gets the terminology store.
     *
     * @return the terminology store
     */
    public static TerminologyStoreDI get() {
        return store;
    }

    
    /**
     * Sets up the terminology store.
     *
     * @throws Exception indicates an exception has occurred
     */
    public static void setup() throws Exception {
        setup("org.ihtsdo.db.bdb.Bdb", "berkeley-db");
    }

    /**
     * Sets up the terminology store.
     *
     * @param storeClassName the class name of the store
     * @param dbRoot a String representing the database root
     * @throws Exception indicates an exception has occurred
     */
    public static void setup(String storeClassName, String dbRoot) throws Exception {
        Class<?> class1 = Class.forName(storeClassName);
        Method method = class1.getMethod("setup", String.class);
        method.invoke(null, dbRoot);
    }
}
