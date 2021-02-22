package com.github.ywm.itg.core.pojo;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author ywm
 */
@Data
public class OperationLog {
	@NotNull(message = "operId不为空")
    private String operId;
	@NotNull(message = "操作模块不为空")
	private String operModul;
	@NotNull(message = "操作类型不为空")
	private String operType;
	/*操作描述*/
	private String operDesc;
	/*请求方法*/
	private String operMethod;
	/*请求参数*/
	private String operRequParam;
	/*返回结果*/
	private String operRespParam;
	/*操作人id*/
	private String operUserId;
	/*操作人姓名*/
	private String operUserName;
	/*操作人ip*/
	private String operIp;
	/*请求uri*/
	private String operUri;
	/*创建时间*/
	private String operCreateTime;
	/*操作版本*/
	private String operVer;
	/*sessionid*/
	private String sessionid;
}
