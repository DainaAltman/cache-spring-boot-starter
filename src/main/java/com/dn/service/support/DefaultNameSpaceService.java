package com.dn.service.support;

import com.dn.service.INameSpaceService;
import com.dn.utils.MethodUtils;

import java.lang.reflect.Method;

public class DefaultNameSpaceService implements INameSpaceService {

    @Override
    public String cacheName(Method method, Object[] args) {
        return MethodUtils.getMethodId(method, args);
    }

}
