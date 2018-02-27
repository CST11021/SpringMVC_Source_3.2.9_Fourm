package com.whz.utils;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by whz on 2017/9/3.
 */
public class ImportAndExportExcelUtil {

    public static final String EXCEL_SUFFIX_XLS = ".xls";
    public static final String EXCEL_SUFFIX_XLSX = ".xlsx";
    public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");

    /**
     * 在应用根目录下创建一个文件夹
     * @param request
     * @param folderName 要创建的文件夹名称
     */
    private String createFolderName(HttpServletRequest request, String folderName) {
        // 创建一个文件夹，用来保存Excel文件
        String folderPath = request.getSession().getServletContext().getRealPath(folderName);
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return folderPath;
    }

    /**
     * 创建Excel文件，数据导入Excel，然后保存到指定的服务器路
     * @param headFields    表头信息
     * @param records       要导出的数据
     * @param sheetName     表示Excel页签
     * @param filepath      表示文件路径（路径 + 文件名 + 文件后缀）
     * @return Excel文件是否创建成功
     */
    public boolean creatWorkbook(List<String> headFields, List<Map<String, String>> records, String sheetName, String filepath) {
        boolean flag;

        try {
            OutputStream os = new FileOutputStream(filepath);
            // 创建一个工作簿，然后根据 title 创建一个页签
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(sheetName);
            XSSFRow row = sheet.createRow(0);

            int cell = 1;
            for (String columName : headFields) {
                row.createCell(cell).setCellValue(columName);
                cell++;
            }

            int rw = 1;
            for (Map<String, String> record : records) {
                XSSFRow xrow = sheet.createRow(rw);
                cell = 1;
                for (String columName : headFields) {
                    xrow.createCell(cell).setCellValue(record.get(columName));
                    cell++;
                }
                rw++;
            }

            workbook.write(os);
            os.close();
            flag = true;
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 将服务器上的文件下载到客户端（用户浏览器）
     * @param path 服务器上的文件路径（路径 + 文件名 + 后缀）
     * @param response
     */
    public void downloadFromService(String path, HttpServletResponse response) {
        try {
            File file = new File(path);
            String fileName = file.getName();
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @RequestMapping("/exportExcelTest")
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 创建表头
        List<String> headFields = new ArrayList<String>();
        headFields.add("序号");
        headFields.add("姓名");

        // 构建表格数据
        List<Map<String, String>> records = new ArrayList<Map<String, String>>();
        for (int i = 1; i <= 5; i++) {
            Map<String, String> record = new HashMap<String, String>();
            record.put("序号", i + "");
            record.put("姓名", "姓名" + i);
            records.add(record);
        }

        // 创建一个保存Excel的文件夹
        String folderPath = createFolderName(request, "exportDocs");
        String filePath = folderPath + FILE_SEPARATOR + "人员信息" + EXCEL_SUFFIX_XLSX;

        // 创建Excel文件，数据导入Excel，然后保存到指定的服务器路径
        boolean flag = creatWorkbook(headFields, records, "sheet1", filePath);
        if (flag) {
            // 将服务器上的文件下载到客户端（用户浏览器）
            downloadFromService(filePath, response);
        } else {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().print("下载文件失败");
        }
    }


    // 获取表头
    public String[] readExcelTitle(InputStream is) {
        POIFSFileSystem poifsFileSystem;
        HSSFWorkbook workbook = null;
        try {
            poifsFileSystem = new POIFSFileSystem(is);
            workbook = new HSSFWorkbook(poifsFileSystem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 获取第一个页签
        Sheet sheet = workbook.getSheetAt(0);
        Row row = sheet.getRow(0);
        // 标题总列数
        int colNum = row.getPhysicalNumberOfCells();
        String[] headFields = new String[colNum];
        for (int i = 0; i < colNum; i++) {
            headFields[i] = getCellFormatValue(row.getCell((short) i));
        }
        return headFields;
    }

    // 获取表格数据
    public Map<Integer, String> readExcelContent(InputStream is) throws IOException {
        String str = "";
        Map<Integer, String> content = new HashMap<Integer, String>();

        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(is);;
        Workbook workbook = new HSSFWorkbook(poifsFileSystem);

        Sheet sheet = workbook.getSheetAt(0);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        Row row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();

        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
            row = sheet.getRow(i);
            int j = 0;
            while (j < colNum) {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                str += getCellFormatValue(row.getCell((short) j)).trim() + "    ";
                j++;
            }
            content.put(i, str);
            str = "";
        }
        return content;
    }

    /**
     * 获取单元格数据内容为字符串类型的数据
     *
     * @param cell Excel单元格
     * @return String 单元格数据内容
     */
    private static String getStringCellValue(Cell cell) {
        String strCell = "";
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_STRING:
                strCell = cell.getStringCellValue();
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                strCell = String.valueOf(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:
                strCell = String.valueOf(cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                strCell = "";
                break;
            default:
                strCell = "";
                break;
        }
        if (strCell.equals("") || strCell == null) {
            return "";
        }
        if (cell == null) {
            return "";
        }
        return strCell;
    }
    // 根据HSSFCell类型设置数据
    private static String getCellFormatValue(Cell cell) {
        String cellvalue = "";
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式

                        //方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        //cellvalue = cell.getDateCellValue().toLocaleString();

                        //方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);

                    }
                    // 如果是纯数字
                    else {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;

    }


}
