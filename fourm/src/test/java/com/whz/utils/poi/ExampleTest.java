package com.whz.utils.poi;

import com.whz.utils.date.DateUtil;
import com.whz.utils.qrCode.QRCodeUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * 显示一个复杂的例子
 */
public class ExampleTest {
    public static void main(String[] args) throws IOException {
        CrmOrderExcelVO vo = new CrmOrderExcelVO();
        new ExampleTest().mock(vo);
        HSSFWorkbook workbook = createExcel(vo);
        File file = new File("/Users/wanghongzhan/Documents/whz/temp/制造商型号-工单号-订单批量.xls");
        try {
            workbook.write(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个Excel文档
     * @param vo
     * @return
     * @throws IOException
     */
    private static HSSFWorkbook createExcel(CrmOrderExcelVO vo) throws IOException {

        String sheetName = "sheet1";
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(sheetName);

        // 设置打印样式
        setPrintSetup(sheet);

        // 设置第一行
        buildRow1(wb, sheet);

        // 设置第二行
        HSSFRow row2 = sheet.createRow(1);
        HSSFCell outOrderCodeLabel = row2.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));
        outOrderCodeLabel.setCellValue("委外订单号：" + vo.getOutRelationOrderCode());

        HSSFCell factoryCellLabel = row2.createCell((short) 6);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 6, 7));
        factoryCellLabel.setCellValue("工厂：" + vo.getFactoryName());

        // 设置第三行
        HSSFRow row3 = sheet.createRow(2);
        HSSFCell orderCodeLabel = row3.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 5));
        orderCodeLabel.setCellValue("工单编号：" + vo.getOrderCode());
        HSSFCell dateCellLabel = row3.createCell((short) 6);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 7));
        dateCellLabel.setCellValue(DateUtil.format(new Date(), "yyyy/MM/dd"));

        // 设置第四行
        HSSFRow row4 = sheet.createRow(3);
        HSSFCell requireCodeLabel = row4.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, 7));
        requireCodeLabel.setCellValue("生产需求编号：" + vo.getProductionRequiredOrderCode());

        // 设置第五行
        HSSFRow row5 = sheet.createRow(4);
        HSSFCell soCellLabel = row5.createCell((short) 0);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));
        soCellLabel.setCellValue("SO：" + StringUtils.trimToEmpty(vo.getSo()));

        // 设置生产资料

        // 设置第6、7行
        HSSFRow row6 = sheet.createRow(5);
        HSSFRow row7 = sheet.createRow(6);
        HSSFCell productNameLabel = row6.createCell(1);         // 产品名称
        productNameLabel.setCellValue("产品名称");
        row7.createCell(1).setCellValue(vo.getProductionName());

        HSSFCell materielCodeLabel = row6.createCell(2);        // 物料编码
        materielCodeLabel.setCellValue("物料编码");
        row7.createCell(2).setCellValue(vo.getMaterielCode());

        HSSFCell customerModelCodeLabel = row6.createCell(3);   // 客户模组编码
        customerModelCodeLabel.setCellValue("客户模组编码");
        row7.createCell(3).setCellValue(vo.getCustomGroupCode());

        HSSFCell productSizeLabel = row6.createCell(4);         // 产品型号
        productSizeLabel.setCellValue("产品型号");
        row7.createCell(4).setCellValue(vo.getProductSize());

        HSSFCell descLabel = row6.createCell(5);                // 信息描述
        descLabel.setCellValue("信息描述");
        row7.createCell(5).setCellValue(vo.getDesc());

        HSSFCell pcbVersionLabel = row6.createCell(6);          // PCB版本号
        pcbVersionLabel.setCellValue("PCB版本号");
        row7.createCell(6).setCellValue(vo.getPcbVersion());

        HSSFCell orderNumLabel = row6.createCell(7);            // 订单批量(pcs)
        orderNumLabel.setCellValue("订单批量");
        // sheet.setColumnWidth(7, (short) (37.5 * 130));
        row7.createCell(7).setCellValue(DateUtil.format(new Date(), "yyyy/MM/dd"));

        // 设置第8~12和14、15行：类别、生产BOM、SMT资料包、FLASH烧录软件、硬件测试工具、激活测试软件名称、check软件名称
        buildRow8(wb, sheet, 7, "类别", vo.getCategory());
        buildRow8(wb, sheet, 8, "生产BOM", vo.getBomName());
        buildRow8(wb, sheet, 9, "SMT资料包", vo.getSmtInfo());
        buildRow8(wb, sheet, 10, "FLASH烧录软件", vo.getFlashBurnSoftware());
        buildRow8(wb, sheet, 11, "硬件测试工具", vo.getTestTool());


        // 激活授权码
        HSSFRow row13 = sheet.createRow(12);
        HSSFCell tokenLabel = row13.createCell(1);            // 激活授权码
        tokenLabel.setCellValue("激活授权码");
        HSSFCell modelLabel = row13.createCell(2);
        sheet.addMergedRegion(new CellRangeAddress(12, 12, 2, 6));
        modelLabel.setCellValue(vo.getTokenId());

        // 生成二维码
        BufferedImage bufferedImage = QRCodeUtils.createQRCODEBufferedImage(vo.getTokenId(), 400, 400);
        fillPicture(row13.createCell(7), bufferedImage, HSSFWorkbook.PICTURE_TYPE_PNG, 100, 130);

        buildRow8(wb, sheet, 13, "激活测试软件名称", vo.getTestSoftware());
        buildRow8(wb, sheet, 14, "check软件名称", vo.getCheckSoftware());

        // 生产资料
        sheet.addMergedRegion(new CellRangeAddress(5, 14, 0, 0));
        HSSFCell productInfoLabel = row6.createCell(0);
        productInfoLabel.setCellValue("生产资料");

        // 设置16、17行：绿色生产要求：工艺要求、有害物质标准要求
        HSSFRow row16 = buildRow8(wb, sheet, 15, "工艺要求", vo.getTechnology());
        buildRow8(wb, sheet, 16, "有害物质标准要求", vo.getStandard());
        sheet.addMergedRegion(new CellRangeAddress(15, 16, 0, 0));

        HSSFCell greenRequiredLabel = row16.createCell(0);
        greenRequiredLabel.setCellValue("绿色生产要求");


        // 设置18行：生产性质
        HSSFRow row18 = sheet.createRow(17);
        row18.createCell(0).setCellValue("生产性质");
        sheet.addMergedRegion(new CellRangeAddress(17, 17, 1, 7));
        row18.createCell(1).setCellValue(vo.getProductionNature());

        // 设置19行：预计生产日期、预计交货日期
        HSSFRow row19 = sheet.createRow(18);
        row19.createCell(0).setCellValue("预计生产日期");
        sheet.addMergedRegion(new CellRangeAddress(18, 18, 1, 3));
        row19.createCell(1).setCellValue(DateUtil.format(new Date(), "yyyy/MM/dd"));

        row19.createCell(4).setCellValue("预计交货日期");
        sheet.addMergedRegion(new CellRangeAddress(18, 18, 5, 7));
        row19.createCell(5).setCellValue(DateUtil.format(new Date(), "yyyy/MM/dd"));

        // 设置20行：客户要求：客户订单号、物料编码、物料型号、model号、批次号
        HSSFRow row20 = sheet.createRow(19);
        HSSFRow row21 = sheet.createRow(20);
        HSSFCell customerOrderCodeLabel = row20.createCell(1);              // 客户订单号
        customerOrderCodeLabel.setCellValue("客户订单号");
        row21.createCell(1).setCellValue(vo.getCustomOderCode());

        HSSFCell customerMaterielCodeLabel = row20.createCell(2);           // 物料编码
        customerMaterielCodeLabel.setCellValue("物料编码");
        row21.createCell(2).setCellValue(vo.getPostMaterielCode());

        HSSFCell materielSizeLabel = row20.createCell(3);                   // 物料型号
        materielSizeLabel.setCellValue("物料型号");
        row21.createCell(3).setCellValue(vo.getMaterielType());

        HSSFCell modelCodeLabel = row20.createCell(4);                      // model号
        modelCodeLabel.setCellValue("model号");
        row21.createCell(4).setCellValue(vo.getModelNum());

        sheet.addMergedRegion(new CellRangeAddress(19, 19, 5, 7));
        HSSFCell numCodeLabel = row20.createCell(5);                        // 批次号
        numCodeLabel.setCellValue("批次号");
        sheet.addMergedRegion(new CellRangeAddress(20, 20, 5, 7));
        row21.createCell(5).setCellValue(vo.getBatchNum());

        // 合并客户要求
        sheet.addMergedRegion(new CellRangeAddress(19, 20, 0, 0));
        HSSFCell customerRequiredLabel = row20.createCell(0);
        customerRequiredLabel.setCellValue("客户要求");

        // 设置22、23行：标签、其他备注
        HSSFRow row22 = sheet.createRow(21);
        HSSFCell targetLabel = row22.createCell(0);                         // 标签
        targetLabel.setCellValue("标签");
        sheet.addMergedRegion(new CellRangeAddress(21, 21, 1, 7));
        String[] targetImageUrls = StringUtils.split(vo.getTarget(), ",");
        if (targetImageUrls != null && targetImageUrls.length > 0) {
            for (int index = 0; index < targetImageUrls.length; index++) {
                BufferedImage targetImage = ImageIO.read(new File(targetImageUrls[index]));
                HSSFCell targetCell = row22.createCell(index + 1);
                fillPicture(targetCell, targetImage, HSSFWorkbook.PICTURE_TYPE_PNG, 50, 50);
            }
        } else {
            row22.createCell(1).setCellValue("/");
        }


        HSSFRow row23 = sheet.createRow(22);
        HSSFCell remarkLabel = row23.createCell(0);                         // 其他备注
        remarkLabel.setCellValue("其他备注");
        sheet.addMergedRegion(new CellRangeAddress(22, 22, 1, 7));
        row23.createCell(1).setCellValue(vo.getRemark());

        // 设置24行：制表、审核
        HSSFRow row24 = sheet.createRow(23);
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 0, 3));
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 4, 5));
        row24.createCell(4).setCellValue("制表：");
        sheet.addMergedRegion(new CellRangeAddress(23, 23, 6, 7));
        row24.createCell(6).setCellValue("审核：");

        setDefaultStyle(wb, sheet);

        return wb;
    }

    private static void setPrintSetup(HSSFSheet sheet) {
        // 设置打印格式
        HSSFPrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(false); // 打印方向，true：横向，false：纵向
        ps.setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); //纸张
        sheet.setDisplayGridlines(true);
        sheet.setPrintGridlines(true);
        sheet.setMargin(HSSFSheet.BottomMargin,0.5 );   // 页边距（下）
        sheet.setMargin(HSSFSheet.LeftMargin,0.1 );     // 页边距（左）
        sheet.setMargin(HSSFSheet.RightMargin, 0.1 );   // 页边距（右）
        sheet.setMargin(HSSFSheet.TopMargin, 0.5 );     // 页边距（上）
        sheet.setHorizontallyCenter(false); //设置打印页面为水平居中
        sheet.setVerticallyCenter(false);   //设置打印页面为垂直居中
    }

    /**
     * 设置Excel第一行
     * @param wb
     * @param sheet
     */
    private static void buildRow1(HSSFWorkbook wb, HSSFSheet sheet) {
        HSSFRow row1 = sheet.createRow(0);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
        HSSFCell cell = row1.createCell((short) 0);
        cell.setCellValue("测试");

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

    /**
     * 设置第8行
     * @param wb
     * @param sheet
     * @param rowIndex
     * @param label
     * @param value
     * @return
     */
    private static HSSFRow buildRow8(HSSFWorkbook wb, HSSFSheet sheet, int rowIndex, String label, String value) {
        HSSFRow row8 = sheet.createRow(rowIndex);
        HSSFCell categoryLabel = row8.createCell(1);            // 类别
        categoryLabel.setCellValue(label);
        HSSFCell modelLabel = row8.createCell(2);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 2, 7));
        modelLabel.setCellValue(value);
        return row8;
    }

    /**
     * 填充图片
     *
     * @param cell        要填充的单元格
     * @param image       图片
     * @param pictureType 图片类型，例如：{@link HSSFWorkbook#PICTURE_TYPE_PNG}
     * @param height      单元格高度(图片自适应)
     * @param width       单元格宽度(图片自适应)
     */
    private static void fillPicture(HSSFCell cell, BufferedImage image, int pictureType, int height, int width) {
        HSSFSheet sheet = cell.getSheet();
        HSSFWorkbook wb = sheet.getWorkbook();
        // 声明一个画图的顶级管理器
        HSSFPatriarch patriarch = cell.getSheet().createDrawingPatriarch();

        // 设置高度，单位px
        cell.getRow().setHeightInPoints(height);
        // 设置宽度
        sheet.setColumnWidth(cell.getColumnIndex(), (short) (37.5 * width));

        HSSFClientAnchor anchor = new HSSFClientAnchor(10, 10, 1000, 250,
                (short) cell.getColumnIndex(), cell.getRowIndex(), (short) cell.getColumnIndex(), cell.getRowIndex());
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        patriarch.createPicture(anchor, wb.addPicture(out.toByteArray(), pictureType));
    }

    private static void setDefaultStyle(HSSFWorkbook wb, HSSFSheet sheet) {
        // 设置默认样式
        HSSFCellStyle defaultStyle = wb.createCellStyle();
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)9);//设置字号
        font.setFontName("华文仿宋");
        defaultStyle.setFont(font);
        defaultStyle.setWrapText(true); // 自动换行
        defaultStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 上下居中
        defaultStyle.setAlignment(HorizontalAlignment.LEFT); // 左对齐
        DataFormat format = wb.createDataFormat();
        defaultStyle.setDataFormat(format.getFormat("@")); // 设置成文本格式

        int lastRowNum = sheet.getLastRowNum();
        for (int index = 1; index <= lastRowNum; index++) {
            HSSFRow currentRow = sheet.getRow(index);
            if (currentRow == null) {
                continue;
            }
            int lastCellNum = currentRow.getLastCellNum();
            for (int colNum = 0; colNum < lastCellNum - 1; colNum++) {
                HSSFCell currentCell = currentRow.getCell(colNum);
                if (currentCell != null) {
                    currentCell.setCellStyle(defaultStyle);
                }
            }
        }
    }

    /**
     * mock的数据（用于测试）
     * @param vo
     */
    @Deprecated
    private static void mock(CrmOrderExcelVO vo) {
        vo.setOrderCode("工单编号");
        vo.setSaleOrderCode("销售订单号");
        vo.setMaterielCode("物料编码");
        vo.setCustomGroupCode("客户模组编号");
        vo.setProductSize("产品型号");
        vo.setDesc("描述");
        vo.setPcbVersion("PCB版本号");
        vo.setPcsNum(1000);


        vo.setManufacturerModel("制造商型号");
        vo.setOutRelationOrderCode("委外订单编号");
        vo.setFactoryName("工厂名称");
        vo.setCreateDate(new Date().getTime());
        vo.setProductionRequiredOrderCode("生产需求编号");
        vo.setSo("合同");
        vo.setProductionName("产品名称");
        vo.setCategory("类别");
        vo.setBomName("生产BOM");
        vo.setSmtInfo("生产资料包");
        vo.setFlashBurnSoftware("FLASH烧录软件");
        vo.setTokenId("激活码");
        vo.setTestTool("硬件测试工具");
        vo.setTestSoftware("测试软件名称");
        vo.setCheckSoftware("check软件名称");
        vo.setTechnology("工艺要求");

        vo.setStandard("有害物质标准");
        vo.setProductionNature("生产性质");
        vo.setPlaceOrderDate(new Date().getTime());
        vo.setExpectDeliveryDate(new Date().getTime());
        vo.setCustomOderCode("客户端订单号");
        vo.setMaterielType("物料型号");
        vo.setModelNum("model号");
        vo.setBatchNum("1");
        vo.setTarget("/Users/wanghongzhan/Documents/whz/temp/timg.jpeg,/Users/wanghongzhan/Documents/whz/temp/log.png,/Users/wanghongzhan/Documents/whz/temp/bug.png");
        vo.setRemark("备注");
    }



    /**
     * 用于封装生产工单的邮件内容
     */
    @Setter
    @Getter
    static class CrmOrderExcelVO {

        // 正文
        private String orderCode;                       // 工单编号
        private String saleOrderCode;                   // 销售订单号
        private String materielCode;                    // 物料编码
        private String customGroupCode;                 // 客户模组编号
        private String productSize;                     // 产品型号
        private String desc;                            // 描述
        private String pcbVersion;                      // PCB版本号
        private Integer pcsNum;                         // 订单批量（pcs)


        // excel
        private String manufacturerModel;               // 制造商型号
        private String outRelationOrderCode;            // 委外订单编号
        private String factoryName;                     // 工厂名称
        private Long createDate;                        // 日期
        private String productionRequiredOrderCode;     // 生产需求编号
        private String so;                              // 合同
        private String productionName;                  // 产品名称
        private String category;                        // 类别
        private String bomName;                         // 生产BOM
        private String smtInfo;                         // 生产资料包
        private String flashBurnSoftware;               // FLASH烧录软件
        private String tokenId;                         // 激活码
        private String testTool;                        // 硬件测试工具
        private String testSoftware;                    // 测试软件名称
        private String checkSoftware;                   // check软件名称
        private String technology;                      // 工艺要求
        private String standard;                        // 有害物质标准
        private String productionNature;                // 生产性质
        private Long placeOrderDate;					// 预计生产日期
        private Long expectDeliveryDate;				// 预计交货日期
        private String customOderCode;                  // 客户端订单号
        private String postMaterielCode;                // 客户物料编码
        private String materielType;                    // 物料型号
        private String modelNum;                        // model号
        private String batchNum;                        // 批次号
        private String target;                          // 标签
        private String remark;                          // 备注

    }
}
