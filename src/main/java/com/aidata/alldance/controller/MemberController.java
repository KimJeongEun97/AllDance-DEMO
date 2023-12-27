package com.aidata.alldance.controller;

import com.aidata.alldance.dto.MemberDto;
import com.aidata.alldance.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class MemberController {
    @Autowired
    private MemberService mServ;

    //메인 페이지 이동
    @GetMapping("/")
    public String home(){
        log.info("home()");
        return "index";
    }
    //회원가입 페이지 이동
    @GetMapping("joinForm")
    public String joinForm(){
        log.info("joinForm()");
        return "joinForm";
    }
    //회원가입 처리
    @PostMapping("joinProc")
    public String joinProc(MemberDto member, RedirectAttributes rttr){
        log.info("joinProc()");
        String view = mServ.memberJoin(member, rttr);
        return view;
    }
    //로그인 이동
    @GetMapping("loginForm")
    public String loginForm(){
        log.info("loginForm()");
        return "loginForm";
    }
    //로그인 처리
    @PostMapping("loginProc")
    public String loginProc(MemberDto member, HttpSession session, RedirectAttributes rttr){
        log.info("loginProc()");
        String view = mServ.loginProc(member, session, rttr);
        return view;
    }
    //로그아웃 처리
    @GetMapping("logout")
    public String logout(HttpSession session){
        log.info("logout()");
        mServ.logout(session);
        return "redirect:/";
    }
}
