package com.example.quiz15.constants;

public enum ResCodMessage {
	SUCCESS(200, "Success!!"),
	ADD_INFO_ERROR(400, "Add Info Error"),
	PASSWORD_ERROR(400,"Password Error"),
	EMAIL_EXISTS(400, "Account Exists"),
	NOT_FOUND(404, "Not Found"),
	PASSWORD_MISMATCH(400, "Password Mismatch"),
	QUIZ_CREATE_ERROR(400, "Quiz Create Error"),
	DATE_FORMAT_ERROR(400, "Date Format Error"),
	QUESTION_TYPE_ERROR(400, "Question Type Error"),
	OPTIONS_INSUFFICIENT(400, "Options Insufficient!!"),
	TEXT_HAS_OPTIONS_ERROR(400, "Text Has Options Error!!"),
	QUIZ_UPDATE_FAILED(400, "Quiz Update Failed!!"),
	OPTIONS_TRANSFER_ERROR(400,"Options Transfer Error!!")
	;
	
	private int code;
	
	private String message;

	//enum 沒有預設建構方法
	//帶有參數的建構方法一定要有
	private ResCodMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
