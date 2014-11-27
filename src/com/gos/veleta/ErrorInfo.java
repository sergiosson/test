package com.gos.veleta;

import android.content.res.Resources;

public class ErrorInfo extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int errorCode;

	String errorMessage;

	public ErrorInfo(int code) {
		this.errorCode = code;

	}

	public ErrorInfo(String error) {
		this.errorMessage = error;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public CharSequence getMessage(Resources resources) {
		if (errorMessage != null) {
			return errorMessage;
		} else {

			return resources.getString(errorCode);
		}
	}
}
