package com.github.ywm.itg.core.log.service;

import java.io.File;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.github.ywm.itg.core.exception.ItgExcption;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;

/**
 * @author ywm
 */
@Service
public class LogConfigService {

	public void updateLogByXml(MultipartFile file) throws Exception {
		if (file.isEmpty()) {
			throw new ItgExcption("{msg:文件不存在!请重新上传}", 500);
		}
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = (HttpServletRequest) requestAttributes
				.resolveReference(RequestAttributes.REFERENCE_REQUEST);
		String rootpath = request.getSession().getServletContext().getRealPath("/");
		File xmlFile = new File(rootpath + "/" + UUID.randomUUID().toString() + ".xml");
		xmlFile.createNewFile();
		file.transferTo(xmlFile);
		new ContextInitializer(loggerContext).configureByResource(xmlFile.toURI().toURL());
	}

	public void changeLevel(String name, String level) throws Exception {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.getLogger(StringUtils.isEmpty(name) ? "root" : name)
				.setLevel(Level.valueOf(StringUtils.isEmpty(level) ? "INFO" : level.toUpperCase()));
	}
}
