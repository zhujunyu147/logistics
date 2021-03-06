/*
 * FileName:	StreamTubeListener.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2014-8-26 1.00 初始版本
 */
package com.honeywell.net.listener;

import org.apache.http.HttpEntity;

/**
 * Http请求返回类型是Stream的接口
 * 
 * @author zhujunyu
 */
public interface EntityTubeListener<Result> extends
        TubeListener<HttpEntity, Result> {

}
