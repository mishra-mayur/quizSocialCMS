package com.coviam.quizMedia.Statistics.controller;

import com.coviam.quizMedia.Statistics.dto.LeaderBoardDto;
import com.coviam.quizMedia.Statistics.dto.ScoreDto;
import com.coviam.quizMedia.Statistics.entity.Score;
import com.coviam.quizMedia.Statistics.services.LeaderBoardService;
import com.coviam.quizMedia.Statistics.services.ScoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping(value = "/leaderboard")
@CrossOrigin("*")
public class LeaderBoardController {

    @Autowired
    LeaderBoardService leaderBoardService;

    @Autowired
    ScoreService scoreService;

    @RequestMapping(method = RequestMethod.GET, value = "/getLeaderBoard/{contestId}")
    public ResponseEntity<?> getScoreByContestId(@PathVariable("contestId") String contestId){

        List<Score> scoreList = leaderBoardService.getGlobalLeaderBoardPerContest(contestId);
        List<ScoreDto> scoreDtoList = new ArrayList<>();
        scoreList.forEach((score)->{
            ScoreDto scoreDto = new ScoreDto();
            BeanUtils.copyProperties(score,scoreDto);
            scoreDtoList.add(scoreDto);
        });
        List<String> userIdList = leaderBoardService.getAllUserId();
        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){

            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),scoreList.get(i).getPoints());
            leaderBoardDtoList.add(leaderBoardDto);
        }
        for (int i = 0; i < leaderBoardDtoList.size(); i++){
            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
                if (leaderBoardDtoList.get(i).getScore()>leaderBoardDtoList.get(j).getScore()){
                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
                }
            }
        }
        List<LeaderBoardDto> resultList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){
            resultList.add(leaderBoardDtoList.get(i));
        }
        return new ResponseEntity<List<LeaderBoardDto>>(resultList,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getGlobalLeaderBoard")
    public ResponseEntity<?> getAllScores(){

        List<String> userIdList = leaderBoardService.getAllUserId();
        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){
            List<Score> scoreList = scoreService.getScoreByUserId(userIdList.get(i));
            int points = 0;
            for (Score score : scoreList){
                points+=score.getPoints();
            }
            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),points);
            leaderBoardDtoList.add(leaderBoardDto);
        }

        for (int i = 0; i < leaderBoardDtoList.size(); i++){
            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
                if (leaderBoardDtoList.get(i).getScore()>leaderBoardDtoList.get(j).getScore()){
                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
                }
            }
        }
        List<LeaderBoardDto> resultList = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            resultList.add(leaderBoardDtoList.get(i));
        }
        return new ResponseEntity<List<LeaderBoardDto>>(resultList,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWeeklyLeaderBoard")
    public ResponseEntity<?> getAllScoresWeekly(){

        List<String> userIdList = leaderBoardService.getAllUserId();
        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){
            List<Score> scoreList = scoreService.getScoreByUserId(userIdList.get(i));
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

            int points = 0;
            for (Score score : filterScoreList){
                points+=score.getPoints();
            }
            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),points);
            leaderBoardDtoList.add(leaderBoardDto);
        }

        for (int i = 0; i < leaderBoardDtoList.size(); i++){
            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
                if (leaderBoardDtoList.get(i).getScore()>leaderBoardDtoList.get(j).getScore()){
                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
                }
            }
        }
        List<LeaderBoardDto> resultList = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            resultList.add(leaderBoardDtoList.get(i));
        }
        return new ResponseEntity<List<LeaderBoardDto>>(resultList,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDailyLeaderBoard")
    public ResponseEntity<?> getAllScoresDaily(){

        List<String> userIdList = leaderBoardService.getAllUserId();
        List<String> userNameList = leaderBoardService.getUserNameList(userIdList);
        List<LeaderBoardDto> leaderBoardDtoList = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){
            List<Score> scoreList = scoreService.getScoreByUserId(userIdList.get(i));
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

            int points = 0;
            for (Score score : filterScoreList){
                points+=score.getPoints();
            }
            LeaderBoardDto leaderBoardDto = new LeaderBoardDto(userIdList.get(i),userNameList.get(i),points);
            leaderBoardDtoList.add(leaderBoardDto);
        }

        for (int i = 0; i < leaderBoardDtoList.size(); i++){
            for (int j = i+1; j < leaderBoardDtoList.size(); j++ ){
                if (leaderBoardDtoList.get(i).getScore()>leaderBoardDtoList.get(j).getScore()){
                    LeaderBoardDto leaderBoardDto = new LeaderBoardDto();
                    BeanUtils.copyProperties(leaderBoardDtoList.get(i),leaderBoardDto);
                    BeanUtils.copyProperties(leaderBoardDtoList.get(j),leaderBoardDtoList.get(i));
                    BeanUtils.copyProperties(leaderBoardDto,leaderBoardDtoList.get(j));
                }
            }
        }
        List<LeaderBoardDto> resultList = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            resultList.add(leaderBoardDtoList.get(i));
        }
        return new ResponseEntity<List<LeaderBoardDto>>(resultList,HttpStatus.OK);
    }
}
