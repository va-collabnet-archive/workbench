/*
 * Created on Dec 13, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import java.io.File;
import java.io.FilenameFilter;

public class VHelp {

    public static String addVersion(String prefix, String directory) {
        File dir = new File(directory);
        String [] matches = dir.list(new VHelpFileFilter(prefix));
        warnIfEmpty(prefix, matches);
        return matches[0];
    }

    private static void warnIfEmpty(String prefix, String[] matches) {
        if (matches == null || matches.length == 0) {
            System.out.println("can't find prefix: " + prefix);
        }
    }
    
    public static String addDlVersion(String prefix) {
        File dir = new File("lib-dl");
        String [] matches = dir.list(new VHelpFileFilter(prefix));
        warnIfEmpty(prefix, matches);
        return matches[0];
    }
    
    public static String addLibVersion(String prefix) {
        File dir = new File("lib");
        String [] matches = dir.list(new VHelpFileFilter(prefix));
        warnIfEmpty(prefix, matches);
        return "lib/" + matches[0];
    }
    
    private static class VHelpFileFilter implements FilenameFilter {
        private String prefix;
        /**
         * @param prefix
         */
        public VHelpFileFilter(String prefix) {
            super();
             this.prefix = prefix;
        }
        public boolean accept(File dir, String name) {
             return name.startsWith(prefix);
        }
        
    }
}
