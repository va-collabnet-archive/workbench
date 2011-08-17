package org.ihtsdo.mojo.maven.transform;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.dwfa.bpa.util.GenerateUuid;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

public class UuidSnomedMapTest extends TestCase{
	//use mock objects to implement file as we are not interested in the file operations 
	// these are tested by other tests. Only want to test UuidSnomedMap class.
	
	File emptyMapFile;
	File nonEmptyMapFile;
	
	@Before
	public void setUp() throws Exception {
		FileWriter fileout = null;
		String outDir ="c:/target/"; // change this 
		File emptyMapDir = new File(outDir);
		emptyMapDir.mkdirs();

		// output.createNewFile();			

		emptyMapFile=new File(outDir,"test.txt"); //create file based on refset name; 
		
	    try {
			fileout=new FileWriter(emptyMapFile);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		fileout.write("");
		fileout.close();
		
	//could use this to create a test file with values
	}

	@After
	public void tearDown() throws Exception {
		// use this to remove created test files
		emptyMapFile.delete();
		//nonEmptyMapFile.delete();
	}

	
	public void testMapFromEmptyFile() {
		// Create a UuidSnomedMap from empty file
		
		UuidSnomedMap map;
		UUID testUuid ;
		Long sctId=  new Long("1121000000103");  // (long) 1123232321;
		map=null;
		try {
			
			// could we use a mock object here to represent file?
			
			map = UuidSnomedMap.read(emptyMapFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// assert initial state of max sequence
		
		
		Assert.assertEquals(0, (int)map.getMaxSequence());
		
		testUuid=Type3UuidFactory.fromSNOMED(sctId);
		
		// put a new value into map and check if MaxSequence has been incremented
		map.put(testUuid,sctId);
		Assert.assertEquals(1, (int)map.getMaxSequence());
	
	}
	@Ignore
	public void testMapFromNonEmptyFile() {
	 // should we have a file created in set up called non_empty?	
	// create a non empty mock file object which contains list of existing uuid to sctid maps
//		UuidSnomedMap map;
//		UUID testUuid ;
//		Long sctId=  new Long("2121000000103"); 
//		map=null;
//	
//		try {
//			
//			// could we use a mock object here to represent file?
//			
//			map = UuidSnomedMap.read(nonEmptyMapFile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// check that maxSequence has been incremented from non empty map file
//		Assert.assertEquals(1, (int)map.getMaxSequence());
//		testUuid=Type3UuidFactory.fromSNOMED(sctId);
//		
//		// put a new value into map and check if MaxSequence has been incremented
//		map.put(testUuid,sctId);
//		Assert.assertEquals(2, (int)map.getMaxSequence());
	}
}
