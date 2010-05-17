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
/*
 * Created on Dec 13, 2005
 */
package org.dwfa.jini;

import java.io.File;
import java.io.FilenameFilter;

public class VHelp {

    public static String addVersion(String prefix, String directory) {
        File dir = new File(directory);
        String[] matches = dir.list(new VHelpFileFilter(prefix));
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
        String[] matches = dir.list(new VHelpFileFilter(prefix));
        warnIfEmpty(prefix, matches);
        if (matches.length >= 1) {
            return matches[0];
        }
        return "";
    }

    public static String addLibVersion(String prefix) {
        File dir = new File("lib");
        String[] matches = dir.list(new VHelpFileFilter(prefix));
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
