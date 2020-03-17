package kr.co.esjee.cloud.job;

import kr.co.esjee.cloud.constant.ErrorConstant.ERROR_CODE;

public class BaseJobException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private ERROR_CODE errorCode = ERROR_CODE.F9999;

	public BaseJobException(Exception e, ERROR_CODE errorCode) {
		super(e);
		this.errorCode = errorCode;
	}

	public BaseJobException(ERROR_CODE errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public ERROR_CODE getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(ERROR_CODE errorCode) {
		this.errorCode = errorCode;
	}

	public Object getErrorMessage() {
		if((this.getMessage() == null)) {
			return errorCode.getMessage();
		} else {
			return "[Exception : " + this.getMessage()+ "]" + errorCode.getMessage();
		}
	}
}
