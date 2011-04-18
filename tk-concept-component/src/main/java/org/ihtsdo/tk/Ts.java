package org.ihtsdo.tk;

import java.lang.reflect.Method;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 * Ts is short for Terminology store...
 * 
 * @author kec
 *
 */
public class Ts {

    private static TerminologyStoreDI store;

    public static void set(TerminologyStoreDI store) {
        Ts.store = store;
    }

    public static TerminologyStoreDI get() {
        return store;
    }

    
    public static void setup() throws Exception {
        setup("org.ihtsdo.db.bdb.Bdb", "berkeley-db");
    }

    public static void setup(String storeClassName, String dbRoot) throws Exception {
        Class<?> class1 = Class.forName(storeClassName);
        Method method = class1.getMethod("setup", String.class);
        method.invoke(null, dbRoot);
    }
}
