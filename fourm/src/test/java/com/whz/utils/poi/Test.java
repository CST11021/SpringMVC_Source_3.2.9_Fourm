package com.whz.utils.poi;

import com.whz.utils.qrCode.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        HSSFWorkbook workbook = createExcel();
        File file = new File("/Users/wanghongzhan/Documents/whz/test/制造商型号-工单号-订单批量.xls");
        try {
            workbook.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HSSFWorkbook createExcel() throws IOException {

        String sheetName = "sheet1";
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName);

        sheet.setDefaultColumnWidth((short) (20));

        // 设置第一行
        buildRow1(wb, sheet);

        // 设置第二行
        HSSFRow row2 = sheet.createRow(1);
        HSSFCell outOrderCodeLabel = row2.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,5));
        outOrderCodeLabel.setCellValue("委外订单号：" + "12312312");

        HSSFCell factoryCellLabel = row2.createCell((short) 6);
        sheet.addMergedRegion(new CellRangeAddress(1,1,6,7));
        factoryCellLabel.setCellValue("工厂：" + "BUG工厂");

        // 设置第三行
        HSSFRow row3 = sheet.createRow(2);
        HSSFCell orderCodeLabel = row3.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(2,2,0,5));
        orderCodeLabel.setCellValue("工单编号：" + "1231212312312");
        HSSFCell dateCellLabel = row3.createCell((short) 6);
        sheet.addMergedRegion(new CellRangeAddress(2,2,6,7));
        dateCellLabel.setCellValue("日期：" + "2018/05/22");

        // 设置第四行
        HSSFRow row4 = sheet.createRow(3);
        HSSFCell requireCodeLabel = row4.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(3,3,0,7));
        requireCodeLabel.setCellValue("生产需求编号：" + "PR20180522000144");

        // 设置第五行
        HSSFRow row5 = sheet.createRow(4);
        HSSFCell soCellLabel = row5.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(4,4,0,7));
        soCellLabel.setCellValue("SO：" + "花花排合同");

        // 设置生产资料

        // 设置第6、7行
        HSSFRow row6 = sheet.createRow(5);
        HSSFRow row7 = sheet.createRow(6);
        HSSFCell productNameLabel = row6.createCell(1);         // 产品名称
        productNameLabel.setCellValue("产品名称");
        row7.createCell(1).setCellValue("智能灯泡");

        HSSFCell materielCodeLabel = row6.createCell(2);        // 物料编码
        materielCodeLabel.setCellValue("物料编码");
        row7.createCell(2).setCellValue("灯泡wuliao");

        HSSFCell customerModelCodeLabel = row6.createCell(3);   // 客户模组编码
        customerModelCodeLabel.setCellValue("客户模组编码");
        row7.createCell(3).setCellValue("SDFAFA3231");

        HSSFCell productSizeLabel = row6.createCell(4);         // 产品型号
        productSizeLabel.setCellValue("产品型号");
        row7.createCell(4).setCellValue("Z-X34");

        HSSFCell descLabel = row6.createCell(5);                // 信息描述
        descLabel.setCellValue("信息描述");
        row7.createCell(5).setCellValue("多功能，智能灯泡");

        HSSFCell pcbVersionLabel = row6.createCell(6);          // PCB版本号
        pcbVersionLabel.setCellValue("PCB版本号");
        row7.createCell(6).setCellValue("v1.0");

        HSSFCell orderNumLabel = row6.createCell(7);            // 订单批量(pcs)
        orderNumLabel.setCellValue("订单批量(pcs)");
        row7.createCell(7).setCellValue("1000");

        // 设置第8~12和14、15行：类别、生产BOM、SMT资料包、FLASH烧录软件、硬件测试工具、激活测试软件名称、check软件名称
        buildRow8(wb, sheet, 7, "类别");
        buildRow8(wb, sheet, 8, "生产BOM");
        buildRow8(wb, sheet, 9, "SMT资料包");
        buildRow8(wb, sheet, 10, "FLASH烧录软件");
        buildRow8(wb, sheet, 11, "硬件测试工具");
        buildRow8(wb, sheet, 13, "激活测试软件名称");
        buildRow8(wb, sheet, 14, "check软件名称");

        // 激活授权码
        HSSFRow row13 = sheet.createRow(12);
        HSSFCell tokenLabel = row13.createCell(1);            // 激活授权码
        tokenLabel.setCellValue("激活授权码");
        HSSFCell modelLabel = row13.createCell(2);
        sheet.addMergedRegion(new CellRangeAddress(12,12,2,6));
        modelLabel.setCellValue("......");

        // 生成二维码
        byte[] fileBytes = QRCodeUtils.createQrCodeByte("二维码测试", 400, 400);
        fillPicture(row13.createCell(7), fileBytes, HSSFWorkbook.PICTURE_TYPE_PNG, 100, 120);

        // 生产资料
        sheet.addMergedRegion(new CellRangeAddress(5,14,0,0));
        HSSFCell productInfoLabel = row6.createCell(0);
        productInfoLabel.setCellValue("生产资料");
        // 居中
        HSSFCellStyle productInfoLabelStyle = wb.createCellStyle();
        productInfoLabelStyle.setAlignment(HorizontalAlignment.CENTER);
        productInfoLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        productInfoLabel.setCellStyle(productInfoLabelStyle);

        // 设置16、17行：绿色生产要求：工艺要求、有害物质标准要求
        HSSFRow row16 = buildRow8(wb, sheet, 15, "工艺要求");
        buildRow8(wb, sheet, 16, "有害物质标准要求");
        sheet.addMergedRegion(new CellRangeAddress(15,16,0,0));

        HSSFCell greenRequiredLabel = row16.createCell(0);
        greenRequiredLabel.setCellValue("绿色生产要求");
        greenRequiredLabel.setCellStyle(productInfoLabelStyle);


        // 设置18行：生产性质
        HSSFRow row18 = sheet.createRow(17);
        row18.createCell(0).setCellValue("生产性质");
        sheet.addMergedRegion(new CellRangeAddress(17,17,1,7));

        // 设置19行：预计生产日期、预计交货日期
        HSSFRow row19 = sheet.createRow(18);
        row19.createCell(0).setCellValue("预计生产日期");
        sheet.addMergedRegion(new CellRangeAddress(18,18,1,3));
        row19.createCell(1).setCellValue("2018-05-23 00:00:00");

        row19.createCell(4).setCellValue("预计交货日期");
        sheet.addMergedRegion(new CellRangeAddress(18,18,5,7));
        row19.createCell(5).setCellValue("2018-05-24 00:00:00");

        // 设置20行：客户要求：客户订单号、物料编码、物料型号、model号、批次号
        HSSFRow row20 = sheet.createRow(19);
        HSSFRow row21 = sheet.createRow(20);
        HSSFCell customerOrderCodeLabel = row20.createCell(1);              // 客户订单号
        customerOrderCodeLabel.setCellValue("客户订单号");
        row21.createCell(1).setCellValue("2313123123");

        HSSFCell customerMaterielCodeLabel = row20.createCell(2);           // 物料编码
        customerMaterielCodeLabel.setCellValue("物料编码");
        row21.createCell(2).setCellValue("灯泡wuliao");

        HSSFCell materielSizeLabel = row20.createCell(3);                   // 物料型号
        materielSizeLabel.setCellValue("物料型号");
        row21.createCell(3).setCellValue("SDFAFA3231");

        HSSFCell modelCodeLabel = row20.createCell(4);                      // model号
        modelCodeLabel.setCellValue("model号");
        row21.createCell(4).setCellValue("Z-X34");

        sheet.addMergedRegion(new CellRangeAddress(19,19,5,7));
        HSSFCell numCodeLabel = row20.createCell(5);                        // 批次号
        numCodeLabel.setCellValue("批次号");
        sheet.addMergedRegion(new CellRangeAddress(20,20,5,7));
        row21.createCell(5).setCellValue("1000");

        // 合并客户要求
        sheet.addMergedRegion(new CellRangeAddress(19,20,0,0));
        HSSFCell customerRequiredLabel = row20.createCell(0);
        customerRequiredLabel.setCellValue("客户要求");
        customerRequiredLabel.setCellStyle(productInfoLabelStyle);

        // 设置22、23行：标签、其他备注
        HSSFRow row22 = sheet.createRow(21);
        HSSFCell targetLabel = row22.createCell(0);                         // 标签
        targetLabel.setCellValue("标签");
        sheet.addMergedRegion(new CellRangeAddress(21,21,1,7));
        row22.createCell(1).setCellValue(".......");

        HSSFRow row23 = sheet.createRow(22);
        HSSFCell remarkLabel = row23.createCell(0);                         // 其他备注
        remarkLabel.setCellValue("其他备注");
        sheet.addMergedRegion(new CellRangeAddress(22,22,1,7));
        row23.createCell(1).setCellValue(".......");

        // 设置24行：制表、审核
        HSSFRow row24 = sheet.createRow(23);
        sheet.addMergedRegion(new CellRangeAddress(23,23,0,3));
        sheet.addMergedRegion(new CellRangeAddress(23,23,4,5));
        row24.createCell(4).setCellValue("制表：");
        sheet.addMergedRegion(new CellRangeAddress(23,23,6,7));
        row24.createCell(6).setCellValue("审核：");

        // 设置默认样式
        HSSFCellStyle defaultStyle = wb.createCellStyle();
        HSSFFont defaultFont = wb.createFont();
        defaultFont.setFontName("宋体");
        defaultFont.setFontHeightInPoints((short) 14);
        defaultStyle.setFont(defaultFont);

        int lastRowNum = sheet.getLastRowNum();
        for (int index = 1; index <= lastRowNum; index++) {
            HSSFRow currentRow = sheet.getRow(index);
            int lastCellNum = currentRow.getLastCellNum();
            for (int colNum = 0; colNum < lastCellNum; colNum++) {
                HSSFCell currentCell = currentRow.getCell(colNum);
                if (currentCell != null) {
                    currentCell.setCellStyle(defaultStyle);
                }
            }
        }

        return wb;
    }

    private static void buildRow1(HSSFWorkbook wb, HSSFSheet sheet) {
        HSSFRow row1 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,7));
        HSSFCell cell = row1.createCell((short) 0);
        cell.setCellValue("杭州**信息技术有限公司");

        HSSFCellStyle style = wb.createCellStyle();
        // 居中
        style.setAlignment(HorizontalAlignment.CENTER);

        // 字体：宋体、24
        HSSFFont font = wb.createFont();
        font.setFontName("宋体");
        //设置字体大小
        font.setFontHeightInPoints((short) 24);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    private static HSSFRow buildRow8(HSSFWorkbook wb, HSSFSheet sheet, int rowIndex, String label) {
        HSSFRow row8 = sheet.createRow(rowIndex);
        HSSFCell categoryLabel = row8.createCell(1);            // 类别
        categoryLabel.setCellValue(label);
        HSSFCell modelLabel = row8.createCell(2);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex,rowIndex,2,7));
        modelLabel.setCellValue("......");
        return row8;
    }

    /**
     * 填充图片
     * @param cell          要填充的单元格
     * @param fileBytes     要填充的图片字节数组
     * @param pictureType   图片类型，例如：{@link HSSFWorkbook#PICTURE_TYPE_PNG}
     * @param height        单元格高度(图片自适应)
     * @param width         单元格宽度(图片自适应)
     */
    private static void fillPicture(HSSFCell cell, byte[] fileBytes, int pictureType, int height, int width) {
        HSSFSheet sheet = cell.getSheet();
        HSSFWorkbook wb = sheet.getWorkbook();
        // 声明一个画图的顶级管理器
        HSSFPatriarch patriarch = cell.getSheet().createDrawingPatriarch();
        // 设置高度，单位px
        cell.getRow().setHeightInPoints(height);
        // 设置宽度
        sheet.setColumnWidth(cell.getColumnIndex(), (short) (37.5*width));

        // dx1：起始单元格的左侧偏移量；
        // dy1：起始单元格的上侧偏移量；
        // dx2：终止单元格的左侧偏移量；
        // dy2：终止单元格的上侧偏移量；
        // col1：起始单元格列序号，从0开始计算；
        // row1：起始单元格行序号，从0开始计算，如例子中col1=0,row1=0就表示起始单元格为A1；
        // col2：终止单元格列序号，从0开始计算；
        // row2：终止单元格行序号，从0开始计算，如例子中col2=2,row2=2就表示起始单元格为C3；
        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 1023, 255,
                (short) cell.getColumnIndex(), cell.getRowIndex(), (short) cell.getColumnIndex(), cell.getRowIndex());
        anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        patriarch.createPicture(anchor, wb.addPicture(fileBytes, pictureType));
    }

}
