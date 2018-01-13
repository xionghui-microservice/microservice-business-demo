package com.github.xionghui.microservice.business.utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.github.xionghui.microservice.business.bean.domain.ProjectDomain;
import com.github.xionghui.microservice.business.bean.enums.BusinessHttpResultEnum;
import com.github.xionghui.microservice.business.bean.enums.ProjectTypeEnum;
import com.github.xionghuicoder.microservice.common.BusinessException;
import com.github.xionghuicoder.microservice.common.bean.CommonConstants;
import com.github.xionghuicoder.microservice.common.utils.CommonResourceBundleMessageSourceUtils;

public class ExcelUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

  private static final String LIST = "m10000";

  private static final String[] PROJECT_HEADERS =
      {"m10001", "m10002", "m10003", "m10004", "m10005", "m10006"};

  public static void buildFinanceXls(HttpServletResponse response) {
    Workbook workbook = new HSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet(CommonResourceBundleMessageSourceUtils.getMessage(LIST));
      // 自适应列宽度,暂时没兼容汉字
      sheet.autoSizeColumn(1);
      sheet.autoSizeColumn(1, true);
      writeHeader(workbook, sheet);
      writeSelectContent(sheet, 1, parseProjectTypeEnum());

      workbook.write(response.getOutputStream());
    } catch (Exception e) {
      throw new BusinessException(e);
    } finally {
      try {
        workbook.close();
      } catch (IOException e) {
        // ignore
      }
    }
  }

  private static void writeHeader(Workbook workbook, Sheet sheet) {
    // 生成样式
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(HSSFColorPredefined.LIGHT_GREEN.getIndex());
    style.setFillBackgroundColor(HSSFColorPredefined.LIGHT_GREEN.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    style.setBorderTop(BorderStyle.THIN);
    style.setBorderBottom(BorderStyle.THIN);
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    // 生成字体
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 12);
    font.setBold(true);
    // 把字体应用到当前的样式
    style.setFont(font);
    // 产生表格标题行
    Row row = sheet.createRow(0);
    for (short i = 0; i < PROJECT_HEADERS.length; i++) {
      Cell cell = row.createCell(i);
      cell.setCellStyle(style);
      HSSFRichTextString text = new HSSFRichTextString(
          CommonResourceBundleMessageSourceUtils.getMessage(PROJECT_HEADERS[i]));
      cell.setCellValue(text);
    }
  }

  private static String[] parseProjectTypeEnum() {
    List<String> valueList = new ArrayList<>();
    for (ProjectTypeEnum theEnum : ProjectTypeEnum.values()) {
      valueList.add(CommonResourceBundleMessageSourceUtils.getMessage(theEnum.getLanguageCode()));
    }
    return valueList.toArray(new String[0]);
  }

  private static void writeSelectContent(Sheet sheet, int col, String[] contentList) {
    CellRangeAddressList regions = new CellRangeAddressList(1, Integer.MAX_VALUE, col, col);
    // 生成下拉框内容
    DVConstraint constraint = DVConstraint.createExplicitListConstraint(contentList);
    // 绑定下拉框和作用区域
    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
    // 对sheet页生效
    sheet.addValidationData(dataValidation);
  }

  public static List<ProjectDomain> parseProjectExcel(boolean isXls, MultipartFile file) {
    List<ProjectDomain> domainList = new ArrayList<>();
    Workbook workbook = null;
    try {
      if (isXls) {
        workbook = new HSSFWorkbook(new POIFSFileSystem(file.getInputStream()));
      } else {
        workbook = new XSSFWorkbook(file.getInputStream());
      }

      Map<String, String> projectTypeMap = new HashMap<>();
      for (ProjectTypeEnum theEnum : ProjectTypeEnum.values()) {
        projectTypeMap.put(
            CommonResourceBundleMessageSourceUtils.getMessage(theEnum.getLanguageCode()),
            theEnum.code);
      }

      // 读第一个sheet
      Sheet sheet = workbook.getSheetAt(0);
      validateFinanceHeader(sheet);
      for (int i = 1, rowNum = sheet.getLastRowNum(); i <= rowNum; i++) {
        ProjectDomain domain = new ProjectDomain();
        domainList.add(domain);

        int j = 0;
        Row row = sheet.getRow(i);
        String name = getCellStringContent(row, j++);
        domain.setName(name);
        String type = getCellStringContent(row, j++);
        domain.setType(projectTypeMap.get(type));
        String employeeName = getCellStringContent(row, j++);
        domain.setEmployeeName(employeeName);
        Double money = (Double) getCellContent(row, j++);
        domain.setMoney(money == null ? null : money.intValue());
        Date timeDate = (Date) getCellContent(row, j++);
        domain.setTime(timeDate == null ? null : new Timestamp(timeDate.getTime()));
        String note = String.valueOf(getCellContent(row, j++));
        domain.setNote(note);
      }
    } catch (BusinessException e) {
      LOGGER.error("cause business exception: ", e);
      throw e;
    } catch (Exception e) {
      LOGGER.error("parse excel data failed: ", e);
      throw new BusinessException("excel data illegal", BusinessHttpResultEnum.ExcelDataError);
    } finally {
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
    return domainList;
  }

  private static String getCellStringContent(Row row, int k) {
    Cell cell = row.getCell(k);
    if (cell == null) {
      return null;
    }
    Object obj = null;
    switch (cell.getCellTypeEnum()) {
      case NUMERIC: // 数字
        DataFormatter dataFormatter = new HSSFDataFormatter();
        obj = dataFormatter.formatCellValue(cell);
        if (DateUtil.isCellDateFormatted(cell)) { // Excel Date类型处理
          Date date = DateUtil.getJavaDate(cell.getNumericCellValue());
          obj = CommonConstants.DATE_FORMAT.get().format(date);
        } else {
          obj = cell.getNumericCellValue();
        }
        break;
      case STRING: // 字符串
        obj = cell.getStringCellValue();
        break;
      case BOOLEAN: // Boolean
        obj = cell.getBooleanCellValue();
        break;
      case FORMULA: // 公式
        obj = cell.getCellFormula();
        break;
      case BLANK: // 空值
        obj = "";
        break;
      case ERROR: // 故障
        throw new BusinessException("error type: " + CellType.ERROR);
      default:
        throw new BusinessException("unkone type: " + cell.getCellTypeEnum());
    }
    return obj == null ? null : obj.toString();
  }

  private static Object getCellContent(Row row, int k) {
    Cell cell = row.getCell(k);
    if (cell == null) {
      return null;
    }
    Object obj = null;
    switch (cell.getCellTypeEnum()) {
      case NUMERIC: // 数字
        if (DateUtil.isCellDateFormatted(cell)) { // Excel Date类型处理
          obj = DateUtil.getJavaDate(cell.getNumericCellValue());
        } else {
          obj = cell.getNumericCellValue();
        }
        break;
      case STRING: // 字符串
        obj = cell.getStringCellValue();
        break;
      case BOOLEAN: // Boolean
        obj = cell.getBooleanCellValue();
        break;
      case FORMULA: // 公式
        obj = cell.getCellFormula();
        break;
      case BLANK: // 空值
        obj = "";
        break;
      case ERROR: // 故障
        throw new BusinessException("error type: " + CellType.ERROR);
      default:
        throw new BusinessException("unkone type: " + cell.getCellTypeEnum());
    }
    return obj;
  }

  private static void validateFinanceHeader(Sheet sheet) {
    Row row = sheet.getRow(0);
    for (int i = 0; i < PROJECT_HEADERS.length; i++) {
      Cell cell = row.getCell(i);
      if (cell.getStringCellValue() == null || !(cell.getStringCellValue().trim())
          .equals(CommonResourceBundleMessageSourceUtils.getMessage(PROJECT_HEADERS[i]))) {
        throw new BusinessException("XlsUtils file header is illegal: " + i,
            BusinessHttpResultEnum.ExcelHeaderIllegal);
      }
    }
  }

  public static void buildProjectXls(List<ProjectDomain> domainList, HttpServletResponse response) {
    Workbook workbook = new HSSFWorkbook();
    try {
      Sheet sheet = workbook.createSheet(CommonResourceBundleMessageSourceUtils.getMessage(LIST));
      // 自适应列宽度,暂时没兼容汉字
      sheet.autoSizeColumn(1);
      sheet.autoSizeColumn(1, true);
      writeHeader(workbook, sheet);
      writeSelectContent(sheet, 1, parseProjectTypeEnum());
      writeContent(workbook, sheet, domainList);
      workbook.write(response.getOutputStream());
    } catch (Exception e) {
      throw new BusinessException(e);
    } finally {
      try {
        workbook.close();
      } catch (IOException e) {
        // swallow
      }
    }
  }

  private static void writeContent(Workbook workbook, Sheet sheet, List<ProjectDomain> domainList) {
    if (domainList == null || domainList.size() == 0) {
      return;
    }

    Map<String, String> projectTypeMap = new HashMap<>();
    for (ProjectTypeEnum theEnum : ProjectTypeEnum.values()) {
      projectTypeMap.put(theEnum.code,
          CommonResourceBundleMessageSourceUtils.getMessage(theEnum.getLanguageCode()));
    }

    CellStyle style = buildStyle(workbook, sheet);
    CellStyle numberStyle = buildNumberStyle(workbook, sheet);
    CellStyle dateStyle = buildDateStyle(workbook, sheet);
    Cell cell;
    HSSFRichTextString text;
    int i = 1;
    for (ProjectDomain domain : domainList) {
      Row row = sheet.createRow(i++);

      int j = 0;
      cell = row.createCell(j++);
      cell.setCellStyle(style);
      String name = domain.getName();
      text = new HSSFRichTextString(name);
      cell.setCellValue(text);

      cell = row.createCell(j++);
      cell.setCellStyle(style);
      String type = domain.getType();
      String typeLanguage = projectTypeMap.get(type);
      text = new HSSFRichTextString(typeLanguage == null ? type : typeLanguage);
      cell.setCellValue(text);

      cell = row.createCell(j++);
      cell.setCellStyle(style);
      String employeeName = domain.getEmployeeName();
      text = new HSSFRichTextString(employeeName);
      cell.setCellValue(text);

      cell = row.createCell(j++);
      cell.setCellStyle(numberStyle);
      Integer money = domain.getMoney();
      cell.setCellValue(money);

      cell = row.createCell(j++);
      cell.setCellStyle(dateStyle);
      cell.setCellValue(domain.getTime());

      cell = row.createCell(j++);
      cell.setCellStyle(style);
      String note = domain.getNote();
      text = new HSSFRichTextString(note);
      cell.setCellValue(text);
    }
  }

  private static CellStyle buildStyle(Workbook workbook, Sheet sheet) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    // 生成字体
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 12);
    font.setBold(false);
    style.setFont(font);
    return style;
  }

  private static CellStyle buildNumberStyle(Workbook workbook, Sheet sheet) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.RIGHT);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    // 设置数字为整数: HSSFDataFormat.getBuiltinFormat("0.00")标识保留两位小数
    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0"));
    // 生成字体
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 12);
    font.setBold(false);
    style.setFont(font);
    return style;
  }

  private static CellStyle buildDateStyle(Workbook workbook, Sheet sheet) {
    CellStyle style = workbook.createCellStyle();
    style.setAlignment(HorizontalAlignment.CENTER);
    style.setVerticalAlignment(VerticalAlignment.CENTER);
    DataFormat format = workbook.createDataFormat();
    style.setDataFormat(format.getFormat("yyyy-m-d hh:mm:ss"));
    // 生成字体
    Font font = workbook.createFont();
    font.setFontHeightInPoints((short) 12);
    style.setFont(font);
    return style;
  }
}
