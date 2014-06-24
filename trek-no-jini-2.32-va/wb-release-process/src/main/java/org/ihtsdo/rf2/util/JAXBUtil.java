package org.ihtsdo.rf2.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBUtil {

	public static Config getConfig(String fileName) {

		Config config = null;
		try {

			JAXBContext context = JAXBContext.newInstance(Config.class);
			Unmarshaller u = context.createUnmarshaller();

			InputStream is = JAXBUtil.class.getResourceAsStream(fileName);

			config = (Config) u.unmarshal(is);

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return config;
	}

	public static void createConceptConfig(String[] args) {

		try {
			JAXBContext context = JAXBContext.newInstance(Config.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			Config config = new Config();

			// Set the file name
			config.setExportFileName("xsct2_Concept_Full_INT_");

			ArrayList<Column> columns = new ArrayList<Column>();

			Column column = new Column();

			column.setName("id");
			column.setDescripton("");
			column.setDelimiter("\\t");
			columns.add(column);

			column = new Column();
			column.setName("effectiveTime");
			column.setDescripton("");
			column.setDelimiter("\\t");
			columns.add(column);

			column = new Column();
			column.setName("active");
			column.setDescripton("");
			column.setDelimiter("\\t");
			columns.add(column);

			column = new Column();
			column.setName("moduleId");
			column.setDescripton("");
			column.setDelimiter("\\t");
			columns.add(column);

			column = new Column();
			column.setName("definitionStatusId");
			column.setDescripton("");
			column.setDelimiter("\\r\\n");
			columns.add(column);

			config.setColumn(columns);

			// create the concept config XML
			OutputStream os = new FileOutputStream("src/main/resources/org/ihtsdo/rf2/core/config/concept_1.xml");
			m.marshal(config, os);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
