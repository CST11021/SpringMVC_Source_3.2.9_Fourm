package com.whz.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public enum Pattern{
        yyyyMMddHHmmssSSS,yyyyMMddHHmmss,yyyyMMddHHmm,yyyyMMddHH,yyyyMMdd,yyyyMM,yyyy;
    }
    public enum Unit{
        y,M,d,H,m,s,S;
    }

    public DateUtil() {}

    /**
     * 判断字符串date是否为合法时间字符串，合法格式包括：yyyyMMddHHmmssSSS、yyyyMMddHHmmss、yyyyMMddHHmm、yyyyMMddHH、yyyyMMdd、yyyyMM、yyyy
     * @param date 时间字符串，不包含“./- 年月日时分秒”特殊字符
     * @return boolean
     */
    public static boolean isValidDate(String date) {
        boolean isValid = true;
        if(StringUtils.isNotBlank(date)){
            int len = StringUtils.length(date);
            SimpleDateFormat format;
            switch (len){
                case 17:
                    format = new SimpleDateFormat(Pattern.yyyyMMddHHmmssSSS.toString());
                    break;
                case 14:
                    format = new SimpleDateFormat(Pattern.yyyyMMddHHmmss.toString());
                    break;
                case 12:
                    format = new SimpleDateFormat(Pattern.yyyyMMddHHmm.toString());
                    break;
                case 10:
                    format = new SimpleDateFormat(Pattern.yyyyMMddHH.toString());
                    break;
                case 8:
                    format = new SimpleDateFormat(Pattern.yyyyMMdd.toString());
                    break;
                case 6:
                    format = new SimpleDateFormat(Pattern.yyyyMM.toString());
                    break;
                case 4:
                    format = new SimpleDateFormat(Pattern.yyyy.toString());
                    break;
                default: return false;
            }

            try {
                format.setLenient(false);//严格校验，比如：2006－02－31根本没有这一天 ，也会认为时间格式不对
                format.parse(date);
            } catch (ParseException e) {
                isValid = false;
            }
        }
        else{
            isValid = false;
        }
        return isValid;
    }

    /**
     * 判断字符串date是否为指定日期格式
     * @param date 时间字符串，不包含“./- 年月日时分秒”特殊字符
     * @param pattern 枚举类型
     * @return boolean
     */
    public static boolean isValidDate(String date,Pattern pattern) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat(pattern.toString());
        try {
            format.setLenient(false);
            format.parse(date);
        } catch (ParseException e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    /**
     * 根据日期字符串date获取日期格式，无效日期时返回null
     * @param date 时间字符串，不包含“./- 年月日时分秒”特殊字符
     * @return String
     */
    public static String getPatternByDate(String date){
        String pattern = null;
        if(isValidDate(date)){
            int len = StringUtils.length(date);
            switch (len){
                case 17:
                    pattern = Pattern.yyyyMMddHHmmssSSS.toString();
                    break;
                case 14:
                    pattern = Pattern.yyyyMMddHHmmss.toString();
                    break;
                case 12:
                    pattern = Pattern.yyyyMMddHHmm.toString();
                    break;
                case 10:
                    pattern = Pattern.yyyyMMddHH.toString();
                    break;
                case 8:
                    pattern = Pattern.yyyyMMdd.toString();
                    break;
                case 6:
                    pattern = Pattern.yyyyMM.toString();
                    break;
                case 4:
                    pattern = Pattern.yyyy.toString();
                    break;
            }
        }
        return pattern;
    }

    /**
     * 将String转为Date
     * @param date 时间字符串，不包含“./- 年月日时分秒”特殊字符,并且是合法的日期
     * @return Date
     */
    public static Date parse(String date){
        String pattern = getPatternByDate(date);
        Date newDate = null;
        try {
            newDate = new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    /**
     * 将Date转为String
     * @param date Date
     * @param pattern 自定义格式
     * @return String
     */
    public static String format(Date date,String pattern){
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * 根据生日计算年龄
     * @param birthday Date类型
     * @return int
     */
    public static int getAgeByBirthday(Date birthday){
        int age = 0;
        Calendar cal = Calendar.getInstance();
        if(cal.before(birthday)){
            throw new IllegalArgumentException("the birday is before");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthday);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH)+1;
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow-yearBirth;
        if(monthNow<=monthBirth){
            if(monthNow==monthBirth){
                if(dayOfMonthNow<dayOfMonthBirth){
                    age--;
                }
            }else{
                age--;
            }
        }
        return age;
    }

    /**
     * 计算时间，interval可为负数，表示时间扣除
     * @param interval 时间差
     * @param unit 单位Unit
     * @param date 时间Date
     * @return Date
     */
    public static Date calculateTime(int interval, Unit unit, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String dw = unit.toString();
        if ("y".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.YEAR, interval);
        } else if ("M".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.MONTH, interval);
        } else if ("d".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.DAY_OF_YEAR, interval);
        } else if ("H".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.HOUR, interval);
        } else if ("m".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.MINUTE, interval);
        } else if ("s".equalsIgnoreCase(dw)) {
            calendar.add(Calendar.SECOND, interval);
        } else{
            calendar.add(Calendar.MILLISECOND, interval);
        }
        Date dat = calendar.getTime();
        return dat;
    }

    /**
     * 获取系统当前时间，主要用于计算程序的执行时间<br/>
     * 示例：<br/>
     * <pre>
     *     long startTime = currentTimeMillis();
     *     do someThing...
     *     long endTime = currentTimeMillis();
     *     long intervalMis = endTime - startTime;
     * </pre>
     * @return
     */
    public static long currentTimeMillis(){
        return System.currentTimeMillis();
    }

    /**
     * 删除特殊字符，包括：“年月日时分秒 :-/.”
     * @param date
     * @return
     */
    public static String delSpecialChar(String date){
        return StringUtils.replaceChars(date, "年月日时分秒 :-/.", "");
    }

    /**
     * 根据给定的date获取当前星期几
     * @param date
     * @return
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (weekIndex < 0)
            weekIndex = 0;
        return weekDays[weekIndex];
    }


    public static void main(String[] args) throws ParseException {
        Date date = new Date();
        System.out.println(currentTimeMillis());
        System.out.println(getWeekOfDate(date));
    }
}














