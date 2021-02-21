package ywm.itg.core.exception.handler;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionException;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.log4j.Log4j2;
import ywm.itg.core.exception.ItgExcption;
import ywm.itg.core.exception.code.ErrorCode;
import ywm.itg.core.exception.code.SQLErrorCode;
import ywm.itg.core.pojo.ExceptionLog;
import ywm.itg.core.service.IdGeneratorService;
import ywm.itg.core.utils.ItgUtils;
import ywm.itg.core.vo.R;

/**
 * @author ywm
 */
@Log4j2
@ControllerAdvice
public class ItgExceptionHandler {

	@Value("${version:1.0}")
	private String operVer;
	@Autowired
	private IdGeneratorService idGenerator;

	@ResponseBody
	@ExceptionHandler(value = Exception.class)
	public R exceptionHandle(Exception e) {
		// 获取RequestAttributes
		final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		// 从获取RequestAttributes中获取HttpServletRequest的信息
		final HttpServletRequest request = (HttpServletRequest) requestAttributes
				.resolveReference(RequestAttributes.REFERENCE_REQUEST);
		String userid = (String) request.getSession().getAttribute("userid");
		ExceptionLog excepLog = new ExceptionLog();
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		Map<String, String> rtnMap = ItgUtils.converMap(request.getParameterMap());
		// 将参数所在的数组转换成json
		String params = gson.toJson(rtnMap);
		excepLog.setExcId(String.valueOf(idGenerator.nextId()));
		excepLog.setExcRequParam(params); // 请求参数
		excepLog.setExcName(e.getClass().getName()); // 异常名称
		excepLog.setExcMessage(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace())); // 异常信息
		if (userid != null)
			excepLog.setOperUserId(userid);
		excepLog.setOperUri(request.getRequestURI()); // 操作URI
		excepLog.setOperIp(ItgUtils.getIPAddress(request)); // 操作员IP
		if (!StringUtils.isEmpty(operVer))
			excepLog.setOperVer(operVer); // 操作版本号
		excepLog.setOperCreateTime(new Timestamp(System.currentTimeMillis()).toString()); // 发生异常时间
		String logStr = gson.toJson(excepLog);
		log.error(logStr);
		R r = null;
		/* 全局自定义异常捕捉处理 */
		if (e instanceof ItgExcption) {
			ItgExcption itgerr = (ItgExcption) e;
			r = R.error(itgerr.getCode(), itgerr.getMessage());
		}
		/* 操作数据库发生异常捕捉处理 */
		else if (e instanceof SQLException) {
			SQLException sqlerr = (SQLException) e;
			String errInfo = SQLErrorCode.errorInfo(sqlerr.getErrorCode());
			errInfo = StringUtils.isEmpty(errInfo) ? sqlerr.getMessage() : errInfo;
			r = R.error(sqlerr.getErrorCode(), "操作数据库发生异常：" + errInfo);
		}
		/* 系统发生转型异常捕捉处理 */
		else if (e instanceof ClassCastException) {
			r = R.error(ErrorCode.ERRORCODEMAP.get("CLASS_CAST_ERROR").getCode(),
					ErrorCode.ERRORCODEMAP.get("CLASS_CAST_ERROR").getMsg());
		}
		/* 方法参数异常捕捉处理 */
		else if (e instanceof MethodArgumentNotValidException) {
			MethodArgumentNotValidException de = (MethodArgumentNotValidException) e;
			List<FieldError> list = de.getBindingResult().getFieldErrors();
			int size = list.size();
			String[] errInfos = new String[size];
			for (int i = 0; i < size; i++) {
				errInfos[i] = list.get(i).getDefaultMessage();
			}
			r = R.error(ErrorCode.ERRORCODEMAP.get("METHOD_ARGUMENT_NOTVALID_ERROR").getCode(),
					ErrorCode.ERRORCODEMAP.get("METHOD_ARGUMENT_NOTVALID_ERROR").getMsg());
		}
		/* 使用BeanUtils时发生异常！ */
		else if (e instanceof ConversionException) {
			r = R.error(ErrorCode.ERRORCODEMAP.get("NO_VALUE_SPECIFIED_FOR_DATE").getCode(),
					ErrorCode.ERRORCODEMAP.get("NO_VALUE_SPECIFIED_FOR_DATE").getMsg());
		} else {
			r = R.error(ErrorCode.ERRORCODEMAP.get("SERVER_ERROR").getCode(),
					ErrorCode.ERRORCODEMAP.get("SERVER_ERROR").getMsg());
		}
		return r;
	}

	/**
	 * 转换异常信息为字符串
	 *
	 * @param exceptionName    异常名称
	 * @param exceptionMessage 异常信息
	 * @param elements         堆栈信息
	 */
	private String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
		StringBuffer strbuff = new StringBuffer();
		for (StackTraceElement stet : elements) {
			strbuff.append(stet + "\n");
			if (strbuff.length() > 2000) {
				break;
			}
		}
		String message = exceptionName + ":" + exceptionMessage + "\n\t" + strbuff.toString();
		return message;
	}
}
