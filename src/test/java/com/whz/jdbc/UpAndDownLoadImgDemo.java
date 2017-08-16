package com.whz.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class UpAndDownLoadImgDemo {
    public static final String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // example: <img src="showPhoto-1.html" width="80" height="100"/>
	@RequestMapping(value = "/showPhoto-{id}.html")
    public void showPhoto(@PathVariable String id,HttpServletRequest request,HttpServletResponse response) throws Exception {
//        upLoadPhoto("D:\\"+id+".jpg");

        String folderPath = request.getSession().getServletContext().getRealPath("personPhotoStore");
        File folder = new File(folderPath);
        File img = new File(folderPath + FILE_SEPARATOR + id + ".jpg");

        if(!folder.exists()){
            folder.mkdirs();
        }

        InputStream photo;
        if(img.exists()) {
            photo = new FileInputStream(img);
        }else{
            photo = getPhotoFromDB(id);//从数据库中获取照片流
            download(new FileOutputStream(img),photo);//将照片下载到服务器
            photo.reset();
        }
        download(response.getOutputStream(),photo);//将照片流返回给客户端
	}

    /**
     * 照片流下载
     * @param out 输出流
     * @param in 输入流
     * @throws IOException
     */
    private void download(OutputStream out,InputStream in) throws IOException {
        int temp = 0;
        byte[] buffer = new byte[4096];
        try {
            while ((temp = in.read(buffer)) != -1) {
                out.write(buffer, 0, temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * 上传照片到数据库
     * @param photoPath 照片路径
     * @throws FileNotFoundException
     */
    private void upLoadPhoto(String photoPath) throws FileNotFoundException {
        final File file = new File(photoPath);
        final InputStream in = new FileInputStream(file);
        jdbcTemplate.execute("INSERT INTO country (name,flag,description) VALUES (?, ?, ?)",
                new PreparedStatementCallback() {
                    @Override
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
                        ps.setString(1, "张三");
                        ps.setBinaryStream(2, in, (int) file.length());
                        ps.setString(3, "一个中国人");
                        ps.execute();
                        return null;
                    }
                });

    }

    /**
     * 从数据库中获取照片流
     * @param id 照片所在记录的ID
     * @return InputStream
     * @throws SQLException
     */
    private InputStream getPhotoFromDB(final String id) throws SQLException {
        InputStream isImg = (InputStream) jdbcTemplate.execute("select flag from Country where id = ?",
                new PreparedStatementCallback() {
                    @Override
                    public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                        ps.setInt(1, Integer.valueOf(id));
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            Blob blob = rs.getBlob("flag");
                            return blob.getBinaryStream();
                        }
                        return null;
                    }
                });
        return isImg;
    }

    /**
     * 从指定的URL下载文件
     * @param urlStr url
     * @param fileName 下载到本地的文件名
     * @param savePath 下载到本地的路径
     * @throws IOException
     */
    private void downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setConnectTimeout(3*1000);//设置超时间为3秒
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");//防止屏蔽程序抓取而返回403错误

        File saveDir = new File(savePath);
        if(!saveDir.exists()) saveDir.mkdir();

        File file = new File(saveDir+File.separator+fileName);
        FileOutputStream out = new FileOutputStream(file);
        InputStream in = conn.getInputStream();
        download(out,in);
        System.out.println("info:"+url+" download success");
    }


}