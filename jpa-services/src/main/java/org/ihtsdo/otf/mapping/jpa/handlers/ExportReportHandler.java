package org.ihtsdo.otf.mapping.jpa.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ihtsdo.otf.mapping.helpers.LocalException;
import org.ihtsdo.otf.mapping.jpa.services.ReportServiceJpa;
import org.ihtsdo.otf.mapping.reports.Report;
import org.ihtsdo.otf.mapping.reports.ReportResult;
import org.ihtsdo.otf.mapping.reports.ReportResultItem;

/**
 * A handler for exporting a {@link Report}.
 */
public class ExportReportHandler {

	/**
	 * Instantiates an empty {@link ExportReportHandler}.
	 */
	public ExportReportHandler() {

	}

	/**
	 * Export report.
	 *
	 * @param report
	 *            the report
	 * @return the input stream
	 * @throws Exception
	 *             the exception
	 */
	public InputStream exportReport(Report report) throws Exception {

		// Create workbook
		Workbook wb = new HSSFWorkbook();

		// Export report
		handleExportReport(report, wb);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		InputStream in = new ByteArrayInputStream(out.toByteArray());
		return in;

	}

	/**
	 * Handle export report.
	 *
	 * @param report
	 *            the report
	 * @param wb
	 *            the wb
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("static-method")
	private void handleExportReport(Report report, Workbook wb) throws Exception {
		Logger.getLogger(ReportServiceJpa.class).info("Exporting report " + report.getName() + "...");

		try {

			CreationHelper createHelper = wb.getCreationHelper();
			// Set font
			Font font = wb.createFont();
			font.setFontName("Cambria");
			font.setFontHeightInPoints((short) 11);

			// Fonts are set into a style
			CellStyle style = wb.createCellStyle();
			style.setFont(font);

			Sheet sheet = wb.createSheet("Report");

			// Create header row and add cells
			int rownum = 0;
			int cellnum = 0;
			Row row = sheet.createRow(rownum++);
			Cell cell = null;
			for (String header : new String[] { "Count", "Value" }) {
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(header));
			}

			for (ReportResult result : report.getResults()) {
				// Add data row
				cellnum = 0;
				row = sheet.createRow(rownum++);

				// Count
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(new Long(result.getCt()).toString()));

				// Value
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(result.getValue().toString()));
			}

			Sheet sheet2 = wb.createSheet("Report Results");

			// Create header row and add cells
			rownum = 0;
			cellnum = 0;
			row = sheet2.createRow(rownum++);
			for (String header : new String[] { "Count", "Value" }) {
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(header));
			}

			for (ReportResult result : report.getResults()) {
				// Add data row
				cellnum = 0;
				row = sheet2.createRow(rownum++);

				// Count
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(new Long(result.getCt()).toString()));

				// Value
				cell = row.createCell(cellnum++);
				cell.setCellStyle(style);
				cell.setCellValue(createHelper.createRichTextString(result.getValue().toString()));

				row = sheet2.createRow(rownum++);

				for (String header : new String[] { "Id", "Name" }) {
					cell = row.createCell(cellnum++);
					cell.setCellStyle(style);
					cell.setCellValue(createHelper.createRichTextString(header));
				}

				// limit results so as not to exceeed (hopefully) the 65535 row
				// limit for HSSF
				if (result.getCt() < 2000) {

					for (ReportResultItem resultItem : result.getReportResultItems()) {

                      // Add data row
                      cellnum = 2;
                      row = sheet2.createRow(rownum++);

                      // Id
                      cell = row.createCell(cellnum++);
                      cell.setCellStyle(style);
                      cell.setCellValue(createHelper.createRichTextString(resultItem.getItemId().toString()));

                      // Name
                      cell = row.createCell(cellnum++);
                      cell.setCellStyle(style);
                      cell.setCellValue(createHelper.createRichTextString(resultItem.getItemName()));
					}
				} else {
                  // Add data row
                  cellnum = 2;
                  row = sheet2.createRow(rownum++);

                  // Id
                  cell = row.createCell(cellnum++);
                  cell.setCellStyle(style);
                  cell.setCellValue(createHelper.createRichTextString("There are too many rows to export (2000 max)"));

                  // Name
                  cell = row.createCell(cellnum++);
                  cell.setCellStyle(style);
                  cell.setCellValue(createHelper.createRichTextString(String.valueOf(result.getCt())));
				  
				}
				   
			}

			for (int i = 0; i < 4; i++) {
				sheet.autoSizeColumn(i);
				sheet2.autoSizeColumn(i);
			}
		} catch (Exception e) {
			throw new LocalException(e.getMessage(), e);
		}

	}
}
