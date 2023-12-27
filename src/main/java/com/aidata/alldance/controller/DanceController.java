package com.aidata.alldance.controller;

import com.aidata.alldance.dto.DanceDto;
import com.aidata.alldance.dto.DanceFileDto;
import com.aidata.alldance.dto.ReplyDto;
import com.aidata.alldance.dto.SearchDto;
import com.aidata.alldance.service.DanceService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class DanceController {
    @Autowired
    private DanceService dServ;
    //트랜젝션 관련 객체 선언

    //댄스 게시판 이동 목록 처리 메소드
    @GetMapping("danceList")
    public ModelAndView danceList(SearchDto sdto, DanceDto dDto, HttpSession session){
        log.info("danceList()");
        ModelAndView mv = dServ.getDanceList(sdto,dDto,session);
        return mv;
    }
    //게시글 작성 이동 처리 메소드
    @GetMapping("danceForm")
    public String danceForm(){
        log.info("danceForm()");
        return "danceForm";
    }
    //게시글 작성 처리 메소드
    @PostMapping("danceProc")
    public String danceProc(@RequestPart List<MultipartFile> files,
                            @RequestPart List<MultipartFile> imgfiles,
                            DanceDto dance,
                            RedirectAttributes rttr,
                            HttpSession session){
        log.info("danceProc()");
        String view = dServ.danceJoin(files, imgfiles, dance, rttr, session);
        return view;
    }
    //글 상세보기
    @GetMapping("danceDetail")
    public ModelAndView danceDetail(DanceDto dDto, HttpSession session){
        log.info("danceDetail() : {}", dDto.getD_id());
        ModelAndView mv = dServ.getDance(dDto, session);
        return mv;
    }
    //파일 다운로드 처리
    @GetMapping("download")
    public ResponseEntity<Resource> fileDownload(DanceFileDto dfile,
                                                 HttpSession session)throws IOException {
        log.info("fileDownload()");
        ResponseEntity<Resource> resp = dServ.fileDownload(dfile, session);
        return resp;
    }
    //게시글 삭제
    @GetMapping("danceDelete")
    public String danceDelete(int d_num, RedirectAttributes rttr, HttpSession session){
        log.info("danceDelete()");
        String view = dServ.deleteDance(d_num, rttr, session);
        return view;
    }
    //게시글 수정 처리
    @GetMapping("correction")
    public ModelAndView correction(int d_num){
        log.info("correction()");
        ModelAndView mv = dServ.correctionBoard(d_num);
        return mv;
    }
    //게시글 수정 완료 후 이동할 메소드
    @PostMapping("correctionProc")
    public String correctionProc(List<MultipartFile> files,
                                 DanceDto dance,
                                 HttpSession session,
                                 RedirectAttributes rttr){
        log.info("correctionProc()");
        String view = dServ.updateDance(files, dance, session, rttr);
        return view;
    }

    //댓글 삭제 처리 메소드
    @GetMapping("replyDelete")
    public String replyDelete(int r_num, int d_num, String r_id,RedirectAttributes rttr){
        log.info("replyDelete()");

        String view = dServ.replyDelete(r_num, d_num, r_id, rttr);
        return view;
    }

    //댓글 수정 처리 메소드
    @GetMapping("updateReply")
    public ModelAndView updateReply(int r_num){
        log.info("updateReply()");
        ModelAndView mv = dServ.updateReplyBoard(r_num);
        return mv;
    }

    //댓글 수정 완료
    @PostMapping("updateReplyProc")
    public String updateReplyProc(ReplyDto reply, RedirectAttributes rttr){
        log.info("updateReplyProc()");
        String view = dServ.updateReply(reply,rttr);
        return view;
    }
}
