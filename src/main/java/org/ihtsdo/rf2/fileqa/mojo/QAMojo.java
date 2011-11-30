package org.ihtsdo.rf2.fileqa.mojo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.fileqa.action.QA;
import org.ihtsdo.rf2.fileqa.model.Props;
import org.ihtsdo.rf2.fileqa.util.DateUtils;

/**
 * @goal qa-run
 */
public class QAMojo extends AbstractMojo {

	private static Logger logger = Logger.getLogger(QAMojo.class.getName());

	/**
	 * release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;

	/**
	 * release date.
	 * 
	 * @parameter
	 */
	private String releaseName;

	/**
	 * release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String prevDir;

	/**
	 * release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String currDir;

	/**
	 * release date.
	 * 
	 * @parameter
	 * @required
	 */
	private String reportName;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		try {

			Date sDate = new Date();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

			logger.info("FileQA Started  :" + sdf.format(sDate));
			if (logger.isDebugEnabled()) {
				logger.debug("FileQA Started  :" + sdf.format(sDate));
				logger.debug("");
			}
			logger.info("");

			if (!DateUtils.isValidDateStr(getReleaseDate(), "yyyyMMdd")) {
				logger.info("Release date :" + getReleaseDate() + " is invalid, please provide a valid release date with format YYYYMMDD");
				if (logger.isDebugEnabled())
					logger.debug("Release date :" + getReleaseDate() + " is invalid, please provide a valid realse date YYYYMMDD");
				throw new MojoExecutionException("Release date :" + getReleaseDate() + " is invalid, please provide a valid realse date YYYYMMDD");
			}

			File prevDir = null;

			try {
				prevDir = new File(getPrevdir());

				if (!prevDir.isDirectory()) {
					logger.info("Previous release folder :" + getPrevdir() + " is not a directory, please provide a valid directory ");
					if (logger.isDebugEnabled())
						logger.debug("Previous release folder :" + getPrevdir() + " is not a directory, please provide a valid directory ");
					throw new MojoExecutionException("Previous release folder :" + getPrevdir() + " is not a directory, please provide a valid directory ");
				}
			} catch (NullPointerException e) {
				logger.info("Cannot open previous release folder :" + getPrevdir() + " " + e.getMessage());
				if (logger.isDebugEnabled())
					logger.debug("Cannot open previous folder :" + getPrevdir() + " " + e.getMessage());
				throw new MojoExecutionException("Cannot open previous folder :" + getPrevdir() + " " + e.getMessage());
			}

			String prevFiles[] = prevDir.list();

			if (prevFiles.length <= 0) {
				logger.info("Previous release folder :" + getPrevdir() + " is empty, please provide a valid directory ");
				if (logger.isDebugEnabled())
					logger.debug("Previous release folder :" + getPrevdir() + " is empty, please provide a valid directory ");
				throw new MojoExecutionException("Previous release folder :" + getPrevdir() + " is empty, please provide a valid directory ");
			}

			File currDir = null;
			try {
				currDir = new File(getCurrDir());
				if (!currDir.isDirectory()) {
					logger.info("Current release folder :" + getCurrDir() + " is not a directory, please provide a valid directory ");
					if (logger.isDebugEnabled())
						logger.debug("Current release folder :" + getCurrDir() + " is not a directory, please provide a valid directory ");
					throw new MojoExecutionException("Current release folder :" + getCurrDir() + " is not a directory, please provide a valid directory ");
				}
			} catch (NullPointerException e) {
				logger.info("Cannot open current release folder :" + getCurrDir() + " " + e.getMessage());
				if (logger.isDebugEnabled())
					logger.debug("Cannot open current folder :" + getCurrDir() + " " + e.getMessage());
				throw new MojoExecutionException("Cannot open current folder :" + getCurrDir() + " " + e.getMessage());
			}

			String currFiles[] = currDir.list();

			if (currFiles.length <= 0) {
				logger.info("Current release folder :" + getCurrDir() + " is empty, please provide a valid directory ");
				if (logger.isDebugEnabled())
					logger.debug("Current release folder :" + getCurrDir() + " is empty, please provide a valid directory ");
				logger.debug("Current release folder :" + getCurrDir() + " is empty, please provide a valid directory ");
				throw new MojoExecutionException("Current release folder :" + getCurrDir() + " is empty, please provide a valid directory ");
			}

			// look for the end path seperator
			if (!getPrevdir().substring(getPrevdir().length() - 1, getPrevdir().length()).equals(File.separator))
				setPrevdir(getPrevdir() + File.separator);

			if (!getCurrDir().substring(getCurrDir().length() - 1, getCurrDir().length()).equals(File.separator))
				setCurrDir(getCurrDir() + File.separator);

			Props props = new Props();

			props.setCurRelDate(getReleaseDate());
			props.setReleaseName(getReleaseName());
			props.setPrevReleaseDir(getPrevdir());
			props.setCurrReleaseDir(getCurrDir());
			props.setReportName(getReportName());

			logger.info("FileQA PROPERTIES");
			logger.info("Release Date               :" + props.getCurRelDate());
			logger.info("Release Name              :" + props.getReleaseName());
			logger.info("Previous Release Folder   :" + props.getPrevReleaseDir());
			logger.info("Current Release Folder    :" + props.getCurrReleaseDir());
			logger.info("Report Name              :" + props.getReportName());

			QA.execute(props, prevDir, currDir);

			Date eDate = new Date();

			logger.info("");
			logger.info("FileQA Started         :" + sdf.format(sDate));
			if (logger.isDebugEnabled()) {
				logger.debug("FileQA Started  :" + sdf.format(sDate));
				logger.debug("");
			}

			logger.info("FileQA Ended           :" + sdf.format(eDate));
			if (logger.isDebugEnabled()) {
				logger.debug("FileQA Ended    :" + sdf.format(eDate));
				logger.debug("");
			}
			logger.info("");

			logger.info(DateUtils.elapsedTime("Total elapsed          :", sDate, eDate));

		} catch (Exception e) {
			logger.error("Message :", e);
			throw new MojoExecutionException(e.getMessage());
		}
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}

	public String getPrevdir() {
		return prevDir;
	}

	public void setPrevdir(String prevDir) {
		this.prevDir = prevDir;
	}

	public String getCurrDir() {
		return currDir;
	}

	public void setCurrDir(String currDir) {
		this.currDir = currDir;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

}
