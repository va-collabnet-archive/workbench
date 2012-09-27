/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;

/**
 * The Class ExcelExportUtil.
 */
public class ExcelExportUtil {
	
	/**
	 * Exort rules context.
	 *
	 * @param data the data
	 * @param header the header
	 * @param selectedPackage the selected package
	 * @param selectedContext the selected context
	 * @return the file
	 */
	public File exortRulesContext(Object[][] data, String[] header, RulesDeploymentPackageReference selectedPackage, I_GetConceptData selectedContext) {
		File rulesExcelFile = null;
		try {
			Workbook wb = new XSSFWorkbook();

			createRulesSheet(data, header, wb, selectedPackage, selectedContext);

			rulesExcelFile = File.createTempFile("RulesExport", ".xlsx");
			FileOutputStream fileOut = new FileOutputStream(rulesExcelFile);
			wb.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		return rulesExcelFile;
	}

	/**
	 * Creates the rules sheet.
	 *
	 * @param data the data
	 * @param header the header
	 * @param wb the wb
	 * @param selectedPackage the selected package
	 * @param selectedContext the selected context
	 */
	private void createRulesSheet(Object[][] data, String[] header, Workbook wb, RulesDeploymentPackageReference selectedPackage, I_GetConceptData selectedContext) {
		Sheet rulesSheet = wb.createSheet("rules");

		CellStyle headerCellStyle = getHeaderCellStyle(wb);

		Row packageHeaderRow = rulesSheet.createRow(0);
		Cell packageNameHeaderCell = packageHeaderRow.createCell(0);
		packageNameHeaderCell.setCellStyle(headerCellStyle);
		packageNameHeaderCell.setCellValue("Package Name");

		Cell packageUUIDHeaderCell = packageHeaderRow.createCell(1);
		packageUUIDHeaderCell.setCellStyle(headerCellStyle);
		packageUUIDHeaderCell.setCellValue("Package UUID");

		Cell packageUrlHeaderCell = packageHeaderRow.createCell(2);
		packageUrlHeaderCell.setCellStyle(headerCellStyle);
		packageUrlHeaderCell.setCellValue("Package URL");

		Cell contextNameHeaderCell = packageHeaderRow.createCell(3);
		contextNameHeaderCell.setCellStyle(headerCellStyle);
		contextNameHeaderCell.setCellValue("Context Name");

		Cell contextUUIDHeaderCell = packageHeaderRow.createCell(4);
		contextUUIDHeaderCell.setCellStyle(headerCellStyle);
		contextUUIDHeaderCell.setCellValue("Context UUID");
		if (selectedPackage != null && selectedContext != null) {
			Row packageDataRow = rulesSheet.createRow(1);
			Cell packageNameCell = packageDataRow.createCell(0);
			packageNameCell.setCellValue(selectedPackage.getName());

			Cell packageUUIDCell = packageDataRow.createCell(1);
			try {
				packageUUIDCell.setCellValue(selectedPackage.getUuids().iterator().next().toString());
			} catch (Exception e) {
				packageUUIDCell.setCellValue("");
			}

			Cell packageUrlCell = packageDataRow.createCell(2);
			packageUrlCell.setCellValue(selectedPackage.getUrl());
			Cell contextNameCell = packageDataRow.createCell(3);
			try {
				contextNameCell.setCellValue(selectedContext.getInitialText());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}

			Cell contextUUIDCell = packageDataRow.createCell(4);

			try {
				contextUUIDCell.setCellValue(selectedContext.getUids().get(0).toString());
			} catch (IOException e) {
				contextUUIDCell.setCellValue("");
			}
		}

		if (header != null) {
			Row rulesHeaderRow = rulesSheet.createRow(3);
			for (int i = 0; i < header.length - 2; i++) {
				Cell c = rulesHeaderRow.createCell(i);
				c.setCellStyle(headerCellStyle);
				c.setCellValue(header[i]);
			}
		}
		if (data != null) {
			for (int i = 0; i < data.length; i++) {
				Object[] rowData = data[i];
				Row dataRow = rulesSheet.createRow(i + 4);
				for (int j = 0; j < rowData.length - 2; j++) {
					Cell dataCell = dataRow.createCell(j);
					dataCell.setCellValue(rowData[j].toString());
				}
			}
		}
		rulesSheet.autoSizeColumn(0); // adjust width of the first column
		rulesSheet.autoSizeColumn(1); // adjust width of the second column
		rulesSheet.autoSizeColumn(3);
		rulesSheet.autoSizeColumn(4);
	}

	/**
	 * Gets the header cell style.
	 *
	 * @param wb the wb
	 * @return the header cell style
	 */
	private static CellStyle getHeaderCellStyle(Workbook wb) {
		CellStyle headerCellStyle = wb.createCellStyle();
		headerCellStyle.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		headerCellStyle.setBorderBottom(CellStyle.BORDER_THICK);
		headerCellStyle.setBorderTop(CellStyle.BORDER_THIN);
		headerCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 16);
		font.setFontName("Arial Narrow");
		headerCellStyle.setFont(font);

		return headerCellStyle;
	}
}
