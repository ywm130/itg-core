package ywm.itg.core.pojo;

import lombok.Data;

/**
 * @author ywm
 */
@Data
public class ExceptionLog {
	private String excId;
	private String excRequParam; // 请求参数
	private String operMethod; // 请求方法名
    private String excName; // 异常名称
    private String excMessage; // 异常信息
    private String operUserId; // 操作员ID
    private String operUri; // 操作URI
    private String operIp; // 操作员IP
    private String operVer; // 操作版本号
    private String operCreateTime; // 发生异常时间
}
