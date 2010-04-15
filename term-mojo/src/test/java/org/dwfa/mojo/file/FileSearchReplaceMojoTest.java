package org.dwfa.mojo.file;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.file.spec.SearchReplaceSpec;
import org.dwfa.mojo.file.util.FileUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

/**
 * Test class for file search replace mojo
 * 
 * @author Luke Swindale
 */
public class FileSearchReplaceMojoTest {

	private static File testDir = new File("target/test/");
	private static File inputFile = new File(testDir,
			"searchReplace_input.txt");
	private static File outputFile = new File(testDir,
			"searchReplace_output.txt");

	private static final String search1 = "searchVal";
	private static final String replace1 = "replaceVal";

	private static final String search2 = "replaceVal";
	private static final String replace2 = "anotherVal";

	private static final String firstLine = "This is the first searchVal line in our searchVal file";
	private static final String secondLine = "And this searchVal is the second line";

    private static SearchReplaceSpec[] specs = new SearchReplaceSpec[2];

    /**
	 * Setup test file object
	 */
	@BeforeClass
	public static void setup() {

		if (!testDir.exists()) {
			testDir.mkdirs();
		}
		try {
			FileWriter writer = new FileWriter(inputFile);
			writer.write(firstLine + "\n" + secondLine);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
                 
        SearchReplaceSpec spec1 = new SearchReplaceSpec();
        SearchReplaceSpec spec2 = new SearchReplaceSpec();
        spec1.setSearch("first");
        spec1.setReplace("1st");
        spec2.setSearch("second");
        spec2.setReplace("2nd");
        specs[0] = spec1;
        specs[1] = spec2;
	}

	@Test
	public void testSearchReplaceDifferentFile()
			throws MojoExecutionException, MojoFailureException, IOException {

		FileSearchReplaceMojo fileSearchReplaceMojo = new FileSearchReplaceMojo();
		fileSearchReplaceMojo.setInputFile(inputFile);
		fileSearchReplaceMojo.setOutputFile(outputFile);
		fileSearchReplaceMojo.setSearch(search1);
		fileSearchReplaceMojo.setReplace(replace1);
		fileSearchReplaceMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();

		if (!(readLine1
				.equals("This is the first replaceVal line in our replaceVal file") && readLine2
				.equals("And this replaceVal is the second line"))) {
			Assert.fail("The text in the output file is incorrect");
		}
	}

	@Test
	public void testSearchReplaceSameFile() throws MojoExecutionException,
			MojoFailureException, IOException {

		FileSearchReplaceMojo fileSearchReplaceMojo = new FileSearchReplaceMojo();
		fileSearchReplaceMojo.setInputFile(outputFile);
		fileSearchReplaceMojo.setOutputFile(outputFile);
		fileSearchReplaceMojo.setSearch(search2);
		fileSearchReplaceMojo.setReplace(replace2);
		fileSearchReplaceMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();

		if (!(readLine1
				.equals("This is the first anotherVal line in our anotherVal file") && readLine2
				.equals("And this anotherVal is the second line"))) {
			Assert.fail("The text in the output file is incorrect");
		}
	}

    @Test
    public void testSearchReplaceSpec() throws MojoExecutionException, MojoFailureException, IOException {

        FileSearchReplaceMojo fileSearchReplaceMojo = new FileSearchReplaceMojo();
		fileSearchReplaceMojo.setInputFile(outputFile);
		fileSearchReplaceMojo.setOutputFile(outputFile);
        fileSearchReplaceMojo.setSpecs(specs);
		fileSearchReplaceMojo.execute();

		BufferedReader br = new BufferedReader(new FileReader(outputFile));
		String readLine1 = br.readLine();
		String readLine2 = br.readLine();

		if (!(readLine1
				.equals("This is the 1st anotherVal line in our anotherVal file") && readLine2
				.equals("And this anotherVal is the 2nd line"))) {
			Assert.fail("The text in the output file is incorrect");
		}
    }

	@AfterClass
	public static void tearDown() {
        FileUtil.deleteDirectory(testDir);
	}
}
