package org.ihtsdo.rf2.file.packaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.ihtsdo.rf2.file.packaging.model.ReadmeConfig;

public class Readme {

	private static BufferedWriter out = null;
	private static StringBuffer output = null;
	public static String newline = System.getProperty("line.separator");

	public Readme() {

	}

	public void process(ReadmeConfig readmeConfig) {
		try {
			int runCount = 1;
			if (readmeConfig.isRunTwice()) {
				runCount = 2;
			}

			if (readmeConfig.getSource() != null) {
				System.out.println("Reading header file: " + readmeConfig.getHeaderLocation());
				String header = null;
				if (readmeConfig.getHeaderLocation() != null) {
					header = readFile(readmeConfig.getHeaderLocation());
				} else {
					throw new Exception("Unable to read header file");
				}

				// allow to run multiple times to get Readme file information
				// that is generated
				for (int i = 0; i < runCount; i++) {
					String[] directories = readmeConfig.getSource().split(",");
					for (int x = 0; x < directories.length; x++) {
						String directory = directories[x];
						System.out.println("Creating readme file in: " + directory);

						File file = new File(directory);
						String location = directory + "/" + readmeConfig.getFileName();

						output = new StringBuffer();
						output.append(header);
						output.append(newline);

						processDirectory(readmeConfig, file, 1);

						// gather all output and write out after all processing complete
						// so we get information on the created Readme file
						out = new BufferedWriter(new FileWriter(location));
						out.write(output.toString());
						closeOutput();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	private void closeOutput() {
		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
			}
		}
	}

	/**
	 * Recursive method to walk directory structure and write directory names, file names and file sized to the readme file
	 * 
	 * @param file
	 * @param level
	 * @throws IOException
	 */
	private void processDirectory(ReadmeConfig readmeConfig, File file, int level) throws IOException {
		String tabs = getTabs(level);

		if (file.isDirectory()) {
			// write directory name
			writeToFile(tabs + file.getName());

			File[] files = file.listFiles();
			// sort names alphabetically
			Arrays.sort(files, new FilenameComparator());

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					// recursively walk directories
					int newLevel = level + 1;
					processDirectory(readmeConfig, files[i], newLevel);
				}
			}
		} else {
			// write file information (do not write readme file information)
			long size = file.length();
			writeToFile(format(readmeConfig, file.getName(), size, level));
		}
	}

	/**
	 * Writes input message to output buffer with a newline The output is written to a file once all processing is done
	 * 
	 * @param msg
	 */
	private void writeToFile(String msg) {
		try {
			if (output != null) {
				output.append(msg);
				output.append(newline);
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}

	/**
	 * Formats the string with the filename and size. Uses the width of the entire string from the properties file, or uses a default width.
	 * 
	 * @param filename
	 * @param fileSize
	 * @param level
	 * @return
	 */
	private static String format(ReadmeConfig readmeConfig, String filename, long fileSize, int level) {
		String tabs = getTabs(level);
		int width = readmeConfig.getFileNameWidth();
		String fill = " ";
		StringBuffer b = new StringBuffer();
		b.append(tabs);
		b.append(filename);
		int length = tabs.length() + filename.length();
		while ((width--) - length > 0) {
			b.append(fill);
		}
		b.append(fileSize);
		b.append(" bytes");
		return b.toString();
	}

	/**
	 * Returns a string with the correct number of space characters for the current level
	 * 
	 * @param level
	 * @return
	 */
	private static String getTabs(int level) {
		StringBuffer tabs = new StringBuffer();
		for (int j = 0; j < level; j++) {
			tabs.append("    ");
		}
		return tabs.toString();
	}

	/**
	 * Read a file from the location and return the contents as a string
	 * 
	 * @param location
	 * @return
	 * @throws Exception
	 */
	private String readFile(String location) throws Exception {
		StringBuffer buffer = new StringBuffer();
		try {
			if (location != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(location)));

				String str;
				while ((str = in.readLine()) != null) {
					buffer.append(str);
					buffer.append(newline);
				}
				in.close();
			}
		} catch (Exception e) {
			throw new Exception("Error reading file [" + location + "]: " + e.getMessage(), e);
		}
		return buffer.toString();
	}

}
