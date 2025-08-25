package com.example.quiz15.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
@IdClass(value = QuestionId.class)
public class Question {

	@Id
	@Column(name = "quiz_id")
	private int quizId;

	@Id
	@Column(name = "question_id")
	private int questionId;
	
	@Column(name = "question")
	private String question;

	@Column(name = "type")
	private String type;

	@Column(name = "is_required")
	private boolean required;

	@Column(name = "option")
	private String option;

	public int getQuizid() {
		return quizId;
	}

	public void setQuizid(int quizid) {
		this.quizId = quizid;
	}

	public int getQuestionid() {
		return questionId;
	}

	public void setQuestionid(int questionid) {
		this.questionId = questionid;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	

}
