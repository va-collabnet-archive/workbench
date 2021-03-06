package org.ihtsdo.rf2.module.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.core.dao.ConceptDAO;

// TODO: Auto-generated Javadoc
/**
 * The Class RF2Writer.
 */
public class RF2Writer {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2Writer.class);

	/**
	 * Write header.
	 *
	 * @param bw the bw
	 * @param columnsList the columns list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeHeader(BufferedWriter bw, ArrayList<Column> columnsList) throws IOException {

		for (int i = 0; i < columnsList.size() - 1; i++) {

			Column column = columnsList.get(i);

			bw.write(column.getName());
			bw.write("\t");
			// conFileWriter.write(column.getDelimiter());
		}

		Column column = columnsList.get(columnsList.size() - 1);
		bw.write(column.getName());

		bw.write("\r\n");
	}

	/**
	 * Write data.
	 *
	 * @param bw the bw
	 * @param columnsList the columns list
	 * @param conceptExport the concept export
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public static void writeData(BufferedWriter bw, ArrayList<Column> columnsList, ConceptDAO conceptExport) throws IOException {

		try {

			Class cls = Class.forName(ConceptDAO.class.getName());

			for (int i = 0; i < columnsList.size() - 1; i++) {

				Column column = columnsList.get(i);
				String methodName = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);

				Method m = cls.getMethod("get" + methodName);
				Object retobj = m.invoke(conceptExport);

				bw.write((String) retobj);
				bw.write("\t");

				// conFileWriter.write(column.getDelimiter());
			}

			Column column = columnsList.get(columnsList.size() - 1);

			String methodName = column.getName().substring(0, 1).toUpperCase() + column.getName().substring(1);

			Method m = cls.getMethod("get" + methodName);
			Object retobj = m.invoke(conceptExport);

			bw.write((String) retobj);
			bw.write("\r\n");

		} catch (Exception e) {
			logger.error(e.getMessage());
			System.exit(0);
		}
	}
}
