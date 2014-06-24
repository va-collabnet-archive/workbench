package org.ihtsdo.rf2.fileqa.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.model.Header;
import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;

public class ColumnHeaderTest {

	private static String failedColumnHeader = null;
	private static Header header = new Header();

	public static Header getColumnHeader(File currFile, Metadata qa,
			final Logger logger, final WriteExcel writeExcel,
			ColumnHeaderRuleEnum columnHeaderRule) throws IOException {

		header.init();

		int lineCount = 0;
		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(currFile.getAbsoluteFile()));

			while (lineCount < 1) {
				header.setData(br.readLine());
				lineCount++;

				if (header.getData() != null) {
					header.setSize(header.getData().length());
					if (logger.isDebugEnabled()) {
						logger.debug("Header :" + header.getData());
						logger.debug("Size :" + header.getSize());
						logger.debug("Column data");
					}

					if (header.getSize() > 0) {

						Scanner lineScanner = new Scanner(header.getData());
						lineScanner.useDelimiter(qa.getFile().getDelimiter());

						int i = 0;
						int headerCount = 0;
						boolean loop = true;

						while (lineScanner.hasNext() && loop) {
							String columnData = lineScanner.next();
							header.setCount(++headerCount);

							if (qa.getColumn() != null
									&& i < qa.getColumn().size()
									&& qa.getColumn().get(i) != null) {
								switch (columnHeaderRule) {
								case EMPTY: {
									header.setPresent(true);
									loop = false;
									break;
								}
								case PRESENT_RULE: {

									if (qa.getColumn().get(i).getHeader()
											.equalsIgnoreCase(columnData))
										header.setPresent(true);
									else
										header.setPresent(false);

									break;
								}
								case SPELL_CHECK_RULE: {

									if (qa.getColumn().get(i).getHeader()
											.equals(columnData))
										header.setPresent(true);
									else {
										header.setPresent(false);
										loop = false;
										failedColumnHeader = columnData;
									}
									break;
								}
								}

								if (logger.isDebugEnabled()) {
									logger
											.debug("Header : "
													+ qa.getColumn().get(i)
															.getHeader());
									logger.debug("File Column Data : "
											+ columnData);
								}
								i++;
							}
						}
						lineScanner.close();
					}
				}
			}
		} catch (FileNotFoundException e) {
			switch (columnHeaderRule) {
			case EMPTY:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderEmptyTest,Current,Failed,"
								+ e.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case PRESENT_RULE:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderPresentTest,Current,Failed,"
								+ e.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case COLUMN_COUNT_CHECK:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderCountTest,Current,Failed,"
								+ e.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case DELIMITER_CHECK:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderSeperatorTest,Current,Failed,"
								+ e.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case SPELL_CHECK_RULE:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderSpellCheckTest,Current,Failed,"
								+ e.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			}
		} catch (IOException ioe) {
			switch (columnHeaderRule) {

			case PRESENT_RULE:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderPresentTest,Current,Failed,"
								+ ioe.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case COLUMN_COUNT_CHECK:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderCountTest,Current,Failed,"
								+ ioe.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			case DELIMITER_CHECK:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderSeperatorTest,Current,Failed,"
								+ ioe.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;

			case SPELL_CHECK_RULE:
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnSpellCheckTest,Current,Failed,"
								+ ioe.getMessage() + ","
								+ currFile.getAbsoluteFile());
				break;
			}
		} finally {
			br.close();
		}
		return header;
	}

	public static boolean execute(final Metadata qa, final File currFile,
			final Logger logger, final WriteExcel writeExcel,
			ColumnHeaderRuleEnum columnHeaderRule) throws IOException {

		boolean passed = false;

		if (logger.isDebugEnabled()) {
			logger.debug("Current Release File Name : " + currFile.getName());
		}

		switch (columnHeaderRule) {
		case EMPTY:
			header = getColumnHeader(currFile, qa, logger, writeExcel,
					columnHeaderRule);
			if (header.isPresent()) {
				passed = true;
			} else {
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderEmptyTest,Current,Failed,Header row is empty,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Column(s) Header EMPTY ");
			}
			break;
		case PRESENT_RULE:
			header = getColumnHeader(currFile, qa, logger, writeExcel,
					columnHeaderRule);
			if (header.isPresent()) {
				writeExcel.addRow(MessageType.SUCCESS,
						"ColumnHeaderPresentTest,Current,Passed, ,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Column(s) Header(s) Present");
				passed = true;
			} else {
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderPresentTest,Current,Failed,Header Not Present,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Column(s) Header NOT Present");
			}
			break;
		case SPELL_CHECK_RULE:
			header = getColumnHeader(currFile, qa, logger, writeExcel,
					columnHeaderRule);
			if (header.isPresent()) {
				writeExcel.addRow(MessageType.SUCCESS,
						"ColumnHeaderSpellCheckTest,Current,Passed, ,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Column(s) Header Spell Check PASSED");
				passed = true;
			} else {
				writeExcel.addRow(MessageType.FAILURE,
						"ColumnHeaderSpellCheckTest,Current,Failed,Column Header :"
								+ failedColumnHeader
								+ " Spelling is not correct,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Column(s) Header Spell Check FAILED");
			}
			break;
		case COLUMN_COUNT_CHECK:
			header = getColumnHeader(currFile, qa, logger, writeExcel,
					columnHeaderRule);

			if (header.getCount() == qa.getColumn().size()) {
				writeExcel.addRow(MessageType.SUCCESS,
						"ColumnHeaderCountTest,Current,Passed, ,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Header Column Count does match");
				passed = true;
			} else {
				writeExcel
						.addRow(
								MessageType.FAILURE,
								"ColumnHesderCountTest,Current,Failed,Column count does not match the metadata column count,"
										+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Header Column Count does not  match");
			}
			break;
		case DELIMITER_CHECK:
			header = getColumnHeader(currFile, qa, logger, writeExcel,
					columnHeaderRule);

			if (header.getCount() == qa.getColumn().size()) {
				writeExcel.addRow(MessageType.SUCCESS,
						"ColumnHeaderSeperatorTest,Current,Passed, ,"
								+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Header Column Header Sperator is correct");
				passed = true;
			} else {
				writeExcel
						.addRow(
								MessageType.FAILURE,
								"ColumnHesderSeperatorTest,Current,Failed,Column count does not match the metadata column count,"
										+ currFile.getAbsoluteFile());
				if (logger.isDebugEnabled())
					logger.debug("Header Column Header Seperator is not correct");
			}
			break;
		}
		return passed;
	}
}
