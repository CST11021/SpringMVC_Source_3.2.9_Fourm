package com.whz.service;

import com.whz.utils.ObjectPropertiesUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SystemParameterService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private String clazz;
    private Map<String, String> sysParams;
    private static final Logger logger = Logger.getLogger(SystemParameterService.class);

    public void init(){
        logger.info("开始系统参数表...");
        sysParams = getSystemParameter();
        logger.info("共初始化完" + sysParams.size() + "个系统参数.");

        if(StringUtils.isNotBlank(clazz)){
            logger.info("开始初始化类参数信息");
            try {
                Object object = Class.forName(clazz).newInstance();
                ObjectPropertiesUtil.copyStaticMustProperties(object, sysParams);
            } catch (Exception e) {
                logger.error("配置的初始化类名找不到" + clazz, e);
            }
        }
    }

    public Map<String, String> getSystemParameter() {

        final Map<String, String> sysParams = new HashMap<String, String>();

        jdbcTemplate.query("SELECT PARAM_NAME,PARAM_VALUE FROM T_SYS_PARAMETER", new RowCallbackHandler() {

            public void processRow(ResultSet rs) throws SQLException {
                String paramName = rs.getString("PARAM_NAME");
                String paramValue = rs.getString("PARAM_VALUE");
                if (StringUtils.isNotBlank(paramName)) {
                    sysParams.put(paramName, paramValue);
                    logger.info("系统参数:[" + paramName + "]的值是:" + paramValue);
                }
            }

        });
        return sysParams;
    }

    public void updateSysParam(String paramName, String paramValue) {
        jdbcTemplate.update("update T_SYS_PARAMETER set PARAM_VALUE=? where PARAM_NAME=? ", new String[]{paramValue, paramName});
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
