package com.aidata.alldance.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class SessionIntercepter implements AsyncHandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        log.info("preHandle()");
        //세션에 로그인 정보가 있는지 확인
        HttpSession session = request.getSession();
        if (session.getAttribute("member")==null){
            //로그인을 안한상태
            log.info("인터셉트! 로그인 안함");
            //로그인 정보가 있으면 요청 페이지로, 없으면 첫페이지로 이동.
            response.sendRedirect("/");
            return false;
        }
        return true;
    }

    //로그아웃 후 처리
    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle()");
        if (request.getProtocol().equals("HTTP/1.1")){
            //1.1버전 캐시 제거
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        }else {
            response.setHeader("Pragma","no-cache");
        }
    }
}
