package org.ihtsdo.rf2.test;

import java.io.FileInputStream;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ihtsdo.rf2.util.FileName;

/**
 * Title:        FileNameTest
 * Description:  Test class for testing properties file reading
 * Copyright:    Copyright (c) 2010
 * Company:      IHTSDO
 * @author 		 Varsha Parekh
 * @version 1.0
 */

/**
 * Unit test for FileNameTest .
 */
public class FileNameTest extends TestCase {
	static String fileNamePropsFile = ".\\src\\main\\resources\\fileconfig.properties";

	/**
	 * Create the test case
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public FileNameTest(String testName) {
		super(testName);
		System.out.println("FileNameTest Junit Test Classes");
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(FileNameTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testApp() {
		assertTrue(true);
	}

	public static void testPropertiesFile() {
		try {
			// Load properties file for FileNames
			Properties propsFileName = new Properties();
			propsFileName.load(new FileInputStream(fileNamePropsFile));
			// Get File Name values
			FileName fileName = new FileName(propsFileName);
			String conceptFileName = fileName.getConceptFileName();
			System.out.println(conceptFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
