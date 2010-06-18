package org.dwfa.mojo.file;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.file.util.FileUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Copyright (c) 2010 International Health Terminology Standards Development Organisation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ConcatFileMojoTest {

    private static File testDir = new File("target/test/");

	private static File inputFile_1 = new File(testDir,
			"concat_input_1.txt");
    private static File inputFile_2 = new File(testDir,
			"concat_input_2.txt");
	private static File outputFile = new File(testDir,
			"concat_output.txt");

	private static final String firstLine_1 = "This is the first line of file 1";
	private static final String secondLine_1 = "This is the second line of file 1";
    private static final String firstLine_2 = "This is the first line of file 2";
	private static final String secondLine_2 = "This is the second line of file 2";
    private static final String thirdLine_2 = "This is the third line of file 2";    

    /**
	 * Setup test file object
	 */
	@BeforeClass
	public static void setup() {

		if (!testDir.exists()) {
			testDir.mkdirs();
		}
		try {
			FileWriter writer = new FileWriter(inputFile_1);
			writer.write(firstLine_1 + "\n" + secondLine_1);
			writer.flush();
			writer.close();
            writer = new FileWriter(inputFile_2);
			writer.write(firstLine_2 + "\n" + secondLine_2 + "\n" + thirdLine_2);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}        
	}

	@Test
	public void testConcatDifferentOutputFile()
			throws MojoExecutionException, MojoFailureException, IOException {

		ConcatFilesMojo concatFilesMojo = new ConcatFilesMojo();
		concatFilesMojo.setInputFiles(new File[] {inputFile_1, inputFile_2});
		concatFilesMojo.setOutputFile(outputFile);
		concatFilesMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();
        String readLine3 = br.readLine();
        String readLine4 = br.readLine();
        String readLine5 = br.readLine();

		if (!((readLine1 + readLine2 + readLine3 + readLine4 + readLine5)
				.equals(firstLine_1 + secondLine_1 + firstLine_2 + secondLine_2 + thirdLine_2))) {
			Assert.fail("The text in the output file is incorrect");
		}
	}

	@Test
	public void testConcatSameOutputFile()
			throws MojoExecutionException, MojoFailureException, IOException {

		ConcatFilesMojo concatFilesMojo = new ConcatFilesMojo();
		concatFilesMojo.setInputFiles(new File[] {inputFile_1, inputFile_2});
		concatFilesMojo.setOutputFile(inputFile_2);
		concatFilesMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(inputFile_2));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();
        String readLine3 = br.readLine();
        String readLine4 = br.readLine();
        String readLine5 = br.readLine();

		if (!((readLine1 + readLine2 + readLine3 + readLine4 + readLine5)
				.equals(firstLine_1 + secondLine_1 + firstLine_2 + secondLine_2 + thirdLine_2))) {
			Assert.fail("The text in the output file is incorrect");
		}
	}

	@AfterClass
	public static void tearDown() {
        FileUtil.deleteDirectory(testDir);
	}
}
