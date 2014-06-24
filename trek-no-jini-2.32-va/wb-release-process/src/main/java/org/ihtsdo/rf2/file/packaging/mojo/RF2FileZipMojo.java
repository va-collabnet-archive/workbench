package org.ihtsdo.rf2.file.packaging.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal zip
 * 
 * @phase install
 */
public class RF2FileZipMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the source folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String zipSourceFolder;

	/**
	 * Location of the zipFolder.
	 * 
	 * @parameter
	 * @required
	 */
	private String zipFolder;

	/**
	 * Zip file name
	 * 
	 * @parameter
	 * @required
	 */
	private String zipFileName;

	public void execute() throws MojoExecutionException {

		getLog().info("Running the RF2 File Zip with the following ");
		getLog().info("Source folder     : " + zipSourceFolder);
		getLog().info("ZIP folder : " + zipFolder);
		getLog().info("ZIP file name : " + zipFileName);
		// check if the release folder exists
		File sFile = new File(zipSourceFolder);
		if (!sFile.exists())
			throw new MojoExecutionException("Source fodler : " + zipSourceFolder + " doesn't exist, exiting ..");

		File zFile = new File(zipFolder);

		// check if the packaging folder exists delete it
		if (zFile.exists()) {
			getLog().info("ZIP folder : " + zipFolder + " exists, deleting ...");
			if (!Delete.deleteDirectory(zFile))
				getLog().info("WARNING! ZIP folder : " + zipFolder + " not deleted, folders/files may be wrong");
			else
				getLog().info("ZIP folder : " + zipFolder + " deleted.");
		}

		if (!zFile.exists()) {
			zFile.mkdirs();
		}

		try {
			String zipFullName = zipFolder + File.separator + zipFileName;
			zipDirectory(zipSourceFolder, zipFullName);
		} catch (IllegalArgumentException e) {
			getLog().error(e);
		} catch (IOException e) {
			getLog().error(e);
		}

		getLog().info("Done.");
	}

	public void zipDirectory(String dir, String zipFile) throws IOException, IllegalArgumentException {

		List<File> fileList = new ArrayList<File>();
		getAllFiles(new File(dir), fileList);
		writeZipFile(new File(dir), fileList);

	}

	public void getAllFiles(File dir, List<File> fileList) {
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				fileList.add(file);
				if (file.isDirectory()) {
					getLog().info("directory:" + file.getCanonicalPath());
					getAllFiles(file, fileList);
				} else {
					getLog().info("     file:" + file.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			getLog().error(e);
		}
	}

	public void writeZipFile(File directoryToZip, List<File> fileList) {

		try {
			FileOutputStream fos = new FileOutputStream(zipFolder + File.separator + zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : fileList) {
				if (!file.isDirectory()) { // we only zip files, not directories
					addToZip(directoryToZip, file, zos);
				}
			}

			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			getLog().error(e);
		} catch (IOException e) {
			getLog().error(e);
		}
	}

	public void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException, IOException {

		FileInputStream fis = new FileInputStream(file);

		// we want the zipEntry's path to be a relative path that is relative
		// to the directory being zipped, so chop off the rest of the path
		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
		getLog().info("Writing '" + zipFilePath + "' to zip file : " + zipFolder + File.separator + zipFileName);
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}
}
