package com.claropr.service;

import com.claropr.model.IccLov;

public interface ICCService {
    IccLov getIccLovByKey(String key);
    void insertErrorInDB(String system, String summary, String details, String stackTrace, String className, String methodName);
}

