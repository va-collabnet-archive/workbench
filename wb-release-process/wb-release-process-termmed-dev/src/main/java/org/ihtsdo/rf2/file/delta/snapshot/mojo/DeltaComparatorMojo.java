package org.ihtsdo.rf2.file.delta.snapshot.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal delta-comparator
 * 
 * @phase install
 */
public class DeltaComparatorMojo extends AbstractMojo {

	private static String DASHES = "--------------------------------------------------------";

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the current and previous files
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<Compare> rF2Files;

	/**
	 * Location of the reportfile
	 * 
	 * @parameter
	 * @required
	 */
	private String reportFile;

	private BufferedWriter bw;

	public void execute() throws MojoExecutionException {

		getLog().info("Running the RF2 File Delta Comparator ");

		try {

			FileOutputStream os = new FileOutputStream(new File(reportFile));
			bw = new BufferedWriter(new OutputStreamWriter(os, "UTF8"), (1 * 1024));

			// write header
			bw.write("Delta Comparator report at :" + new Date());
			bw.newLine();

			for (int i = 0; i < rF2Files.size(); i++) {
				getLog().info("Previous file     :" + rF2Files.get(i).prevFile);
				getLog().info("Current file       :" + rF2Files.get(i).currFile);
				getLog().info("Report file       :" + reportFile);

				// check if the previous file exist
				File pFile = new File(rF2Files.get(i).prevFile);
				if (!pFile.exists())
					throw new MojoExecutionException("Previous file : " + rF2Files.get(i).prevFile + " doesn't exist, exiting ..");

				// check if the current file exist
				File cFile = new File(rF2Files.get(i).currFile);
				if (!cFile.exists())
					throw new MojoExecutionException("Current file : " + rF2Files.get(i).currFile + " doesn't exist, exiting ..");

				// create the report file
				try {

					bw.write(DASHES);
					bw.newLine();
					bw.newLine();
					bw.write("Current Delta File : " + rF2Files.get(i).currFile);
					bw.newLine();
					bw.write("Previous Delta File : " + rF2Files.get(i).prevFile);
					bw.newLine();
					bw.write(DASHES);
					bw.newLine();

					ArrayList<String> src = loadFile(pFile);

					ArrayList<String> dst = loadFile(cFile);

					// diff the src and dst
					bw.write("Rows present in previous : " + rF2Files.get(i).prevFile + " : but NOT present in current");
					bw.newLine();
					bw.write(DASHES);
					bw.newLine();
					diff(src, dst);

					// diff dst and src
					bw.write("Rows present in current : " + rF2Files.get(i).currFile + " : but NOT present in previous");
					bw.newLine();
					bw.write(DASHES);
					bw.newLine();
					diff(dst, src);

				} catch (IOException e) {
					getLog().error(e);
				}
			}
		} catch (IOException e) {
			getLog().error(e);
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				getLog().error(e);
			}
		}

		getLog().info("Done.");
	}

	public ArrayList<String> loadFile(File file) throws IOException {

		ArrayList<String> lines = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new FileReader(file));

		String line;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}

		return lines;
	}

	public void diff(ArrayList<String> src, ArrayList<String> dst) throws IOException {

		boolean found = false;
		int noneCount = 0;

		for (int i = 0; i < src.size(); i++) {
			found = false;
			for (int j = 0; j < dst.size(); j++) {
				if (src.get(i).equals(dst.get(j))) {
					found = true;
				}
			}
			if (!found) {
				if (getLog().isDebugEnabled())
					getLog().info("Line No. : " + i + " : Not found " + src.get(i));
				bw.write("Line No. : " + i + " : Not found : " + src.get(i));
				bw.newLine();
				noneCount++;
			}
		}

		if (noneCount == 0) {
			bw.write("None");
			bw.newLine();
			bw.write(DASHES);
			bw.newLine();

		}
	}
}
