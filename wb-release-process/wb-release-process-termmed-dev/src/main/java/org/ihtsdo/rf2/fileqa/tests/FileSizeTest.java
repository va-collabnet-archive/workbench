package org.ihtsdo.rf2.fileqa.tests;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;


public class FileSizeTest {
	public static long getFileSize(File file, Metadata qa, final Logger logger,
			final WriteExcel writeExcel) {

		long fileSize = file.length();
		if (logger.isDebugEnabled()) {
			logger.debug("File :" + file.getName());
			logger.debug("Size :" + fileSize);
		}

		return fileSize;
	}

	public static boolean execute(final Metadata qa, final File currFile,
			File prevFile, final Logger logger, final WriteExcel writeExcel)
			throws IOException {

		boolean passed = false;

		if (logger.isDebugEnabled()) {
			logger.debug("Executing File Size Rule");
			logger.debug("Current Release File Name : " + currFile.getName());
			logger.debug("Previous Release File Name: " + prevFile.getName());
		}

		long prevFileSize = getFileSize(prevFile, qa, logger, writeExcel);
		long currFileSize = getFileSize(currFile, qa, logger, writeExcel);

		if (currFileSize != prevFileSize) {
			if (qa.getFile().getCarryForward().equals("true")) {
				writeExcel
						.addRow(MessageType.FAILURE,"FileSizeTest,Current,Failed,Carry forward set to TRUE Bytes: "
								+ currFileSize
								+ " Size don't match,"
								+ currFile.getAbsoluteFile());
				writeExcel.addRow(MessageType.FAILURE,"FileSizeTest,Previous,Failed,Carry forward set to TRUE Bytes: "
								+ prevFileSize
								+ " Size don't match,"
								+ prevFile.getAbsoluteFile());
			} else {
				writeExcel.addRow(MessageType.SUCCESS,"FileSizeTest,Current,Passed, ,"
						+ currFile.getAbsoluteFile());
				writeExcel.addRow(MessageType.SUCCESS,"FileSizeTest,Previous,Passed, ,"
						+ prevFile.getAbsoluteFile());
			}

			if (logger.isDebugEnabled())
				logger
						.debug("Previous and Current Release File Size DONT match");
		} else {
			if (qa.getFile().getCarryForward().equals("false")) {
				writeExcel.addRow(MessageType.FAILURE,"FileSizeTest,Current,Failed,Carry forward set to FALSE Bytes: "
								+ currFileSize
								+ " Size do match,"
								+ currFile.getAbsoluteFile());
				writeExcel.addRow(MessageType.FAILURE,"FileSizeTest,Previous,Failed,Carry forward set to FASLE Bytes: "
								+ prevFileSize
								+ " Size do match,"
								+ prevFile.getAbsoluteFile());
			} else {
				writeExcel.addRow(MessageType.SUCCESS,"FileSizeTest,Current,Passed, ,"
						+ currFile.getAbsoluteFile());
				writeExcel.addRow(MessageType.SUCCESS,"FileSizeTest,Previous,Passed, ,"
						+ prevFile.getAbsoluteFile());
			}
			if (logger.isDebugEnabled())
				logger.debug("Previous and Current Release File Size DO match");
			passed = true;
		}

		return passed;
	}
}
