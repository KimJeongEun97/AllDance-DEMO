package com.aidata.alldance.controller;

import com.aidata.alldance.dto.DanceDto;
import com.aidata.alldance.dto.DanceFileDto;
import com.aidata.alldance.dto.ReplyDto;
import com.aidata.alldance.service.DanceService;
import com.aidata.alldance.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class DanceRestController {
    @Autowired
    private MemberService mServ;
    @Autowired
    private DanceService dServ;

    //아이디 체크
    @GetMapping("idcheck")
    public String idcheck(String mid){
        log.info("idcheck()");
        String res = mServ.idcheck(mid);
        return res;
    }

    //게시글 수정시 파일 삭제
    @PostMapping("delFile")
    public List<DanceFileDto> delFile(DanceFileDto dFile, HttpSession session){
        log.info("delFile()");
        List<DanceFileDto> fList = dServ.delFile(dFile, session);
        return fList;
    }

    //댓글 작성 처리 메소드
    @PostMapping("replyInsert")
    public ReplyDto replyInsert(ReplyDto reply, DanceDto dDto){
        log.info("replyInsert()");
        reply = dServ.replyInsert(reply, dDto);
        return reply;
    }
}
