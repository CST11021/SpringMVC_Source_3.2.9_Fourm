package com.whz.propertyeditor;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class AddressPropertyEditor extends PropertyEditorSupport {

    //支持的格式为 streeValue,doorNumValue,postCode
    public void setAsText(String text) {
        System.out.println("使用自己的编辑器。");
        if (text == null || !StringUtils.hasText(text)) {
            throw new IllegalArgumentException("老大，不能为空啊！");
        } else {
            String[] strArr = StringUtils.tokenizeToStringArray(text, ",");
            Address add = new Address();
            add.setStreet(strArr[0]);
            add.setDoorNum(strArr[1]);
            add.setPostCode(strArr[2]);
            setValue(add);
        }
    }

    public String getAsText() {
        Address add = (Address) getValue();
        return "" + add;
    }
}
 