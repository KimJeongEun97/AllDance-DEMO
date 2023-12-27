package com.aidata.alldance.service;

import com.aidata.alldance.dao.MemberDao;
import com.aidata.alldance.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
@Slf4j
public class MemberService {
    @Autowired
    private MemberDao mDao;
    //비밀번호 인코더
    private BCryptPasswordEncoder pEncoder = new BCryptPasswordEncoder();

    //아이디 확인 기능 처리
    public String idcheck(String mid) {
        log.info("idcheck()");
        String result = "";
        int mcnt = mDao.selectId(mid);
        if (mcnt == 0) {
            result = "ok";
        } else {
            result = "fail";
        }
        return result;
    }

    //회원가입 기능 처리
    public String memberJoin(MemberDto member, RedirectAttributes rttr){
        log.info("memberJoin()");
        String view = null;
        String msg = null;

        //비밀번호 암호화
        String encpwd = pEncoder.encode(member.getM_pwd());
        log.info(encpwd);
        member.setM_pwd(encpwd);//암호화문구로 덮기

        try {
            mDao.insertMember(member);
            msg = "가입에 성공했습니다.";
            view = "redirect:/";
        }catch (Exception e){
            e.printStackTrace();
            msg = "가입 실패";
            view = "redirect:joinForm";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //로그인 기능 처리
    public String loginProc(MemberDto member, HttpSession session, RedirectAttributes rttr) {
        log.info("loginProc()");
        String view = null;
        String msg = null;
        String encPwd = mDao.selectPwd(member.getM_id());
        if (encPwd != null){
            if (pEncoder.matches(member.getM_pwd(), encPwd)){
                member = mDao.selectMember(member.getM_id());
                session.setAttribute("member",member);
                view = "redirect:/";
            }else {
                view = "redirect:loginForm";
                msg = "비밀번호가 틀립니다.";
            }
        }else {
            view = "redirect:joinForm";
            msg = "아이디가 존재하지 않습니다.";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    public void logout(HttpSession session){
        log.info("logout()");
        session.invalidate();
    }
}
