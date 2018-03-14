package com.whz.web.controller;

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
import java.util.Iterator;

@Controller
public class UploadController {

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

}