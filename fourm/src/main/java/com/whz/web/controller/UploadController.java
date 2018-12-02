package com.whz.web.controller;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Controller
public class UploadController {

    public static final String EXCEL_SUFFIX_XLSX = ".xlsx";
    public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");

    @Autowired
    private MultipartResolver multipartResolver;

    @RequestMapping("/testFileUpAndDownLoad")
    public String testFileUpAndDownLoad(HttpServletRequest request, HttpServletResponse response) {
        return "forward:/WEB-INF/views/jsp/testFileUpAndDownLoad.jsp";
    }

    /**
     * 使用springMVC的包装类上传文件
     * @param request
     * @param response
     * @return
     * @throws IllegalStateException
     * @throws java.io.IOException
     */
    @RequestMapping("/upload1")
    public String upload1(HttpServletRequest request, HttpServletResponse response) throws IllegalStateException, IOException {

//        ServletContext servletContext = request.getSession().getServletContext();
//        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(servletContext);// 如果不使用Spring中的 multipartResolver 可以再次使用servletContext再创建

        // 判断是否为文件上传request
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

            // 取得request中的所有上传组件的组件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                long pre = System.currentTimeMillis();
                // 获取表单中上传组件的组件名
                String paramName = iter.next();
                // 根据组件名取得上传的文件对象
                MultipartFile file = multiRequest.getFile(paramName);
                if (file != null) {
                    // 取得当前上传文件的文件名称
                    String myFileName = file.getOriginalFilename();
                    if (myFileName.trim() != "") {
                        // 重命名上传后的文件名
                        String fileName = "demoUpload" + file.getOriginalFilename();
                        // 定义上传路径
                        String folderPath = "D:/tempDir/";
                        File f = new File(folderPath);
                        if (!f.exists()) {
                            f.mkdirs();
                        }
                        String path = folderPath + fileName;
                        File localFile = new File(path);
                        file.transferTo(localFile);
                    }
                }
                long finaltime = System.currentTimeMillis();
                System.out.println("上传文件耗时："+(finaltime - pre)+"秒");
            }
        }
        return null;
    }

    /**
     * 从服务的指定目录下载文件
     * @param fileName
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/downLoad1-({fileName})")
    public ModelAndView download1(@PathVariable String fileName,HttpServletRequest request, HttpServletResponse response) throws Exception {
        String ctxPath = "D:/tempDir/";
        String downLoadPath = ctxPath + fileName;

        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("UTF-8");

        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            long fileLength = new File(downLoadPath).length();
            response.setContentType("application/x-msdownload;");
            response.setHeader("Content-disposition", "attachment; filename="+ new String(fileName.getBytes("gb2312"), "ISO8859-1"));
            response.setHeader("Content-Length", String.valueOf(fileLength));
            bis = new BufferedInputStream(new FileInputStream(downLoadPath));
            bos = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
        }
        return null;
    }


    @RequestMapping("/exportExcelTest")
    public void exportExcelTest(HttpServletRequest request, HttpServletResponse response) throws IOException {

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

}