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
package org.dwfa.maven;

import java.io.File;

public class MvnUtil {

    private static String localRepository;

    public static String path(String groupId, String artifactId, String version) {
        StringBuffer buff = new StringBuffer();
        buff.append(localRepository);
        buff.append(File.separatorChar);
        buff.append(groupId.replace('.', File.separatorChar));
        buff.append(File.separatorChar);
        buff.append(artifactId);
        buff.append(File.separatorChar);
        buff.append(version);
        buff.append(File.separatorChar);
        buff.append(artifactId);
        buff.append("-");
        buff.append(version);
        buff.append(".jar");
        return buff.toString();
    }

    public static String getLocalRepository() {
        return localRepository;
    }

    public static void setLocalRepository(String localRepository) {
        MvnUtil.localRepository = localRepository;
    }

}
