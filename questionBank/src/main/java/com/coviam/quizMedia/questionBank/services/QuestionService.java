package com.coviam.quizMedia.questionBank.services;

import com.coviam.quizMedia.questionBank.entity.Question;

import java.util.List;


public interface QuestionService {

    List<Question> getQuestionListFromFile(String fileName);
    List<Question> getAllQuestions();
    List<Question> getQuestionByCategory(String category);
    List<String> deleteByIdCategory(String id, String category);
    Question getQuestionByQueNo(String queNo);

}
