package com.coviam.quizMedia.Statistics.services.impl;

import com.coviam.quizMedia.Statistics.dto.LeaderBoardDto;
import com.coviam.quizMedia.Statistics.dto.ScoreDto;
import com.coviam.quizMedia.Statistics.entity.LeaderBoardEntity;
import com.coviam.quizMedia.Statistics.entity.Question;
import com.coviam.quizMedia.Statistics.entity.Score;
import com.coviam.quizMedia.Statistics.entity.State;
import com.coviam.quizMedia.Statistics.repository.ScoreRepository;
import com.coviam.quizMedia.Statistics.services.LeaderBoardService;
import com.coviam.quizMedia.Statistics.services.ScoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

import java.lang.Math.*;

import static java.lang.Integer.min;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Bean
    public RestTemplate restTemplate(@Lazy RestTemplateBuilder builder) {
        // Do any additional configuration here
        return builder.build();
    }

    @Autowired
    ScoreRepository scoreRepository;

    @Override
    public List<Score> getScoreByUserId(String userId) {
        return scoreRepository.getScoreByUserId(userId);
    }

    @Autowired
    RestTemplate    restTemplate;
    @Autowired
    LeaderBoardService leaderBoardService;
    @Autowired
    ScoreService scoreService;

    @Autowired
    LeaderBoardDbService leaderBoardDbService;

    @Override
    public List<Score> fetchScore(State state) {
        List<Score> scoreList = scoreRepository.getScoreByContestId(state.getContestId());

        return scoreList;
    }

    @Override
    public List<Score> scoreByContestId(String contestId) {
        return scoreRepository.getScoreByContestId(contestId);
    }

    @Override
    public List<Score> saveScore(State state) {
        Map<String, String> response = state.getResponse();
        List<String> questionNoList = new ArrayList<>();
        for (Map.Entry<String, String> entry : response.entrySet()){
            questionNoList.add(entry.getKey());
        }
        List<String> answerList = new ArrayList<>();
        List<Question> questionList = new ArrayList<>();
        for (int i = 0; i < questionNoList.size(); i++){
            String queNo = questionNoList.get(i);
            String url = "http://localhost:9081/questionbank/getQuestion/"+queNo;
            restTemplate=new RestTemplate();
            Question question = restTemplate.getForObject(url, Question.class);
            questionList.add(question);
            answerList.add(question.getRightAnswer());
        }
        int points = 0;
        int i = 0;
        int unSkipped = 0;
        for (Map.Entry<String, String> entry : response.entrySet()){

            if (entry.getValue().toUpperCase().equals(answerList.get(i).toUpperCase())){
                if (questionList.get(i).getDifficultyLevel().toUpperCase().equals("EASY")) {
                    points++;
                }
                if (questionList.get(i).getDifficultyLevel().toUpperCase().equals("AVERAGE")) {
                    points+=2;
                }
                if (questionList.get(i).getDifficultyLevel().toUpperCase().equals("HARD")) {
                    points+=3;
                }
                unSkipped++;
            }
            i++;
        }
        if (unSkipped == response.entrySet().size()){points+=5;}

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        Score score = new Score(state.getUserId(),state.getContestId(),state.getContestName(),points,date);
        List<Score> list = new ArrayList<>();
        list.add(score);
        List<Score> scoreList = scoreRepository.save(list);
        return scoreList;
    }

    public void calculateGlobalLeaderBoard(String id,String name)
    {
        List<Score> scoreList=scoreRepository.findAll();
        HashSet<String> userIds=new HashSet<>();
        List<LeaderBoardDto> leaderBoardDtoList=new ArrayList<>();
        HashMap<String,String> userIdScoreList=new HashMap<>();
        for(Score score:scoreList)
        {
            userIds.add(score.getUserId());
            if(userIdScoreList.get(score.getUserId())==null){
                userIdScoreList.put(score.getUserId(), String.valueOf(score.getPoints()));
            }
            else {
                int newScore=score.getPoints()+Integer.parseInt(userIdScoreList.get(score.getUserId()));
                userIdScoreList.replace(score.getUserId(), String.valueOf(newScore));
            }

        }
        List<String> list= new ArrayList<>(userIds);
        List<String> nameList=leaderBoardService.getUserNameList(list);
        System.out.println(nameList);

        List<LeaderBoardDto> leaderBoardDtos=new ArrayList<>();
        for(int i=0;i<userIds.size();i++)
        {
            LeaderBoardDto leaderBoardDto=new LeaderBoardDto(list.get(i),nameList.get(i),Integer.parseInt(userIdScoreList.get(list.get(i))));
            leaderBoardDtos.add(leaderBoardDto);
        }
        Collections.sort(leaderBoardDtos);
        System.out.println(leaderBoardDtos);
        LeaderBoardEntity leaderBoardEntity=new LeaderBoardEntity(id,name,leaderBoardDtos);
        leaderBoardDbService.save(leaderBoardEntity);
    }


    public void calculateWeeklyLeaderBoard(String id,String name)
    {
        List<Score> scoreList=scoreRepository.findAll();
        HashSet<String> userIds=new HashSet<>();
        List<LeaderBoardDto> leaderBoardDtoList=new ArrayList<>();
        HashMap<String,String> userIdScoreList=new HashMap<>();
        List<Score> filterScoreList = new ArrayList<>();
            LocalDate today = LocalDate.now();
            // Go backward to get Monday
            LocalDate monday = today;
            while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
                monday = monday.minusDays(1);
            }

            // Go forward to get Sunday
            LocalDate sunday = today;
            while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                sunday = sunday.plusDays(1);
            }

            for (Score score : scoreList){
                Date timestamp = score.getTimeStamp();
                Instant instant = Instant.ofEpochMilli(timestamp.getTime());
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                LocalDate submissionTime = localDateTime.toLocalDate();
                if (((submissionTime.isBefore(sunday))||(submissionTime.equals(sunday))&&(submissionTime.isAfter(monday))||(submissionTime.equals(monday)))){
                    filterScoreList.add(score);
                }

            }
        for(Score score:filterScoreList)
        {
            userIds.add(score.getUserId());
            if(userIdScoreList.get(score.getUserId())==null){
                userIdScoreList.put(score.getUserId(), String.valueOf(score.getPoints()));
            }
            else {
                int newScore=score.getPoints()+Integer.parseInt(userIdScoreList.get(score.getUserId()));
                userIdScoreList.replace(score.getUserId(), String.valueOf(newScore));
            }

        }
        List<String> list= new ArrayList<>(userIds);
        List<String> nameList=leaderBoardService.getUserNameList(list);
        System.out.println(nameList);

        List<LeaderBoardDto> leaderBoardDtos=new ArrayList<>();
        for(int i=0;i<userIds.size();i++)
        {
            LeaderBoardDto leaderBoardDto=new LeaderBoardDto(list.get(i),nameList.get(i),Integer.parseInt(userIdScoreList.get(list.get(i))));
            leaderBoardDtos.add(leaderBoardDto);
        }
        Collections.sort(leaderBoardDtos);
        System.out.println(leaderBoardDtos);
        LeaderBoardEntity leaderBoardEntity=new LeaderBoardEntity(id,name,leaderBoardDtos);
        leaderBoardDbService.save(leaderBoardEntity);
    }

    public void calculateDailyLeaderBoard(String id,String name)
    {
        List<Score> scoreList=scoreRepository.findAll();
        HashSet<String> userIds=new HashSet<>();
        List<LeaderBoardDto> leaderBoardDtoList=new ArrayList<>();
        HashMap<String,String> userIdScoreList=new HashMap<>();
        List<Score> filterScoreList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Score score : scoreList){
            Date timestamp = score.getTimeStamp();
            Instant instant = Instant.ofEpochMilli(timestamp.getTime());
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            LocalDate submissionTime = localDateTime.toLocalDate();
            if (submissionTime.equals(today)){
                filterScoreList.add(score);
            }
        }
        for(Score score:filterScoreList)
        {
            userIds.add(score.getUserId());
            if(userIdScoreList.get(score.getUserId())==null){
                userIdScoreList.put(score.getUserId(), String.valueOf(score.getPoints()));
            }
            else {
                int newScore=score.getPoints()+Integer.parseInt(userIdScoreList.get(score.getUserId()));
                userIdScoreList.replace(score.getUserId(), String.valueOf(newScore));
            }

        }
        List<String> list= new ArrayList<>(userIds);
        List<String> nameList=leaderBoardService.getUserNameList(list);
        System.out.println(nameList);

        List<LeaderBoardDto> leaderBoardDtos=new ArrayList<>();
        for(int i=0;i<userIds.size();i++)
        {
            LeaderBoardDto leaderBoardDto=new LeaderBoardDto(list.get(i),nameList.get(i),Integer.parseInt(userIdScoreList.get(list.get(i))));
            leaderBoardDtos.add(leaderBoardDto);
        }
        Collections.sort(leaderBoardDtos);
        System.out.println(leaderBoardDtos);
        LeaderBoardEntity leaderBoardEntity=new LeaderBoardEntity(id,name,leaderBoardDtos);
        leaderBoardDbService.save(leaderBoardEntity);
    }
    public void calculateContestLeaderBoard(String id,String name)
    {
        List<Score> scoreList=scoreRepository.findAll();
        HashSet<String> userIds=new HashSet<>();
        List<LeaderBoardDto> leaderBoardDtoList=new ArrayList<>();
        HashMap<String,String> userIdScoreList=new HashMap<>();
        List<Score> filterScoreList = new ArrayList<>();

        for(Score score:scoreList)
        {
            if(score.getContestId().equals(id))
                filterScoreList.add(score);
        }

        for(Score score:filterScoreList)
        {
            userIds.add(score.getUserId());
            if(userIdScoreList.get(score.getUserId())==null){
                userIdScoreList.put(score.getUserId(), String.valueOf(score.getPoints()));
            }
            else {
                int newScore=score.getPoints()+Integer.parseInt(userIdScoreList.get(score.getUserId()));
                userIdScoreList.replace(score.getUserId(), String.valueOf(newScore));
            }

        }
        List<String> list= new ArrayList<>(userIds);
        List<String> nameList=leaderBoardService.getUserNameList(list);
        System.out.println(nameList);

        List<LeaderBoardDto> leaderBoardDtos=new ArrayList<>();
        for(int i=0;i<userIds.size();i++)
        {
            LeaderBoardDto leaderBoardDto=new LeaderBoardDto(list.get(i),nameList.get(i),Integer.parseInt(userIdScoreList.get(list.get(i))));
            leaderBoardDtos.add(leaderBoardDto);
        }
        Collections.sort(leaderBoardDtos);
        System.out.println(leaderBoardDtos);
        LeaderBoardEntity leaderBoardEntity=new LeaderBoardEntity(id,name,leaderBoardDtos);
        leaderBoardDbService.save(leaderBoardEntity);
    }

//
//    public void calculateWeeklyLeaderBoard(String id,String name)
//    {
//        List<String> userIdList = leaderBoardService.getAllUserId();
//        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
//        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//            List<Score> scoreList = scoreService.getScoreByUserId(userIdList.get(i));
//            List<Score> filterScoreList = new ArrayList<>();
//            LocalDate today = LocalDate.now();
//            // Go backward to get Monday
//            LocalDate monday = today;
//            while (monday.getDayOfWeek() != DayOfWeek.MONDAY) {
//                monday = monday.minusDays(1);
//            }
//
//            // Go forward to get Sunday
//            LocalDate sunday = today;
//            while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
//                sunday = sunday.plusDays(1);
//            }
//
//            for (Score score : scoreList){
//                Date timestamp = score.getTimeStamp();
//                Instant instant = Instant.ofEpochMilli(timestamp.getTime());
//                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//                LocalDate submissionTime = localDateTime.toLocalDate();
//                if (((submissionTime.isBefore(sunday))||(submissionTime.equals(sunday))&&(submissionTime.isAfter(monday))||(submissionTime.equals(monday)))){
//                    filterScoreList.add(score);
//                }
//
//            }
//
//            int points = 0;
//            for (Score score : filterScoreList){
//                points+=score.getPoints();
//            }
//            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),points);
//            leaderBoardDtoList.add(leaderBoardDto);
//        }
//
//        for (int i = 0; i < leaderBoardDtoList.size(); i++){
//            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
//                if (leaderBoardDtoList.get(i).getScore()<leaderBoardDtoList.get(j).getScore()){
//                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
//                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
//                }
//            }
//        }
//        List<LeaderBoardDto> resultList = new ArrayList<>();
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//            resultList.add(leaderBoardDtoList.get(i));
//        }
//        LeaderBoardEntity l=new LeaderBoardEntity();
//        l.setId(id);
//        l.setLeaderBoardName(name);
//        l.setLeadersDetails(resultList);
//        leaderBoardDbService.save(l);
//    }
//
//    public void calculateDailyLeaderBoard(String id,String name)
//    {
//        List<String> userIdList = leaderBoardService.getAllUserId();
//        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
//        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//            List<Score> scoreList = scoreService.getScoreByUserId(userIdList.get(i));
//            List<Score> filterScoreList = new ArrayList<>();
//            LocalDate today = LocalDate.now();
//
//            for (Score score : scoreList){
//                Date timestamp = score.getTimeStamp();
//                Instant instant = Instant.ofEpochMilli(timestamp.getTime());
//                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//                LocalDate submissionTime = localDateTime.toLocalDate();
//                if (submissionTime.equals(today)){
//                    filterScoreList.add(score);
//                }
//            }
//
//            int points = 0;
//            for (Score score : filterScoreList){
//                points+=score.getPoints();
//            }
//            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),points);
//            leaderBoardDtoList.add(leaderBoardDto);
//        }
//
//        for (int i = 0; i < leaderBoardDtoList.size(); i++){
//            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
//                if (leaderBoardDtoList.get(i).getScore()<leaderBoardDtoList.get(j).getScore()){
//                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
//                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
//                }
//            }
//        }
//        List<LeaderBoardDto> resultList = new ArrayList<>();
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//            resultList.add(leaderBoardDtoList.get(i));
//        }
//        LeaderBoardEntity l=new LeaderBoardEntity();
//        l.setId(id);
//        l.setLeaderBoardName(name);
//        l.setLeadersDetails(resultList);
//        leaderBoardDbService.save(l);
//    }
//
//    public void calculateLeaderBoardPerContest(String id,String name)
//    {
//        List<Score> scoreList = leaderBoardService.getGlobalLeaderBoardPerContest(id);
//        List<ScoreDto> scoreDtoList = new ArrayList<>();
//        scoreList.forEach((score)->{
//            ScoreDto scoreDto = new ScoreDto();
//            BeanUtils.copyProperties(score,scoreDto);
//            scoreDtoList.add(scoreDto);
//        });
//        List<String> userIdList = leaderBoardService.getAllUserId();
//        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
//        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
//        List<ScoreDto> scoreDtos = new ArrayList<>();
//        int m = 0;
//        for (ScoreDto scoreDto: scoreDtoList){
//            if (scoreDtoList.get(m).getContestId().equals(id)){
//                for(int k = 0; k < min(userIdList.size(),scoreDtoList.size()); k++) {
//                    if (scoreDtoList.get(m).getUserId().equals(userIdList.get(k)))
//                        scoreDtos.add(scoreDto);
//                }
//            }
//        }
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//
//            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),scoreDtos.get(i).getPoints());
//            leaderBoardDtoList.add(leaderBoardDto);
//        }
//        for (int i = 0; i < leaderBoardDtoList.size(); i++){
//            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
//                if (leaderBoardDtoList.get(i).getScore()<leaderBoardDtoList.get(j).getScore()){
//                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
//                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
//                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
//                }
//            }
//        }
//        List<LeaderBoardDto> resultList = new ArrayList<>();
//        for (int i = 0; i < min(userIdList.size(),10); i++){
//            resultList.add(leaderBoardDtoList.get(i));
//        }
//        LeaderBoardEntity l=new LeaderBoardEntity();
//        l.setId(id);
//        l.setLeaderBoardName(name);
//        l.setLeadersDetails(resultList);
//        leaderBoardDbService.save(l);
//    }
}
