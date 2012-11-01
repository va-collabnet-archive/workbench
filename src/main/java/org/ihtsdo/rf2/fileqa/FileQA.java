package org.ihtsdo.rf2.fileqa;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;

import javax.xml.parsers.ParserConfigurationException;

import jxl.write.WriteException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.action.QA;
import org.ihtsdo.rf2.fileqa.model.Props;
import org.ihtsdo.rf2.fileqa.util.DateUtils;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;
import org.xml.sax.SAXException;

public class FileQA {

	private static Logger logger = Logger.getLogger(FileQA.class.getName());

	/**
	 * @param args
	 */

	private static void dumpProps(Props props) {

		if (logger.isDebugEnabled()) {
			logger.debug("FileQA PROPERTIES  :");
			logger.debug(props.getCurRelDate());
			logger.debug(props.getReleaseName());
			logger.debug(props.getPrevReleaseDir());
			logger.debug(props.getCurrReleaseDir());
			logger.debug(props.getReportName());
			logger.debug("");
		}
	}

	public static void main(String[] args) {

		BasicConfigurator.configure();

		if (args.length < 5) {
			System.out.println("Invalid arguments");
			System.out.println("Usage: java -jar FileQA.jar  <release> <releaseName> <prevReleaseDir> <currentReleaseDir> <reportName>");
			System.out.println("Usage: java -jar FileQA.jar 20100731 20100731STU c:\\prevDir c:\\currDir Report.xls");
			System.exit(1);
		}

		Date sDate = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");

		logger.info("FileQA Started  :" + sdf.format(sDate));
		if (logger.isDebugEnabled()) {
			logger.debug("FileQA Started  :" + sdf.format(sDate));
			logger.debug("");
		}
		logger.info("");

		if (!DateUtils.isValidDateStr(args[0], "yyyyMMdd")) {
			logger.info("Release date :" + args[0] + " is invalid, please provide a valid release date with format YYYYMMDD");
			if (logger.isDebugEnabled())
				logger.debug("Release date :" + args[0] + " is invalid, please provide a valid realse date YYYYMMDD");
			System.exit(1);
		}

		File prevDir = null;

		try {
			prevDir = new File(args[2]);

			if (!prevDir.isDirectory()) {
				logger.info("Previous release folder :" + args[2] + " is not a directory, please provide a valid directory ");
				if (logger.isDebugEnabled())
					logger.debug("Previous release folder :" + args[2] + " is not a directory, please provide a valid directory ");
				System.exit(1);
			}
		} catch (NullPointerException e) {
			logger.info("Cannot open previous release folder :" + args[2] + " " + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Cannot open previous folder :" + args[2] + " " + e.getMessage());
			System.exit(1);
		}

		String prevFiles[] = prevDir.list();

		if (prevFiles.length <= 0) {
			logger.info("Previous release folder :" + args[2] + " is empty, please provide a valid directory ");
			if (logger.isDebugEnabled())
				logger.debug("Previous release folder :" + args[2] + " is empty, please provide a valid directory ");
			System.exit(1);
		}

		File currDir = null;
		try {
			currDir = new File(args[3]);
			if (!currDir.isDirectory()) {
				logger.info("Current release folder :" + args[3] + " is not a directory, please provide a valid directory ");
				if (logger.isDebugEnabled())
					logger.debug("Current release folder :" + args[3] + " is not a directory, please provide a valid directory ");
				System.exit(1);
			}
		} catch (NullPointerException e) {
			logger.info("Cannot open current release folder :" + args[3] + " " + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Cannot open current folder :" + args[3] + " " + e.getMessage());
			System.exit(1);
		}

		String currFiles[] = currDir.list();

		if (currFiles.length <= 0) {
			logger.info("Current release folder :" + args[3] + " is empty, please provide a valid directory ");
			if (logger.isDebugEnabled())
				logger.debug("Current release folder :" + args[3] + " is empty, please provide a valid directory ");
			System.exit(1);
		}

		// look for the end path seperator
		if (!args[2].substring(args[2].length() - 1, args[2].length()).equals(File.separator))
			args[2] += File.separator;

		if (!args[3].substring(args[3].length() - 1, args[3].length()).equals(File.separator))
			args[3] += File.separator;

		Props props = new Props();

		props.setCurRelDate(args[0]);
		props.setReleaseName(args[1]);
		props.setPrevReleaseDir(args[2]);
		props.setCurrReleaseDir(args[3]);
		props.setReportName(args[4]);

		logger.info("FileQA PROPERTIES");
		logger.info("Release Date               :" + props.getCurRelDate());
		logger.info("Release Name              :" + props.getReleaseName());
		logger.info("Previous Release Folder   :" + props.getPrevReleaseDir());
		logger.info("Current Release Folder    :" + props.getCurrReleaseDir());
		logger.info("Report Name              :" + props.getReportName());

		if (logger.isDebugEnabled())
			dumpProps(props);

		try {

			QA.execute(props, prevDir, currDir);

		} catch (InvalidPropertiesFormatException e) {
			logger.error("Message : ", e);
		} catch (IOException e) {
			logger.error("Message : ", e);
		} catch (ParserConfigurationException e) {
			logger.error("Message : ", e);
		} catch (SAXException e) {
			logger.error("Message : ", e);
		} catch (Exception e) {
			logger.error("Message : ", e);
		}

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

	}
}
