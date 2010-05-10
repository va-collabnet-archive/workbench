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

import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class LocalVersionedTerminology {
    private static I_TermFactory factory;

    private static I_IdVersioned authorityId;

    private static I_TermFactory stealthfactory;

    private static Object home;

    public static I_TermFactory get() {
        if (stealthfactory != null) {
            return stealthfactory;
        }
        return factory;
    }

    public static void close(I_TermFactory factory) {
        if (LocalVersionedTerminology.factory == factory) {
            LocalVersionedTerminology.factory = null;
        }
    }

    public static void set(I_TermFactory factory) {
        if (stealthfactory != null && stealthfactory == factory) {
            // stealth factory set
        } else {
            if (LocalVersionedTerminology.factory == null || LocalVersionedTerminology.factory == factory) {
                setFactory(factory);
            } else {
                try {
                    if (authorityId.equals(factory.getAuthorityId())) {
                        setFactory(factory);
                    } else if (authorityId.equals(factory.getPreviousAuthorityId())) {
                        setFactory(factory);
                    } else {
                        AceLog.getAppLog().info("authorityId: " + authorityId);
                        AceLog.getAppLog().info("factory.getAuthorityId(): " + factory.getAuthorityId());
                        AceLog.getAppLog()
                            .info("factory.getPreviousAuthorityId(): " + factory.getPreviousAuthorityId());
                        throw new RuntimeException("LocalVersionedTerminology.factory is already set to: "
                            + LocalVersionedTerminology.factory + " new factory: " + factory);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void setFactory(I_TermFactory factory) {
        AceLog.getAppLog().info("Setting LocalVersionedTerminology to: " + factory);
        LocalVersionedTerminology.factory = factory;
        try {
            LocalVersionedTerminology.authorityId = factory.getAuthorityId();
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, IOException {
        if (stealthfactory != null && stealthfactory == factory) {
            // stealth factory set
        } else {
            I_ImplementTermFactory factory = factoryClass.newInstance();
            factory.setup(envHome, readOnly, cacheSize);
            home = envHome;
            set(factory);
        }
    }

    public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly,
            Long cacheSize, DatabaseSetupConfig databaseSetupConfig) throws InstantiationException,
            IllegalAccessException, IOException {
        if (stealthfactory != null && stealthfactory == factory) {
            // stealth factory set
        } else {
            I_ImplementTermFactory factory = factoryClass.newInstance();
            factory.setup(envHome, readOnly, cacheSize, databaseSetupConfig);
            home = envHome;
            set(factory);
        }
    }

    @SuppressWarnings("unchecked")
    public static void openDefaultFactory(File envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        if (stealthfactory != null && stealthfactory == factory) {
            // stealth factory set
        } else {
            open((Class<I_ImplementTermFactory>) Class.forName("org.dwfa.vodb.VodbEnv"), envHome, readOnly, cacheSize);
        }
    }

    @SuppressWarnings("unchecked")
    public static void createFactory(File envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        if (stealthfactory != null && stealthfactory == factory) {
            // stealth factory set
        } else {
            open((Class<I_ImplementTermFactory>) Class.forName("org.dwfa.vodb.VodbEnv"), envHome, readOnly, cacheSize,
                dbSetupConfig);
        }
    }

    /**
     * Create a new facotry and clear all the static local vars.
     * 
     * @param envHome File DB directory
     * @param readOnly boolean
     * @param cacheSize Long
     * @param dbSetupConfig DatabaseSetupConfig
     * @param clear boolean clear all the static local vars
     * @throws InstantiationException creating instance of factory
     * @throws IllegalAccessException creating instance of factory
     * @throws ClassNotFoundException creating instance of factory
     * @throws IOException reading DB files.
     */
    @SuppressWarnings("unchecked")
    public static void createFactory(File envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig,
            boolean clear) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
        factory = null;
        authorityId = null;
        stealthfactory = null;
        home = null;

        createFactory(envHome, readOnly, cacheSize, dbSetupConfig);
    }

    public static Object getHome() {
        return home;
    }

    public static I_TermFactory getStealthfactory() {
        return stealthfactory;
    }

    public static void setStealthfactory(I_TermFactory stealthfactory) {
        LocalVersionedTerminology.stealthfactory = stealthfactory;
    }

}
