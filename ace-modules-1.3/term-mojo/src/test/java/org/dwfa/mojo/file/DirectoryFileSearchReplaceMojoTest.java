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
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Test class for directory file search replace mojo
 *
 * @author Luke Swindale
 */
public class DirectoryFileSearchReplaceMojoTest {

	private static File testDir = new File("target/test/");
	private static File inputDir = new File(testDir, "inputDir/");
	private static File outputDir = new File(testDir, "outputDir/");
	private static File inputFile1 = new File(inputDir,
			"searchReplace_1.txt");
	private static File outputFile1 = new File(outputDir,
			"searchReplace_1.txt");
	private static File inputFile2 = new File(inputDir,
			"searchReplace_2.txt");
	private static File outputFile2 = new File(outputDir,
			"searchReplace_2.txt");

	private static final String search1 = "searchVal";
	private static final String replace1 = "replaceVal";

	private static final String search2 = "replaceVal";
	private static final String replace2 = "anotherVal";

	private static final String firstLine1 = "This is the first searchVal line in our searchVal file";
	private static final String secondLine1 = "And this searchVal is the second line";

	private static final String firstLine2 = "blah blah blah searchVal whatever searchVal blah";
	private static final String secondLine2 = "And some more guff searchVal ... blah";

	/**
	 * Setup test file object
	 */
	@BeforeClass
	public static void setup() {

		if (!inputDir.exists()) {
			inputDir.mkdirs();
		}
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
		try {
			FileWriter writer = new FileWriter(inputFile1);
			writer.write(firstLine1 + "\n" + secondLine1);
			writer.flush();
			writer.close();

			writer = new FileWriter(inputFile2);
			writer.write(firstLine2 + "\n" + secondLine2);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSearchReplaceDifferentDir()
			throws MojoExecutionException, MojoFailureException, IOException {

		DirectoryFileSearchReplaceMojo dirFileSearchReplaceMojo = new DirectoryFileSearchReplaceMojo();
		dirFileSearchReplaceMojo.setInputDirectory(inputDir);
		dirFileSearchReplaceMojo.setOutputDirectory(outputDir);
		dirFileSearchReplaceMojo.setSearch(search1);
		dirFileSearchReplaceMojo.setReplace(replace1);
        dirFileSearchReplaceMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile1));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();
        br.close();

		if (!(readLine1
				.equals("This is the first replaceVal line in our replaceVal file") && readLine2
				.equals("And this replaceVal is the second line"))) {
			Assert.fail("The text in the output file 1 is incorrect");
		}

		br = new BufferedReader(new FileReader(outputFile2));
		readLine1 = br.readLine();
		readLine2 = br.readLine();
        br.close();

		if (!(readLine1
				.equals("blah blah blah replaceVal whatever replaceVal blah") && readLine2
				.equals("And some more guff replaceVal ... blah"))) {
			Assert.fail("The text in the output file 2 is incorrect");
		}
	}

	@Test
	public void testSearchReplaceSameDir() throws MojoExecutionException,
			MojoFailureException, IOException {

		DirectoryFileSearchReplaceMojo dirFileSearchReplaceMojo = new DirectoryFileSearchReplaceMojo();
		dirFileSearchReplaceMojo.setInputDirectory(outputDir);
		dirFileSearchReplaceMojo.setOutputDirectory(outputDir);
		dirFileSearchReplaceMojo.setSearch(search2);
		dirFileSearchReplaceMojo.setReplace(replace2);
		dirFileSearchReplaceMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile1));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();
        br.close();

		if (!(readLine1
				.equals("This is the first anotherVal line in our anotherVal file") && readLine2
				.equals("And this anotherVal is the second line"))) {
			Assert.fail("The text in the output file 1 is incorrect");
		}

		br = new BufferedReader(new FileReader(outputFile2));
		readLine1 = br.readLine();
		readLine2 = br.readLine();
        br.close();

		if (!(readLine1
				.equals("blah blah blah anotherVal whatever anotherVal blah") && readLine2
				.equals("And some more guff anotherVal ... blah"))) {
			Assert.fail("The text in the output file 2 is incorrect");
		}
	}

	@AfterClass
	public static void tearDown() {
        FileUtil.deleteDirectory(testDir);
	}
}
