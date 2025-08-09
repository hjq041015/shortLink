package com.shortLink.admin.common.convention.exception;

import com.shortLink.admin.common.convention.errorcode.BaseErrorCode;
import com.shortLink.admin.common.convention.errorcode.IErrorCode;

public class ServiceException extends AbstractException{

    public ServiceException(IErrorCode errorCode) {
        this(null,null,errorCode);
    }

    public ServiceException(String message) {
        this(message,null, BaseErrorCode.SERVICE_ERROR);
    }

    public ServiceException(String message,IErrorCode errorCode) {
        this(message,null,errorCode);
    }


    public ServiceException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }
}
