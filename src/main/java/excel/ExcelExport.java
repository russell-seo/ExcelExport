package excel;

import excel.annotation.ExcelColumn;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExcelExport<T> {
    private static final int ROW_START_INDEX = 0;
    private static final int COLUMN_START_INDEX = 0;

    private SXSSFWorkbook wb;
    private SXSSFSheet sheet;


    public ExcelExport(List<T> data, Class<T> type) {
        this.wb = new SXSSFWorkbook();
        renderExcel(data, type);
    }

    private void renderExcel(List<T> data, Class<T> objectType){
        sheet = wb.createSheet();
        createHeaders(sheet, ROW_START_INDEX, COLUMN_START_INDEX, objectType);

        if(data.isEmpty()) return;

        int rowIndex = ROW_START_INDEX + 1;

        for (T datum : data) {
            createBody(datum, rowIndex++, COLUMN_START_INDEX, objectType);
        }

    }


    /**
     * Set Excel Header
     * Get from Class Annotation @HeaderName
     */
    private void createHeaders(SXSSFSheet sheet, int rowStartIndex, int columnStartIndex, Class<T> objectType) {
        SXSSFRow row = sheet.createRow(rowStartIndex);

        for (Field declaredField : objectType.getDeclaredFields()) {
            SXSSFCell cell = row.createCell(columnStartIndex++);
            ExcelColumn annotation = declaredField.getAnnotation(ExcelColumn.class);
            cell.setCellValue(annotation.HeaderName());
        }
    }

    /**
     * Set Excel Data Row
     */
    private void createBody(Object obj, int i, int columnStartIndex,  Class<T> objectType) {
        SXSSFRow row = sheet.createRow(i);

        for (Field declaredField : objectType.getDeclaredFields()) {
            SXSSFCell cell = row.createCell(columnStartIndex++);
            Field field = getField(obj.getClass(), declaredField.getName());
            ExcelColumn annotation = declaredField.getAnnotation(ExcelColumn.class);
            field.setAccessible(true);

            try {
                Object value = field.get(obj);
                addCellValueByType(value, cell, annotation.DataType());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private Field getField(Class<?> aClass, String name) {
        try {
            return aClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

    private void addCellValueByType(Object value, SXSSFCell cell, ExportColumn.DataType dataType){
        if(ExportColumn.DataType.currency.equals(dataType)){
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue(NumberFormat.getCurrencyInstance(new Locale("en", "US")).format(value));
        }else if(ExportColumn.DataType.date.equals(dataType)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            cell.setCellValue(sdf.format(value));
        }else if(ExportColumn.DataType.datetime.equals(dataType)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cell.setCellValue(sdf.format(value));
        }else if(ExportColumn.DataType.percentage.equals(dataType)){
            Double d = Double.valueOf(value.toString()) * 100;
            cell.setCellValue(Math.floor(d * 100)/100 + "%");
        }else{
            cell.setCellValue(value == null ? "" : value.toString());
        }

    }

    public void write(HttpServletResponse res, String fileName) throws IOException {
        res.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "UTF-8") + ";");
        wb.write(res.getOutputStream());
        wb.close();
        res.getOutputStream().close();
    }
}
