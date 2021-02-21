package ywm.itg.core.aspect;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.log4j.Log4j2;
import ywm.itg.core.annotation.OperLog;
import ywm.itg.core.pojo.OperationLog;
import ywm.itg.core.service.IdGeneratorService;
import ywm.itg.core.utils.ItgUtils;

/**
 * 切面处理类，操作日志异常日志记录处理
 *
 * @author ywm
 */
@Log4j2
@Aspect
@Component
public class OperLogAspect {
	
	@Autowired
	private IdGeneratorService idGenerator;
    /**
     * 操作版本号
     * <p>
     * 项目启动时从命令行传入，例如：java -jar xxx.war --version=201902
     * </p>
     */
    @Value("${version:1.0}")
    private String operVer;

    /**
     * 设置操作日志切入点 记录操作日志 在注解的位置切入代码
     */
    @Pointcut("@annotation(ywm.itg.core.annotation.OperLog)")
    public void operLogPoinCut() {
    }

    /**
     * 正常返回通知，拦截用户操作日志，连接点正常执行完成后执行， 如果连接点抛出异常，则不会执行
     *
     * @param joinPoint 切入点
     * @param keys      返回结果
     */
    @AfterReturning(value = "operLogPoinCut()", returning = "keys")
    public void saveOperLog(JoinPoint joinPoint, Object keys) {
    	Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes
                .resolveReference(RequestAttributes.REFERENCE_REQUEST);
        String sessionid = request.getSession().getId();
        String userid = (String) request.getSession().getAttribute("userid");
        OperationLog operlog = new OperationLog();
        try {
//            operlog.setOperId(UUID.randomUUID().toString()); // 主键ID
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            // 获取切入点所在的方法
            Method method = signature.getMethod();
            // 获取操作
            OperLog opLog = method.getAnnotation(OperLog.class);
            String level = "";
            if (opLog != null) {
                String operModul = opLog.operModul();
                String operType = opLog.operType();
                String operDesc = opLog.operDesc();
                level = opLog.operLevel();
                operlog.setOperModul(operModul); // 操作模块
                operlog.setOperType(operType); // 操作类型
                operlog.setOperDesc(operDesc); // 操作描述
            }
            // 获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            // 获取请求的方法名
            String methodName = method.getName();
            methodName = className + "." + methodName;
            operlog.setOperId(String.valueOf(idGenerator.nextId()));
            operlog.setOperMethod(methodName); // 请求方法
            operlog.setSessionid(sessionid);
            // 请求的参数
            Map<String, String> rtnMap = ItgUtils.converMap(request.getParameterMap());
            // 将参数所在的数组转换成json
            String params = gson.toJson(rtnMap);
            operlog.setOperRequParam(params); // 请求参数
            operlog.setOperRespParam(gson.toJson(keys)); // 返回结果
            if(userid != null) 
            	operlog.setOperUserId(userid); // 请求用户ID
            operlog.setOperIp(ItgUtils.getIPAddress(request)); // 请求IP
            operlog.setOperUri(request.getRequestURI()); // 请求URI
            operlog.setOperCreateTime(new Timestamp(System.currentTimeMillis()).toString()); // 创建时间
            if(!StringUtils.isEmpty(operVer)) 
            	operlog.setOperVer(operVer); // 操作版本号
//            operationLogService.insert(operlog);
            //打印log
            String logStr = gson.toJson(operlog);
            log.log(Level.toLevel(level), logStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}