package com.zrb.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

// 读取配置文件中以prefix打头的配置并将其封装为bean返回
// 目前只支持读取xxx.properties配置文件
public class ConfigUtils {

    public static <T> T loadConfig(Class<T> tClass,String prefix){
        return loadConfig(tClass,prefix,"");
    }

    public static <T> T loadConfig(Class<T> tClass,String prefix,String environment){

        StringBuilder sb = new StringBuilder("application");
        if(StrUtil.isNotBlank(environment)){
            sb.append("-").append(environment);
        }
        sb.append(".properties");

        // 将配置文件中前缀是prefix的配置转化为Bean
        Props props = new Props(sb.toString());
        return props.toBean(tClass,prefix);
    }
}
