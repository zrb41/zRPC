package com.zrb.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 方法返回值的类型
        Class<?> returnType = method.getReturnType();
        return getDefaultObject(returnType);
    }

    private Object getDefaultObject(Class<?> type){

        if(type==boolean.class){
            return false;
        }else if(type==int.class||type==Integer.class){
            return 0;
        }else if(type==double.class||type==Double.class){
            return 0.0;
        }else if(type==String.class){
            return "空字符串";
        } else{
            return null;
        }
    }
}
