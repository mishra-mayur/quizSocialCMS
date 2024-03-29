package com.coviam.quizSocialCMS.CMS.controller;

import com.coviam.quizSocialCMS.CMS.entity.ScreenedDataEntityClass;
import com.coviam.quizSocialCMS.CMS.entity.StaticContestEntityClass;
import com.coviam.quizSocialCMS.CMS.entityDto.ActiveContestDto;
import com.coviam.quizSocialCMS.CMS.entityDto.RandomQuizDto;
import com.coviam.quizSocialCMS.CMS.entityDto.StaticContestDto;
import com.coviam.quizSocialCMS.CMS.repository.StaticContestRepository;
import com.coviam.quizSocialCMS.CMS.service.StaticContestInterface;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;

@RestController
@RequestMapping(value = "/cms")
@CrossOrigin(origins = "*")
public class CmsController {

    @Autowired
    StaticContestInterface staticContestInterface;

    @RequestMapping(method = RequestMethod.GET,value = "/getAllContests")
    public ResponseEntity<?> getAllContest()
    {
        List<StaticContestEntityClass> staticContestEntityClasses=staticContestInterface.findAll();
        if(staticContestEntityClasses==null)
        {
            return new ResponseEntity<>(new ArrayList<>(),HttpStatus.OK);
        }
        return new ResponseEntity<>(staticContestEntityClasses,HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.POST,value = "/saveContest")
    public ResponseEntity<?> saveContest(@RequestBody StaticContestDto staticContestDto)
    {
        System.out.println("Save Contest Called "+new Date());

        StaticContestEntityClass staticContestEntityClass=new StaticContestEntityClass();
        BeanUtils.copyProperties(staticContestDto,staticContestEntityClass);
        try {
            staticContestInterface.saveContest(staticContestEntityClass);
            return new ResponseEntity<>("{\"msg\":\"saved\"}", HttpStatus.OK);

        }
        catch (Exception e)
        {
            return new ResponseEntity<>("{\"err\":\"error saving contest\"}", HttpStatus.OK);

        }

    }

    @RequestMapping(method = RequestMethod.GET,value = "/getContestById/{id}")
    public ResponseEntity<?> getContestById(@PathVariable("id") String id)
    {
        System.out.println("Get Contest By Id  Called "+new Date());

        try{
            StaticContestEntityClass staticContestEntityClass= staticContestInterface.getContestById(id);
            if(staticContestEntityClass==null)
            {
                return new ResponseEntity<String>("{\"err\":\"contest ended\"}", HttpStatus.OK);
            }
            return new ResponseEntity<StaticContestEntityClass>(staticContestEntityClass, HttpStatus.OK);

        }catch (Exception e) {
            return new ResponseEntity<String>("{\"err\":\"contest ended\"}", HttpStatus.OK);

        }
    }


    @RequestMapping(method = RequestMethod.GET,value = "/getActiveContests")
    public ResponseEntity<List<ActiveContestDto>> getActiveContest()
    {
        System.out.println("Get Active Contest Called "+new Date());

        try {
            List<StaticContestEntityClass> staticContestEntityClasses = staticContestInterface.getActiveContest();
            List<ActiveContestDto> activeContestDtos = new ArrayList<>();
            for (StaticContestEntityClass staticContestEntityClass : staticContestEntityClasses) {
                ActiveContestDto activeContestDto = new ActiveContestDto();
                if(staticContestEntityClass.isActive())
                {
                    BeanUtils.copyProperties(staticContestEntityClass, activeContestDto);
                    activeContestDtos.add(activeContestDto);
                }
            }
            return new ResponseEntity<List<ActiveContestDto>>(activeContestDtos, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<List<ActiveContestDto>>(new ArrayList<>(), HttpStatus.OK);

        }
    }

    @RequestMapping(method = RequestMethod.GET,value = "/getRandomQuestion")
    public ResponseEntity<RandomQuizDto> getRandomQuestion()
    {
        System.out.println("Get Random Question Called "+new Date());

        RandomQuizDto randomQuizDto=new RandomQuizDto();
        try {
            List<StaticContestEntityClass> staticContestEntityClasses = staticContestInterface.getActiveContest();
            if (staticContestEntityClasses.size() == 0) {
                return new ResponseEntity<>(randomQuizDto, HttpStatus.OK);
            }
            Random random = new Random();
            int maxLoop = 10, i;
            StaticContestEntityClass staticContestEntityClass = new StaticContestEntityClass();
            while (maxLoop-- > 0) {
                i = random.nextInt(staticContestEntityClasses.size() - 1);
                staticContestEntityClass = staticContestEntityClasses.get(i);
                if (staticContestEntityClass.isActive()) {
                    break;
                }
            }
            if(maxLoop<=0)
                return new ResponseEntity<RandomQuizDto>(randomQuizDto,HttpStatus.OK);

            maxLoop = 10;
            while (maxLoop-- > 0) {
                i = Math.abs(random.nextInt() % (staticContestEntityClass.getQuestionId().size() - 1));
                if(staticContestEntityClass.getQuestionId().get(i).getQuestionType().equals("Text"))
                {
                    BeanUtils.copyProperties(staticContestEntityClass, randomQuizDto);
                    randomQuizDto.setQuestionId(staticContestEntityClass.getQuestionId().get(i));
                    return new ResponseEntity<RandomQuizDto>(randomQuizDto, HttpStatus.OK);
                }
            }
            if(maxLoop<=0)
                return new ResponseEntity<RandomQuizDto>(randomQuizDto,HttpStatus.OK);

            return new ResponseEntity<RandomQuizDto>(randomQuizDto, HttpStatus.OK);
        }
        catch (Exception e)

        {
            return new ResponseEntity<RandomQuizDto>(randomQuizDto,HttpStatus.OK);

        }

    }

    @Autowired
    StaticContestRepository repository;

    @RequestMapping(method = RequestMethod.GET,value = "/getContestByCategory/{category}")
    public ResponseEntity<List<ActiveContestDto>> getContestByCategory(@PathVariable("category") String category)
    {
        System.out.println("Get Contest By category Called "+new Date());

        try {
            List<StaticContestEntityClass> staticContestEntityClasses = staticContestInterface.getContestByCategory(category);
            List<StaticContestEntityClass> list = Lists.newArrayList(staticContestEntityClasses);
            List<ActiveContestDto> finalList = new ArrayList<>();
            for (StaticContestEntityClass staticContestEntityClass : list) {
                ActiveContestDto activeContestDto = new ActiveContestDto();
                BeanUtils.copyProperties(staticContestEntityClass, activeContestDto);
                finalList.add(activeContestDto);
            }
            return new ResponseEntity<List<ActiveContestDto>>(finalList, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<List<ActiveContestDto>>(new ArrayList<>(), HttpStatus.OK);

        }
    }


    @RequestMapping(method = RequestMethod.GET,value = "/getContestByContestName/{name}")
    public ResponseEntity<List<ActiveContestDto>> getContestByContestName(@PathVariable("name") String name)
    {
        System.out.println("Get Contest By Contest Name Called "+new Date());

        List<StaticContestEntityClass> staticContestEntityClasses= staticContestInterface.getContestByContestName(name) ;
        try{
            List<StaticContestEntityClass> list=Lists.newArrayList(staticContestEntityClasses);

        List<ActiveContestDto> finalList=new ArrayList<>();
        for(StaticContestEntityClass staticContestEntityClass:list)
        {
            ActiveContestDto activeContestDto=new ActiveContestDto();
            BeanUtils.copyProperties(staticContestEntityClass,activeContestDto);
            finalList.add(activeContestDto);
        }
        return new ResponseEntity<List<ActiveContestDto>>(finalList,HttpStatus.OK);}
        catch (Exception e)
        {
            return new ResponseEntity<List<ActiveContestDto>>(new ArrayList<>(),HttpStatus.OK);
        }

    }


    @RequestMapping(method = RequestMethod.GET,value = "/getContestQuestions/{contestId}")
    public ResponseEntity<List<ScreenedDataEntityClass>> getContestQuestion(@PathVariable("contestId")String contestId)
    {
        System.out.println("Get Contest Question Called "+new Date());

        try {
            List<ScreenedDataEntityClass> list = staticContestInterface.getContestById(contestId).getQuestionId();

            return new ResponseEntity<List<ScreenedDataEntityClass>>(list, HttpStatus.OK);
        }
        catch (Exception e)
        {
            return new ResponseEntity<List<ScreenedDataEntityClass>>(new ArrayList<>(), HttpStatus.OK);

        }
    }




}

