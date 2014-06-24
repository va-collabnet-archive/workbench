package org.ihtsdo.rf2.fileqa.util;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import org.ihtsdo.rf2.fileqa.model.MessageType;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class WriteExcel {

	private static WritableWorkbook workbook = null;
	private WritableCellFormat arialBold;
	private WritableCellFormat arial;
	private WritableCellFormat arialBoldRed;
	private String inputFile;
	private static int row = 0;

	public void setOutputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void write() throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Report", 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet);
	}

	public void close() throws IOException, WriteException {

		workbook.write();
		workbook.close();
	}

	private void createLabel(WritableSheet sheet) throws WriteException {

		// Lets create a arial font
		WritableFont arial10pt = new WritableFont(WritableFont.ARIAL, 10);
		// Define the cell format
		arial = new WritableCellFormat(arial10pt);
		arial.setWrap(false);

		// Create create a bold font with unterlines
		WritableFont arial10ptBold = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false);
		arialBold = new WritableCellFormat(arial10ptBold);
		arialBold.setWrap(false);

		// Create create a bold font with bold RED
		WritableFont arial10ptBoldRed = new WritableFont(WritableFont.ARIAL,
				10, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);

		arialBoldRed = new WritableCellFormat(arial10ptBoldRed);
		arialBoldRed.setWrap(false);

		CellView cv = new CellView();
		cv.setFormat(arial);
		cv.setAutosize(true);

		// Write a few headers
		addCaption(sheet, 0, row, "Test Name");
		addCaption(sheet, 1, row, "Release");
		addCaption(sheet, 2, row, "Status");
		addCaption(sheet, 3, row, "Issue");
		addCaption(sheet, 4, row, "File Name");
		row++;

	}

	public void addHeaderRow(String string) {
		try {
			addCaption(workbook.getSheet(0), 0, row, string);
			row++;
		} catch (RowsExceededException e) {
			try {
				this.close();
			} catch (WriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("FATAL ERROR, Can't proceed :" + e.getMessage());
			System.exit(1);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addRow(MessageType messageType, String string) {

		try {
			StringTokenizer st = new StringTokenizer(string, ",");

			int col = 0;
			while (st.hasMoreTokens()) {
				String token = (String) st.nextToken();
				switch (messageType) {
				case SUCCESS:
					addLabel(workbook.getSheet(0), col, row, token);
					break;
				case FAILURE:
					addFailureLabel(workbook.getSheet(0), col, row, token);
					break;
				}

				col++;
			}
			row++;
		} catch (RowsExceededException e) {
			try {
				this.close();
			} catch (WriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			System.out.println("FATAL ERROR, Can't proceed :" + e.getMessage());
			System.exit(1);
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, arialBold);
		sheet.addCell(label);
	}

	@SuppressWarnings("unused")
	private void addNumber(WritableSheet sheet, int column, int row,
			Integer integer) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, integer, arial);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, arial);
		sheet.addCell(label);
	}

	private void addFailureLabel(WritableSheet sheet, int column, int row,
			String s) throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, arialBoldRed);
		sheet.addCell(label);
	}
}
