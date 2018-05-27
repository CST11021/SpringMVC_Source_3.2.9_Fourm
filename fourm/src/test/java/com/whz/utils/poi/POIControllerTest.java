package com.whz.utils.poi;

import com.whz.utils.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;



//<div class="export">
//    <form id="down_form" action="-" method="POST">
//        <a href="javascript:void(0);" onclick="exportData();" class="a-btn3">导出</a>
//    </form>
//</div>
//
//<script>
//    function exportData(){
//        var frame = document.getElementById("down_form");
//        var url = 'person.dragon.exportData(${key})';
//        frame.action = url;
//        frame.submit();
//    }
//</script>

// HSSFWorkbook：是操作Excel2003以前（包括2003）的版本，扩展名是.xls
// XSSFWorkbook：是操作Excel2007的版本，扩展名是.xlsx
// 对于不同版本的EXCEL文档要使用不同的工具类，如果使用错了，会提示如下错误信息：
// org.apache.poi.openxml4j.exceptions.InvalidOperationExceptionorg.apache.poi.poifs.filesystem.OfficeXmlFileException
@Controller
public class POIControllerTest {

    public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");

    @RequestMapping("/export")
    public void export(HttpServletRequest request, HttpServletResponse response) throws IOException {

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



        // 创建一个文件夹，用来保存Excel文件
        String folderPath = request.getSession().getServletContext().getRealPath("exportdocs");
        File file = new File(folderPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        // 使用当前时间作为Excel文件名
        String currentTime = DateUtil.format(new Date(), "yyyyMMddHHmmssSS");
        String fileName = currentTime + ".xlsx";
        String filePath = folderPath + FILE_SEPARATOR + fileName;


        // 创建Excel文件，数据导入Excel，然后保存到指定的服务器路径
        boolean flag = creatWorkbook(records, headFields, "人员信息", filePath);

        if (flag) {
            // 将服务器上的文件下载到客户端（用户浏览器）
            download(filePath, response);
        } else {
            response.setContentType("text/html;charset=GBK");
            response.getWriter().print("下载文件失败");
            return;
        }
    }

    /**
     * 创建Excel文件，数据导入Excel，然后保存到指定的服务器路径
     * @param records       要导出的数据
     * @param headFields    表头信息
     * @param title         表示Excel页签
     * @param filepath      表示文件路径（路径 + 文件名 + 文件后缀）
     * @return Excel文件是否创建成功
     */
    public boolean creatWorkbook(List<Map<String, String>> records, List<String> headFields, String title, String filepath) {
        boolean flag;

        try {
            OutputStream os = new FileOutputStream(filepath);
            // 创建一个工作簿，然后根据 title 创建一个页签
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(title);
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
     * @param path 服务器上的文件路径
     * @param response
     */
    public void download(String path, HttpServletResponse response) {
        try {
            File file = new File(path);
            String filename = file.getName();
            InputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            response.reset();
            response.addHeader("Content-Disposition", "attachment;filename=" + new String(filename.getBytes()));
            response.addHeader("Content-Length", "" + file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/vnd.ms-excel;charset=gb2312");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
