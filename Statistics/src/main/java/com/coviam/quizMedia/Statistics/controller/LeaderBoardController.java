package com.coviam.quizMedia.Statistics.controller;

import com.coviam.quizMedia.Statistics.dto.LeaderBoardDto;
import com.coviam.quizMedia.Statistics.dto.ScoreDto;
import com.coviam.quizMedia.Statistics.entity.LeaderBoardEntity;
import com.coviam.quizMedia.Statistics.entity.Score;
import com.coviam.quizMedia.Statistics.services.LeaderBoardService;
import com.coviam.quizMedia.Statistics.services.ScoreService;
import com.coviam.quizMedia.Statistics.services.impl.LeaderBoardDbService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping(value = "/leaderboard")
@CrossOrigin("*")
public class LeaderBoardController {

    @Autowired
    LeaderBoardService leaderBoardService;

    @Autowired
    LeaderBoardDbService leaderBoardDbService;

    @Autowired
    ScoreService scoreService;

    @RequestMapping(method = RequestMethod.GET,value = "/getTop3Leaders")
    public ResponseEntity<?> getTop3Leaders(){
        List<LeaderBoardEntity> leaderBoardEntities = leaderBoardDbService.findAll();
        List<LeaderBoardEntity> leaderBoardEntities1 = leaderBoardEntities.subList(3, leaderBoardEntities.size());
        List<LeaderBoardEntity> list=new ArrayList<>();
        List<Integer> range = IntStream.range(3, leaderBoardEntities.size()).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(range);
        int j=0;
        while(j<4){
            int i=range.get(j);
            List<LeaderBoardDto> l=leaderBoardEntities.get(i).getLeadersDetails().subList(0, Math.min(3,leaderBoardEntities.get(i).getLeadersDetails().size()));
            leaderBoardEntities.get(i).setLeadersDetails(l);
            list.add(leaderBoardEntities.get(i));
            j++;
        }
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getLeaderBoard/{contestId}")
    public ResponseEntity<?> getScoreByContestId(@PathVariable("contestId") String contestId){

        LeaderBoardEntity leaderBoardDtos=leaderBoardDbService.find(contestId);
        return new ResponseEntity<>(leaderBoardDtos,HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/getGlobalLeaderBoard")
    public ResponseEntity<?> getAllScores(){

        try {
            LeaderBoardEntity leaderBoardDtos = leaderBoardDbService.find("1");
            return new ResponseEntity<>(leaderBoardDtos, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<String>("{\"err\":\"error saving\"}", HttpStatus.OK);

        }

    }

    @RequestMapping(method = RequestMethod.GET, value = "/getWeeklyLeaderBoard")
    public ResponseEntity<?> getAllScoresWeekly(){

        try {
            LeaderBoardEntity leaderBoardEntity = leaderBoardDbService.find("2");
            return new ResponseEntity<LeaderBoardEntity>(leaderBoardEntity, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<String>("{\"err\":\"error saving\"}", HttpStatus.OK);

        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getDailyLeaderBoard")
    public ResponseEntity<?> getAllScoresDaily(){
        try {
            LeaderBoardEntity leaderBoardEntity = leaderBoardDbService.find("3");
            return new ResponseEntity<LeaderBoardEntity>(leaderBoardEntity, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<String>("{\"err\":\"error saving\"}", HttpStatus.OK);

        }

    }
}
