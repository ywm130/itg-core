package ywm.itg.core.exception;

import ywm.itg.core.exception.code.ErrorCode;

public class ItgExcption extends RuntimeException {
	private static final long serialVersionUID = -1376668028768465706L;

	private Integer code;
 
	/**
	 * 继承exception，加入错误状态值
	 * @param exceptionEnum
	 */
	public ItgExcption(ErrorCode exceptionEnum) {
		super(exceptionEnum.getMsg());
		this.code = exceptionEnum.getCode();
	}
	public ItgExcption(ErrorCode exceptionEnum,Exception e) {
		super(exceptionEnum.getMsg(),e);
		this.code = exceptionEnum.getCode();
	}
	/**
	 * 自定义错误信息
	 * @param message
	 * @param code
	 */
	public ItgExcption(String message, Integer code) {
		super(message);
		this.code = code;
	}
 
	public Integer getCode() {
		return code;
	}
 
	public void setCode(Integer code) {
		this.code = code;
	}

}
