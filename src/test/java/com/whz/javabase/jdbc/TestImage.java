package com.whz.javabase.jdbc;

import org.junit.Test;

import java.sql.*;
import java.io.*;

/* 数据库脚本
create database Image;
use Image;
create table country (
id int primary key auto_increment,
name varchar(30),
flag blob,
description varchar(255)
);*/
public class TestImage {
    private static final String URL = "jdbc:mysql://localhost/Image?user=root&password=123456&useUnicode=true";
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private File file = null;
	private InputStream inputImage = null;
	private OutputStream outputImage = null;
	
	/**
	 * 将图片存入Mysql
	 * @param infile 图片路径
	 */
	public void blobInsert(String infile) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection(URL);
		    pstmt = conn.prepareStatement("insert into Country (name,flag,description) values (?,?,?)");
		    pstmt.setString(1, "china");
		    file = new File(infile);
		    try {
		    	inputImage = new FileInputStream(file);
		    } catch (FileNotFoundException e) {
		    	e.printStackTrace();
		    }
		    pstmt.setBinaryStream(2, inputImage, (int) (file.length()));
		    pstmt.setString(3, "A flag of China");
		    pstmt.executeUpdate();
	   } catch (ClassNotFoundException e) {
		   e.printStackTrace();
	   } catch (SQLException e) {
		   e.printStackTrace();
	   } finally {
		   try {
			   inputImage.close();
			   pstmt.close();
			   conn.close();
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
	   }
	}
	
	/**
	 * 下载图片
	 * @param path 下载到本地的路径
	 * @param id 图片所在记录的ID
	 */
	public void readBolb(String path, int id) {
	   InputStream is = null;
	   byte[] buffer = new byte[4096];
	   try {
		   Class.forName("com.mysql.jdbc.Driver");
		   conn = DriverManager.getConnection(URL);
		   pstmt = conn.prepareStatement("select flag from Country where id =?");
	    
		   pstmt.setInt(1, id);
		   rs = pstmt.executeQuery();
		   rs.next();
		   file = new File(path);
		   if (!file.exists()) {
			   try {
				   file.createNewFile();
			   } catch (IOException e) {
				   e.printStackTrace();
			   }
		   }
		   try {
			   outputImage = new FileOutputStream(file);
		   } catch (FileNotFoundException e) {
			   e.printStackTrace();
		   }
		   Blob blob = rs.getBlob("flag");
		   is = blob.getBinaryStream();
		   try {
			   System.out.println(is.available()+"Byte");
		   } catch (IOException e2) {
			   e2.printStackTrace();
		   }
		   int size = 0;
		   try {
			   while ((size = is.read(buffer)) != -1) {
				   outputImage.write(buffer, 0, size);
			   }
		   } catch (IOException e) {
			   e.printStackTrace();
		   }
	   	} catch (ClassNotFoundException e) {
	   		e.printStackTrace();
	   	} catch (SQLException e) {
	   		e.printStackTrace();
	   	} finally {
	   		try {
	   			is.close();
	   			outputImage.close();
	   			pstmt.close();
	   			conn.close();
	   		} catch (Exception e) {
	   			e.printStackTrace();
	   		}
	   }
	}
	

	@Test
	public void testBlobInsert() {
		new TestImage().blobInsert("d:\\test.jpg");
	}

	@Test
	public void testReadBolb() {
		new TestImage().readBolb("d:\\test-load.png", 1);
	}

}