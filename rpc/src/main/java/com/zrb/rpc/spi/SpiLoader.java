package com.zrb.rpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.zrb.rpc.serializer.Serializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SpiLoader {

    // key: 接口的全限定名
    // value:{ key:name value: 接口的实现类型 }
    private static final Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();

    // 存储实例缓存，避免重复创建
    // key: 实现类的全限定名
    // value: 实现类
    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    // 路径
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";

    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    // 扫描路径
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    // 动态加载的类列表
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    public static void loadAll() {
        System.out.println("加载所有SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    // 获取实现了接口tClass的实例对象，该对象简称为key
    public static <T> T getInstance(Class<?> tClass, String key) {

        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);

        // 没有这个接口
        if(keyClassMap==null){
            throw new RuntimeException(String.format("未加载%s类型",tClassName));
        }
        if(!keyClassMap.containsKey(key)){
            throw new RuntimeException(String.format("接口%s的实现类%s不存在",tClassName,key));
        }

        // 获取到接口的实现类型
        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();

        // 从缓存中加载直到类型的实例
        // 用的时候才new
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException("类加载失败", e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }

    // 加载某个接口的所有实现类，实现类的对象还没有new出来，属于懒加载
    public static Map<String,Class<?>> load(Class<?> loadClass){
        // 扫描路径
        HashMap<String, Class<?>> keyClassMap = new HashMap<>();
        for(String scanDir:SCAN_DIRS){
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            for(URL resource:resources){
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while((line=bufferedReader.readLine())!=null){
                        String[] strArray = line.split("=");
                        if(strArray.length>1){
                            String key=strArray[0];
                            String className=strArray[1];
                            keyClassMap.put(key,Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        loaderMap.put(loadClass.getName(),keyClassMap);
        return keyClassMap;
    }

}
