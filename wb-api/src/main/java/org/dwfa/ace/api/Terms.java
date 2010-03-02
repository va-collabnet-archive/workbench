package org.dwfa.ace.api;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.log.AceLog;

public class Terms {
    private static I_TermFactory factory;

    private static Object home;

    public static I_TermFactory get() {
        return factory;
    }

    public static void close(I_TermFactory factory) {
        if (Terms.factory == factory) {
        	Terms.factory = null;
        }
    }

    public static void set(I_TermFactory factory) {
         setFactory(factory);
    }

    private static void setFactory(I_TermFactory factory) {
        AceLog.getAppLog().info("Setting Terms to: " + factory);
        Terms.factory = factory;
    }

    public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, IOException {
            I_ImplementTermFactory factory = factoryClass.newInstance();
            factory.setup(envHome, readOnly, cacheSize);
            home = envHome;
            set(factory);
    }

    public static void open(Class<I_ImplementTermFactory> factoryClass, Object envHome, boolean readOnly,
            Long cacheSize, DatabaseSetupConfig databaseSetupConfig) throws InstantiationException,
            IllegalAccessException, IOException {
            I_ImplementTermFactory factory = factoryClass.newInstance();
            factory.setup(envHome, readOnly, cacheSize, databaseSetupConfig);
            home = envHome;
            set(factory);
    }

    @SuppressWarnings("unchecked")
    public static void openDefaultFactory(File envHome, boolean readOnly, Long cacheSize)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
            open((Class<I_ImplementTermFactory>) Class.forName("org.dwfa.vodb.VodbEnv"), envHome, readOnly, cacheSize);
    }

    @SuppressWarnings("unchecked")
    public static void createFactory(File envHome, boolean readOnly, Long cacheSize, DatabaseSetupConfig dbSetupConfig)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
            open((Class<I_ImplementTermFactory>) Class.forName("org.dwfa.vodb.VodbEnv"), envHome, readOnly, cacheSize,
                dbSetupConfig);
    }

    public static Object getHome() {
        return home;
    }

    /**
     * @deprecated use setFactory
     * @return
     */
    @Deprecated
    public static I_TermFactory getStealthfactory() {
        return factory;
    }

    /**
     * @deprecated use setFactory
     * @param stealthfactory
     */
    @Deprecated
    public static void setStealthfactory(I_TermFactory stealthfactory) {
        Terms.factory = stealthfactory;
    }

}
