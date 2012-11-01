package org.ihtsdo.rf2.fileqa.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;


public class FileRowCountTest {
	public static long getRows(File file, Metadata qa, final Logger logger,
			final WriteExcel writeExcel) throws FileNotFoundException {

		long rows = 0;

		long fileSize = file.length();
		if (logger.isDebugEnabled()) {
			logger.debug("File :" + file.getName());
			logger.debug("Size :" + fileSize);
		}

		Scanner scanner = new Scanner(file);

		int headerSize = 0;
		int rowSize = 0;
		int count = 0;
		scanner.useDelimiter(qa.getFile().getDelimiter());
		while (scanner.hasNextLine() && count < 1) {

			String headerData = scanner.nextLine();
			headerSize = headerData.length();
			if (logger.isDebugEnabled()) {
				logger.debug("Header :" + headerData);
				logger.debug("Size :" + headerSize);
			}

			String rowData = scanner.nextLine();
			rowSize = rowData.length();

			if (logger.isDebugEnabled()) {
				logger.debug("Row :" + rowData);
				logger.debug("Size :" + rowSize);
			}
			count++;
		}

		scanner.close();

		rows = (fileSize - headerSize) / rowSize;
		if (logger.isDebugEnabled())
			logger.debug("File Rows Count :" + rows);

		return rows;
	}

	public static boolean execute(final Metadata qa, final File currFile,
			File prevFile, final Logger logger, final WriteExcel writeExcel)
			throws IOException {

		boolean passed = false;

		if (logger.isDebugEnabled()) {
			logger.debug("Executing File Row Count Rule");
			logger.debug("Current Release File Name : " + currFile.getName());
			logger.debug("Previous Release File Name: " + prevFile.getName());
		}

		long prevRows = getRows(prevFile, qa, logger, writeExcel);
		long currRows = getRows(currFile, qa, logger, writeExcel);

		if (currRows != prevRows) {
			writeExcel
					.addRow(MessageType.FAILURE,"Previous and Current Release File Row Count DONT match");
			if (logger.isDebugEnabled())
				logger
						.debug("Previous and Current Release File Row Count DONT match");
		} else {
			writeExcel
					.addRow(MessageType.SUCCESS,"Previous and Current Release File Row Count DO match");
			if (logger.isDebugEnabled())
				logger
						.debug("Previous and Current Release File Row Count DO match");
			passed = true;
		}
		return passed;
	}
}
