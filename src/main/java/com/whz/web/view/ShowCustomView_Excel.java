package com.whz.web.view;

import com.whz.domain.User;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractExcelView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

// 继承于AbstractExcelView,使用POI技术创建Excel文档
public class ShowCustomView_Excel extends AbstractExcelView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, HSSFWorkbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Excel文档名称编码必须为iso8859-1，否则显示乱码
		response.setHeader("Content-Disposition", "inline; filename=" + new String("用户列表.xls".getBytes(), "iso8859-1"));
		List<User> userList = (List<User>) model.get("userList");
		HSSFSheet sheet = workbook.createSheet("users");
		HSSFRow header = sheet.createRow(0);
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("姓名");

		int rowNum = 1;
		for (User user : userList) {
			HSSFRow row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(user.getUserId());
			row.createCell(1).setCellValue(user.getUserName());
		}
	}
}