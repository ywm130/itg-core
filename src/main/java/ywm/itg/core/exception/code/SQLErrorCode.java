package ywm.itg.core.exception.code;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.log4j.Log4j2;


/**
 * @author ywm
 */
@Log4j2
public class SQLErrorCode {
	private static ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<Integer, String>();

	public static void put(Integer code, String msg) {
		map.put(code, msg);
	}

	final public static String SQL_ERROR_CODE_FILE = "sqlErrorCode.properties";
	static {
		Properties properties = new Properties();
		// 使用ClassLoader加载properties配置文件生成对应的输入流
		InputStream in = ErrorCode.class.getClassLoader().getResourceAsStream(SQL_ERROR_CODE_FILE);
		// 使用properties对象加载输入流
		try {
			properties.load(new InputStreamReader(in, "UTF-8"));
			for (Map.Entry<Object, Object> entry : properties.entrySet()) {
				map.put(Integer.parseInt(entry.getKey().toString()), entry.getValue().toString());
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		// 获取key对应的value值
		for (Entry<Integer, String> entry : map.entrySet()) {
			System.out.println("sql错误代码：" + entry.getKey() + ":" + entry.getValue());
		}
	}

	public static String errorInfo(Integer code) {
		return map.get(code);
	}
}