package iot.lane.alipaycontest.firstseason;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DataAnalyze {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			 java.util.Date mDate = MYSQLCONFIG.dateFormat.parse("2013-07-15");
			java.util.Date aDate=MYSQLCONFIG.dateFormat.parse("2013-07-25");
			long k=(mDate.getTime()-aDate.getTime())/(1000*60*60*24);
			Math.abs(k);
			int i = 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 test();
		ExcelWriter writer = null;
		try {
			writer = new ExcelWriter("D:\\test\\test.xlsx");
		} catch (IOException e) {
		}

		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Calendar tempEarlierCal = Calendar.getInstance();
		try {
			tempEarlierCal.setTime(format1.parse(MYSQLCONFIG.DateThreshold));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tempEarlierCal.add(Calendar.MONTH, 1);
		Date tempSqlDate = new java.sql.Date(tempEarlierCal.getTime().getTime());
		tempEarlierCal.add(Calendar.MONTH, 1);
		tempEarlierCal.add(Calendar.DAY_OF_YEAR, 0);
		String Cal = tempEarlierCal.toString();
		java.util.Date tempSqlDate2 = tempEarlierCal.getTime();

		LinkedList<Integer> resultItems = new LinkedList<Integer>();
		resultItems.add(4);
		resultItems.add(234);
		resultItems.add(77);
		resultItems.add(2);
		boolean contains = resultItems.contains(77);

		System.out.println("results: ");
	}

	public static void test() {

		Hashtable<Integer, Integer> map = new Hashtable<Integer, Integer>();
		ValueComparator bvc = new ValueComparator(map);
		TreeMap<Integer, Integer> sorted_map = new TreeMap<Integer, Integer>(
				bvc);

		map.put(12, 99);
		map.put(2, 67);
		map.put(56, 67);
		map.put(32, 67);

		Enumeration<Integer> key = map.keys();
		while (key.hasMoreElements()) {
			int myk = key.nextElement();
			int j = map.get(myk);
			int i = 1;
		}
		// System.out.println("unsorted map: "+map);

		sorted_map.putAll(map);

		System.out.println("results: " + sorted_map);
	}

}

class ValueComparator implements Comparator<Integer> {

	Map<Integer, Integer> base;

	public ValueComparator(Map<Integer, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Integer a, Integer b) {
		if (base.get(a) >= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}

class ExcelWriter {

	private OutputStream outputStream = null;
	private String sheetName = "sheet1";
	private XSSFWorkbook workbook;

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * @param fileOutputStream
	 * @param forName
	 * @throws IOException
	 */
	public ExcelWriter(String filePath) throws IOException {
		workbook = new XSSFWorkbook();
		this.outputStream = new FileOutputStream(filePath);
	}

	public void close() {
		try {
			workbook.write(outputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void writeRecord(String[] strings) throws IOException {
		int sheetNum = workbook.getNumberOfSheets();
		XSSFSheet sheet = null;
		if (sheetNum == 0) {
			sheet = workbook.createSheet();
			workbook.setSheetName(0, sheetName);
		} else {
			sheet = workbook.getSheetAt(0);
		}
		XSSFCell cell = null;
		XSSFCellStyle cs = null;
		XSSFRichTextString xssfValue = null;
		int rowNum = sheet.getLastRowNum();
		rowNum++;
		XSSFRow row = sheet.createRow(rowNum);
		for (int n = 0; n < strings.length; n++) {// 写出列
			cell = row.createCell(n);
			cs = cell.getCellStyle();
			cs.setFillPattern(XSSFCellStyle.ALIGN_GENERAL);
			cs.setWrapText(true);
			cs.setVerticalAlignment(XSSFCellStyle.ALIGN_LEFT);
			cell.setCellStyle(cs);
			cell.setCellType(XSSFCell.CELL_TYPE_STRING);
			xssfValue = new XSSFRichTextString(strings[n]);
			cell.setCellValue(xssfValue);
		}
	}
}