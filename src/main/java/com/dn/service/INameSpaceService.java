package com.dn.service;

import java.lang.reflect.Method;


public interface INameSpaceService {

    String cacheName(Method method, Object[] args);
}
