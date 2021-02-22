package com.github.ywm.itg.core.exception.code;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ErrorCode {
	public static ConcurrentHashMap<String ,ErrorCode> ERRORCODEMAP = new ConcurrentHashMap<String ,ErrorCode>();
	final public static String ERROR_CODE_FILE = "errorCode.properties";
    static {
    	ERRORCODEMAP.put("SUCCESS", new ErrorCode("SUCCESS", 0, "成功"));
    	ERRORCODEMAP.put("CLASS_CAST_ERROR", new ErrorCode("CLASS_CAST_ERROR", 10009, "系统发生转型异常！"));
    	Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = ErrorCode.class.getClassLoader().getResourceAsStream(ERROR_CODE_FILE);
        // 使用properties对象加载输入流
        try {
			properties.load(new InputStreamReader(in, "UTF-8"));
			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				String[] value = entry.getValue().toString().split(",");
				ErrorCode error = new ErrorCode(entry.getKey().toString(), Integer.parseInt(value[0]), value[1]);
				ERRORCODEMAP.put(entry.getKey().toString(), error);
	        }
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
    }
	
	private ErrorCode(String type, Integer code, String msg) {
		this.type = type;
		this.code = code;
		this.msg = msg;
	}

	private Integer code;
	private String msg;
    private String type;
	
	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}
    
	public String getType() {
		return type;
	}
}