package com.example.quiz15.vo;

import java.util.List;

public class QuestionRes extends BasicRes{

	private List<QuestionVO>questionVoList;

	public QuestionRes() {
		super();
	}

	public QuestionRes(int code, String message) {
		super(code, message);
	}

	public QuestionRes(int code, String message, List<QuestionVO> questionVoList) {
		super(code, message);
		this.questionVoList = questionVoList;
	}

	public List<QuestionVO> getQuestionVoList() {
		return questionVoList;
	}

	public void setQuestionVoList(List<QuestionVO> questionVoList) {
		this.questionVoList = questionVoList;
	}
	
}
