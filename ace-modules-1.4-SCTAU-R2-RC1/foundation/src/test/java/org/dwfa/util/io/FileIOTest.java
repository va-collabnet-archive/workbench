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
package org.dwfa.util.io;

import java.io.File;

import junit.framework.TestCase;

public class FileIOTest extends TestCase {

    public void testGetRelativePath() {

        File startupDir = new File(System.getProperty("user.dir"));

        File subDir = new File(startupDir, "sub");

        File subDirFile = new File(subDir, "subfile");
        System.out.println(FileIO.getRelativePath(subDirFile));
        assertEquals("sub" + File.separator + "subfile", FileIO.getRelativePath(subDirFile));

        File superDir = startupDir.getParentFile();
        File superDirFile = new File(superDir, "superfile");
        System.out.println(FileIO.getRelativePath(superDirFile));
        assertEquals(".." + File.separator + "superfile", FileIO.getRelativePath(superDirFile));

        File superSuperDir = startupDir.getParentFile().getParentFile();
        File superSuperDirFile = new File(superSuperDir, "superSuperfile");
        System.out.println(FileIO.getRelativePath(superSuperDirFile));
        assertEquals(".." + File.separator + ".." + File.separator + "superSuperfile",
            FileIO.getRelativePath(superSuperDirFile));

    }

}
