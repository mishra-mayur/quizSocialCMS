package com.coviam.quizSocialCMS.CMS.service.impl;

import com.coviam.quizSocialCMS.CMS.entity.StaticContestEntityClass;
import com.coviam.quizSocialCMS.CMS.repository.StaticContestRepository;
import com.coviam.quizSocialCMS.CMS.service.StaticContestInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaticContestService implements StaticContestInterface {

    @Autowired
    StaticContestRepository staticContestRepository;

    @Override
    public StaticContestEntityClass saveContest(StaticContestEntityClass staticContestEntityClass) {
        return staticContestRepository.save(staticContestEntityClass);
    }

    @Override
    public StaticContestEntityClass getContestById(String id) {
        return staticContestRepository.findOne(id);
    }

    @Override
    public void deleteContestById(String id) {
        staticContestRepository.delete(id);
    }

    @Override
    public List<StaticContestEntityClass> getActiveContest() {
        return staticContestRepository.findAll();
    }

    @Override
    public List<StaticContestEntityClass> getContestByCategory(String category) {
        return staticContestRepository.findByCategory(category);
    }

    @Override
    public List<StaticContestEntityClass> getContestByContestName(String name) {
        return staticContestRepository.findByContestName(name);
    }

    @Override
    public List<StaticContestEntityClass> findAll() {
        return staticContestRepository.findAll();
    }


}
