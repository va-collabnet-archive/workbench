package org.ihtsdo.rf2.fileqa.action;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import jxl.write.WriteException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.model.Column;
import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;
import org.ihtsdo.rf2.fileqa.model.Props;
import org.ihtsdo.rf2.fileqa.tests.ColumnDataTests;
import org.ihtsdo.rf2.fileqa.tests.ColumnHeaderRuleEnum;
import org.ihtsdo.rf2.fileqa.tests.ColumnHeaderTest;
import org.ihtsdo.rf2.fileqa.tests.FileNameTest;
import org.ihtsdo.rf2.fileqa.tests.FileSizeTest;
import org.ihtsdo.rf2.fileqa.util.DateUtils;
import org.ihtsdo.rf2.fileqa.util.JAXBUtil;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;
import org.xml.sax.SAXException;

public class QA {

	private static Logger logger = Logger.getLogger(QA.class.getName());
	private static Metadata metadata;
	private static WriteExcel writeExcel = null;

	public static void execute(final Props props, final File prevDir, final File currDir) throws ParserConfigurationException, SAXException, IOException, Exception {

		writeExcel = new WriteExcel();
		writeExcel.setOutputFile(props.getReportName());

		try {
			writeExcel.write();
		} catch (WriteException e) {
			logger.info("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
		} catch (IOException e) {
			logger.info("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
			System.exit(1);
		} catch (NullPointerException e) {
			logger.info("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Cannot create report file :" + props.getReportName() + " " + e.getMessage());
			System.exit(1);
		}

		logger.info("");
		logger.info("Opened Report File        :" + props.getReportName());
		if (logger.isDebugEnabled()) {
			logger.debug("Opened Report File :" + props.getReportName());
			logger.debug("");
		}
		logger.info("");

		try {
			processFolders(props, prevDir, currDir);
		} catch (Exception e) {
			logger.error("Message : ", e);

		} finally {
			try {
				writeExcel.close();
			} catch (WriteException e) {
				logger.error("Message : ", e);
			} catch (IOException e) {
				logger.error("Message : ", e);
			}
		}
	}

	public static void processFolders(final Props props, final File prevDir, final File currDir) throws ParserConfigurationException, SAXException, IOException, Exception {

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		};

		String[] currFiles = currDir.list(filter);
		String[] prevFiles = prevDir.list(filter);
		String cS = null;

		// write an empty line for the report
		writeExcel.addRow(MessageType.SUCCESS, " , , ");

		// check for files previously released but not found in current
		for (int i = 0; i < prevFiles.length; i++) {
			boolean foundCurrMatch = false;
			String currMatch = prevFiles[i].substring(0, prevFiles[i].length() - 12);
			for (int j = 0; j < currFiles.length && !foundCurrMatch; j++) {
				if ((prevFiles[i].substring(0, prevFiles[i].length() - 12)).contains(currFiles[j].substring(0, currFiles[j].length() - 12))) {
					currMatch = currFiles[j].substring(0, currFiles[j].length() - 12);
					foundCurrMatch = true;
				}
			}

			if (!foundCurrMatch) {
				writeExcel.addRow(MessageType.FAILURE, "FileTest,Current,Failed,File is missing in folder :" + props.getCurrReleaseDir() + currMatch + "YYYYMMDD.txt");
				writeExcel.addRow(MessageType.SUCCESS, "FileTest,Previous,Passed, ," + props.getPrevReleaseDir() + prevFiles[i]);
				writeExcel.addRow(MessageType.SUCCESS, " , , ");
			}
		}

		// start processing the current release files
		for (int i = 0; i < currFiles.length; i++) {

			boolean foundPrevMatch = false;
			writeExcel.addHeaderRow(currFiles[i]);

			for (int j = 0; j < prevFiles.length && !foundPrevMatch; j++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Current File :" + currFiles[i]);
					logger.debug("Previous File :" + prevFiles[j]);
				}

				// stripping the date portion and the extension
				// eg. _20101007.txt
				cS = currFiles[i].substring(0, currFiles[i].length() - 12);
				String pS = prevFiles[j].substring(0, prevFiles[j].length() - 12);

				if (cS.contains(pS)) {

					Date sDate = new Date();
					foundPrevMatch = true;

					// look for a metadata file matching the file name
					// excluding the date and the extension
					// eg. exclude 20101007.txt
					if (getMetadata(props, "/metadata/" + cS + "Metadata.xml")) {
						if (logger.isDebugEnabled())
							dumpMetadata();

						File currFile = new File(props.getCurrReleaseDir() + currFiles[i]);
						File prevFile = new File(props.getPrevReleaseDir() + prevFiles[j]);

						logger.info("Processing  ...        :" + currFile.getAbsoluteFile());
						if (logger.isDebugEnabled())
							logger.info("Processing File name :" + currFile.getAbsolutePath());

						// start the rule tests

						if (logger.isDebugEnabled()) {
							logger.debug("");
							logger.debug(" ========== " + "Start: File Name Match Rule " + " ========== ");
						}
						FileNameTest.execute(metadata, currFile, prevFile, logger, writeExcel);
						if (logger.isDebugEnabled()) {
							logger.debug(" ========== " + "End:   File Name Match Rule " + " ========== ");
						}

						if (logger.isDebugEnabled()) {
							logger.debug("");
							logger.debug(" ========== " + "Start: File Size Match Rule " + " ========== ");
						}

						FileSizeTest.execute(metadata, currFile, prevFile, logger, writeExcel);

						if (logger.isDebugEnabled()) {
							logger.debug(" ========== " + "End:  File Size Match Rule " + " ========== ");
						}

						if (logger.isDebugEnabled()) {
							logger.debug("");
							logger.debug(" ========== " + "Start: Column Header Empty Rule " + " ========== ");
						}
						boolean passed = ColumnHeaderTest.execute(metadata, currFile, logger, writeExcel, ColumnHeaderRuleEnum.EMPTY);

						if (logger.isDebugEnabled()) {
							logger.debug(" ========== " + "End: Column Header EMPTY Rule " + " ========== ");
						}

						// we only do other COlumn Header Tests
						// if the Columne Header is NOT empty
						if (passed) {

							if (logger.isDebugEnabled()) {
								logger.debug("");
								logger.debug(" ========== " + "Start: Column Header Seperator Rule " + " ========== ");
							}
							ColumnHeaderTest.execute(metadata, currFile, logger, writeExcel, ColumnHeaderRuleEnum.DELIMITER_CHECK);

							if (logger.isDebugEnabled()) {
								logger.debug(" ========== " + "End: Column Header Seperator Rule " + " ========== ");
							}

							if (logger.isDebugEnabled()) {
								logger.debug("");
								logger.debug(" ========== " + "Start: Column Header Count Rule " + " ========== ");
							}
							ColumnHeaderTest.execute(metadata, currFile, logger, writeExcel, ColumnHeaderRuleEnum.COLUMN_COUNT_CHECK);
							if (logger.isDebugEnabled()) {
								logger.debug(" ========== " + "End: Column Header Count Rule " + " ========== ");
								logger.debug("");
							}

							if (logger.isDebugEnabled()) {
								logger.debug("");
								logger.debug(" ========== " + "Start: Column Header Present Rule " + " ========== ");
							}
							ColumnHeaderTest.execute(metadata, currFile, logger, writeExcel, ColumnHeaderRuleEnum.PRESENT_RULE);

							if (logger.isDebugEnabled()) {
								logger.debug(" ========== " + "End: Column Header Present Rule " + " ========== ");
							}

							if (logger.isDebugEnabled()) {
								logger.debug("");
								logger.debug(" ========== " + "Start: Column Header Spell Check Rule " + " ========== ");
							}
							ColumnHeaderTest.execute(metadata, currFile, logger, writeExcel, ColumnHeaderRuleEnum.SPELL_CHECK_RULE);
							if (logger.isDebugEnabled()) {
								logger.debug(" ========== " + "End: Column Header Spell Check Rule " + " ========== ");
								logger.debug("");
							}

						}

						if (logger.isDebugEnabled()) {
							logger.debug("");
							logger.debug(" ========== " + "Start: Column Data Rules " + " ========== ");
						}
						ColumnDataTests.execute(props, metadata, currFile, logger, writeExcel);
						if (logger.isDebugEnabled()) {
							logger.debug(" ========== " + "End: Column Data Rules " + " ========== ");
							logger.debug("");
						}
						logger.info("Finished               :" + currFile.getAbsoluteFile());
						Date eDate = new Date();
						logger.info(DateUtils.elapsedTime("Elapsed                :", sDate, eDate));
						if (logger.isDebugEnabled())
							logger.debug("Finished          :" + currFile.getAbsoluteFile());
					}
				}
			}
			if (!foundPrevMatch) {
				writeExcel.addRow(MessageType.SUCCESS, "FileTest,Current,Passed, ," + props.getCurrReleaseDir() + currFiles[i]);

				writeExcel.addRow(MessageType.FAILURE, "FileTest,Previous,Failed,File is missing in folder :" + props.getPrevReleaseDir() + cS + "YYYYMMDD.txt");
			}
			// end all tests
			writeExcel.addRow(MessageType.SUCCESS, " , , ");
		}
	}

	private static boolean getMetadata(Props props, String metaDataFile) throws IOException {

		boolean success = false;

		if (metadata != null)
			metadata.init();

		logger.info("");
		logger.info("Loading  MetaData File :" + metaDataFile);

		metadata = JAXBUtil.getMetadata(metaDataFile, writeExcel);

		// check if there was an error and the metadata xml
		// was not marshalled properly
		if (metadata != null) {
			success = true;
			// sort the list of columns based on thier position
			ArrayList<Column> columns = metadata.getColumn();
			Collections.sort(columns);
			metadata.setColumn(columns);
		} else
			logger.info("Not processing file not found");

		return success;
	}

	private static void dumpMetadata() {

		org.ihtsdo.rf2.fileqa.model.File file = metadata.getFile();
		ArrayList<Column> columns = metadata.getColumn();

		if (file != null) {
			logger.debug("METADATA Loaded into the File object");
			logger.debug(file.getDescription());
			logger.debug(file.getDelimiter());
			logger.debug(file.getEncoding());
			logger.debug(file.getRegex());
		} else
			logger.debug("File Object is null");

		columns = metadata.getColumn();

		if (columns != null) {
			logger.debug("Column object");
			for (int i = 0; i < columns.size(); i++) {

				Column column = columns.get(i);
				logger.debug("Header :" + column.getHeader());
				logger.debug("Regex :" + column.getRegex());

			}
		} else
			logger.debug("Column(s) Object is null");

		logger.debug("");
	}

}
