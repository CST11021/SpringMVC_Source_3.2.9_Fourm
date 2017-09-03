package com.whz.utils;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
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

    @RequestMapping("/test")
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

}
