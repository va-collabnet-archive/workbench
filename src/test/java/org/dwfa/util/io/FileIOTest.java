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
        assertEquals(".." + File.separator + ".." + File.separator + "superSuperfile", FileIO.getRelativePath(superSuperDirFile));
        
        
    }

}
