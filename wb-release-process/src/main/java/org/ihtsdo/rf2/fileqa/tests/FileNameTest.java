package org.ihtsdo.rf2.fileqa.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;
import org.ihtsdo.rf2.fileqa.model.Regex;
import org.ihtsdo.rf2.fileqa.util.WriteExcel;


public class FileNameTest {

	public static boolean execute(final Metadata qa, final File currFile,
			final File prevFile, final Logger logger,
			final WriteExcel writeExcel) throws IOException {

		boolean passed = false;

		if (!qa.getFile().getCarryForward().equals("true")) {
			if (logger.isDebugEnabled()) {
				logger.debug("File Carry Forward Set to FALSE");
				logger.debug("Executing File Name Rule");
				logger.debug("Processing File Name : " + currFile.getName());
				logger.debug("File MetaData Regex : "
						+ qa.getFile().getRegex());
			}

			ArrayList<Regex> regexList = qa.getFile().getRegex();

			for (int i = 0; i < regexList.size(); i++) {

				boolean match = false;
				try {
					match = currFile.getName().matches(
							qa.getFile().getRegex().get(i).getExpression());
				} catch (PatternSyntaxException e) {
					writeExcel
							.addRow(MessageType.FAILURE, qa.getFile()
									.getRegex().get(i).getTest()
									+ ",Current,Failed,Regex: "
									+ regexList.get(i).getExpression()
									+ " :"
									+ e.getMessage()
									+ ","
									+ currFile.getAbsoluteFile());
				}

				if (match) {
					writeExcel.addRow(MessageType.SUCCESS, qa.getFile()
							.getRegex().get(i).getTest()
							+ ",Current,Passed, ,"
							+ "Regex :"
							+ regexList.get(i).getExpression()
							+ " File : "
							+ currFile.getAbsoluteFile());
					writeExcel.addRow(MessageType.SUCCESS, qa.getFile()
							.getRegex().get(i).getTest()
							+ ",Previous,Not tested, ,"
							+ prevFile.getAbsoluteFile());

					if (logger.isDebugEnabled())
						logger.debug("Release file name," + currFile.getName()
								+ "," + regexList.get(i).getSuccessMessage()
								+ regexList.get(i).getExpression());
					passed = true;
				} else {
					writeExcel.addRow(MessageType.FAILURE, qa.getFile()
							.getRegex().get(i).getTest()
							+ ",Current,Failed,Regex: "
							+ regexList.get(i).getExpression()
							+ " don't match," + currFile.getAbsoluteFile());
					writeExcel.addRow(MessageType.SUCCESS, qa.getFile()
							.getRegex().get(i).getTest()
							+ ",Previous,Not tested, ,"
							+ prevFile.getAbsoluteFile());
					if (logger.isDebugEnabled())
						logger.debug("Release file name," + currFile.getName()
								+ "," + regexList.get(i).getFailureMessage()
								+ regexList.get(i).getExpression());
				}
			}
		} else if (logger.isDebugEnabled())
			logger.debug("File Carry Forward Set to TRUE");
		return passed;
	}
}
