package com.whz.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
      
//根据身份证提取信息，如：省份、性别、年龄、出生日期
public class IdcardInfoExtractor {

    private String province;
    private String city;
    private String region;
    private int year;
    private int month;
    private int day;
    private String gender;
    private Date birthday;
    private int age;

    private Map<String, String> cityCodeMap = new HashMap<String, String>() {
        {
            this.put("11", "北京");
            this.put("12", "天津");
            this.put("13", "河北");
            this.put("14", "山西");
            this.put("15", "内蒙古");
            this.put("21", "辽宁");
            this.put("22", "吉林");
            this.put("23", "黑龙江");
            this.put("31", "上海");
            this.put("32", "江苏");
            this.put("33", "浙江");
            this.put("34", "安徽");
            this.put("35", "福建");
            this.put("36", "江西");
            this.put("37", "山东");
            this.put("41", "河南");
            this.put("42", "湖北");
            this.put("43", "湖南");
            this.put("44", "广东");
            this.put("45", "广西");
            this.put("46", "海南");
            this.put("50", "重庆");
            this.put("51", "四川");
            this.put("52", "贵州");
            this.put("53", "云南");
            this.put("54", "西藏");
            this.put("61", "陕西");
            this.put("62", "甘肃");
            this.put("63", "青海");
            this.put("64", "宁夏");
            this.put("65", "新疆");
            this.put("71", "台湾");
            this.put("81", "香港");
            this.put("82", "澳门");
            this.put("91", "国外");
        }
    };
            
    private IdcardValidatorUtil validator = null;

    // 通过构造方法初始化各个成员属性
    public IdcardInfoExtractor(String idcard) {
        try {
            validator = new IdcardValidatorUtil();
            if (validator.isValidatedAllIdcard(idcard)) {
                if (idcard.length() == 15) {
                    idcard = validator.convertIdcarBy15bit(idcard);
                }
                // 获取省份
                String provinceId = idcard.substring(0, 2);
                Set<String> key = this.cityCodeMap.keySet();
                for (String id : key) {
                    if (id.equals(provinceId)) {
                        this.province = this.cityCodeMap.get(id);
                        break;
                    }
                }

                // 获取性别
                String id17 = idcard.substring(16, 17);
                if (Integer.parseInt(id17) % 2 != 0) {
                    this.gender = "男";
                } else {
                    this.gender = "女";
                }

                // 获取出生日期
                String birthday = idcard.substring(6, 14);
                Date birthdate = new SimpleDateFormat("yyyyMMdd").parse(birthday);
                this.birthday = birthdate;
                GregorianCalendar currentDay = new GregorianCalendar();
                currentDay.setTime(birthdate);
                this.year = currentDay.get(Calendar.YEAR);
                this.month = currentDay.get(Calendar.MONTH) + 1;
                this.day = currentDay.get(Calendar.DAY_OF_MONTH);

                //获取年龄
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy");
                String year=simpleDateFormat.format(new Date());
                this.age=Integer.parseInt(year)-this.year;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProvince() {
        return province;
    }
    public String getCity() {
        return city;
    }
    public String getRegion() {
        return region;
    }
    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }
    public String getGender() {
        return gender;
    }
    public Date getBirthday() {
        return birthday;
    }
    public int getAge() {
        return age;
    }
    @Override
    public String toString() {
        return "省份：" + this.province +
                "，城市：" + this.city +
                "，区域：" + this.getRegion() +
                "，性别：" + this.gender +
                "，出生日期：" + this.birthday +
                "，年龄：" + this.age;
    }
    public static void main(String[] args) {
        String idcard = "350583199209206317";
        IdcardInfoExtractor ie = new IdcardInfoExtractor(idcard);
        System.out.println(ie.toString());
    }

}