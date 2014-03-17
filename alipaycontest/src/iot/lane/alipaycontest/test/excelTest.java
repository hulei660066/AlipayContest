package iot.lane.alipaycontest.test;

import iot.lane.alipaycontest.test.ETLCONFIG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class excelTest {
	public static void main(String[] args) {
		Date date = null;
		String str = "2007-1-18";
		String str2 = "2007-2-18";
		int com = str.compareTo(str2);
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");      
		
		try {
			date = format1.parse(str);
			Calendar tempEarlierCal = Calendar.getInstance();
			tempEarlierCal.setTime(date);
			tempEarlierCal.add(Calendar.DAY_OF_YEAR, 3);
			str=format1.format(tempEarlierCal.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<Integer, Object[]> data = new HashMap<Integer, Object[]>();
		data.put(1, new Object[] { "Emp No.", "Name", "Salary" });
		data.put(2, new Object[] { 1d, "John", 1500000d });
		data.put(3, new Object[] { 2d, "Sam", 800000d });
		data.put(4, new Object[] { 3d, "Dean", 700000d });

		write2ExcelFile(data);
	}

	public static void write2ExcelFile(Map<Integer, Object[]> data) {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sample sheet");

		Set<Integer> keyset = data.keySet();
		int rownum = 0;
		for (Integer key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = data.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date)
					cell.setCellValue((Date) obj);
				else if (obj instanceof Boolean)
					cell.setCellValue((Boolean) obj);
				else if (obj instanceof String)
					cell.setCellValue((String) obj);
				else if (obj instanceof Double)
					cell.setCellValue((Double) obj);
			}
		}

		try {
			FileOutputStream out = new FileOutputStream(new File(
					ETLCONFIG.TMPPATH + data.get(0).getClass().getName()
							+ ".xls"));
			workbook.write(out);
			out.close();
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}