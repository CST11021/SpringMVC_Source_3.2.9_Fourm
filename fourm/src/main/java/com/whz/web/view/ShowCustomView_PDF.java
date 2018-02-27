package com.whz.web.view;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.whz.domain.User;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

public class ShowCustomView_PDF extends AbstractPdfView {

	@Override
	protected void buildPdfDocument(Map<String, Object> model,Document document, PdfWriter writer, HttpServletRequest request,HttpServletResponse response) throws Exception {
		response.setHeader("Content-Disposition", "inline; filename=" + new String("用户列表".getBytes(), "iso8859-1"));
		List<User> userList = (List<User>) model.get("userList");
		Table table = new Table(3);
		table.setWidth(80);
		table.setBorder(1);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

		/************************使用中文字体，这里的字体需导入ITextAsian.jar包*****************************/
		BaseFont cnBaseFont = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", false);
	    Font cnFont = new Font(cnBaseFont, 10, Font.NORMAL, Color.BLUE);
		/************************使用中文字体，这里的字体需导入ITextAsian.jar包*****************************/


		table.addCell(buildFontCell("编号",cnFont));//对于中文字符使用中文字段构造Cell对象，否则发生乱码
		table.addCell(buildFontCell("姓名",cnFont));
		for (User user : userList) {
			table.addCell(user.getUserId()+"");//英文字符可直接添加到Cell中
			table.addCell(buildFontCell(user.getUserName(),cnFont));
		}
		document.add(table);
	}
	
	private Cell buildFontCell(String content,Font font) throws RuntimeException{ 
		try {
			 Phrase phrase = new Phrase(content, font);
			 return new Cell(phrase);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}	
}
