package com.aidata.alldance.service;

import com.aidata.alldance.dao.DanceDao;
import com.aidata.alldance.dao.MemberDao;
import com.aidata.alldance.dto.*;
import com.aidata.alldance.util.PagingUtil;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@Service
@Slf4j
public class DanceService {
    @Autowired
    private DanceDao dDao;
    @Autowired
    private MemberDao mDao;

    @Autowired
    private PlatformTransactionManager manager;
    @Autowired
    private TransactionDefinition definition;

    private int lcnt = 5;//목록이 보여질 개수

    //댄스 게시판 목록 보이는 기능 처리
    public ModelAndView getDanceList(SearchDto sdto,DanceDto dDto,HttpSession session) {
        log.info("getDanceList()");
        ModelAndView mv = new ModelAndView(); //객체 생성
        //세션에 member 속성이 없으면 로그인 폼으로 리다이렉트
        if (session.getAttribute("member") == null) {
            mv.setViewName("redirect:loginForm"); //로그인 폼으로 리다이렉트
            return mv; //메소드 종료
        }
        int num = sdto.getPageNum(); //페이지 번호를 가져옴
        if (num == 0) num = 1; //페이지 번호가 0인 경우 1로 설정
        if (sdto.getListCnt() == 0) {
            sdto.setListCnt(lcnt); //검색 조건으로 주어진 목록 개수가 0이면 lcnt로 설정
        }
        sdto.setPageNum((num - 1) * sdto.getListCnt()); //시작하는 항목 인덱스 설정
        //검색 조건에 따라 게시물 목록을 데이터베이스에서 가져옴
        List<DanceDto> dList = dDao.selectDanceList(sdto);
        mv.addObject("dList", dList);

        sdto.setPageNum(num); //페이지 번호를 원래 값으로 복원
        String pageHtml = getPaging(sdto); //페이지 번호에 따른 페이징 HTML을 생성
        mv.addObject("paging", pageHtml); //생성한 페이징 HTML을 ModelAndView 객체에 추가

        //페이지 번호와 검색 관련 내용을 세션에 저장
        if (sdto.getColname() != null) {
            session.setAttribute("sdto", sdto);
        } else {// 검색 조건이 없는 경우 세션에서 제거
            session.removeAttribute("sdto");
        }

        // 현재 페이지 번호를 세션에 저장
        session.setAttribute("pageNum", num);

        mv.setViewName("DanceList");// 뷰 이름을 "boardList"로 설정
        return mv; // ModelAndView 객체 반환
    }

    //페이징 기능 처리
    private String getPaging(SearchDto sdto) {
        String pageHtml = null; // 페이징 HTML 문자열을 초기화
        int maxNum = dDao.selectDanceCnt(sdto); // 전체 게시물 수를 데이터베이스에서 가져옴
        int pageCnt = 5; // 페이징에 표시할 페이지 번호 개수를 설정
        String listName = "danceList?"; // 페이징 링크의 기본 URL
        if (sdto.getColname() != null) { // 검색 조건이 있는 경우 URL에 검색 조건을 추가
            listName += "colname=" + sdto.getColname() + "&keyword=" + sdto.getKeyword() + "&";
        }

        PagingUtil paging = new PagingUtil(  // PagingUtil 객체를 생성하고 필요한 정보를 전달하여 페이징을 계산
                maxNum,          // 전체 게시물 수
                sdto.getPageNum(), // 현재 페이지 번호
                sdto.getListCnt(), // 한 페이지에 표시할 게시물 수
                pageCnt,            // 페이징에 표시할 페이지 번호 개수
                listName);           // 페이징 링크의 기본 URL

        pageHtml = paging.makePaging(); // 생성한 페이징 HTML을 가져와 pageHtml에 저장

        return pageHtml;
    }

    //게시글 작성시 기능 처리
    public String danceJoin(List<MultipartFile> files,
                            List<MultipartFile> imgfiles,
                            DanceDto dance,
                            RedirectAttributes rttr,
                            HttpSession session) {
        log.info("danceJoin()");

        TransactionStatus status = manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            log.info("게시글 번호 : " + dance.getD_num());
            //글 내용 저장
            dDao.insertDance(dance);
            log.info("게시글 번호 : " + dance.getD_num());

            //영상 파일 저장(파일 정보 저장)
            fileUpload(files, session, dance.getD_num());
            //섬네일 파일 저장
            imgfileUpload(imgfiles, session, dance.getD_num());

            manager.commit(status); //최종승인
            msg = "공유 감사합니다!";
            view = "redirect:danceList";
        }catch (Exception e){
            e.printStackTrace();
            manager.rollback(status); //취소
            msg = "작성 실패";
            view = "redirect:danceForm";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //게시글 작성시 섬네일 이미지 업로드 기능
    private void imgfileUpload(List<MultipartFile> imgfile,
                               HttpSession session,
                               int d_num) throws IOException {
        log.info("imgfileUpload()");

        String realPath = session.getServletContext().getRealPath("/");
        log.info(realPath);
        realPath += "imgupload/";

        File folder = new File(realPath);
        if (folder.isDirectory() == false){
            folder.mkdir();
        }
        for (MultipartFile mf : imgfile){
            String oriname = mf.getOriginalFilename();
            if (oriname.equals("")){
                return;
            }

            DanceImgFileDto dfd = new DanceImgFileDto();
            dfd.setIf_dnum(d_num);
            dfd.setIf_oriname(oriname);
            String sysname = System.currentTimeMillis() + oriname.substring(oriname.lastIndexOf("."));
            dfd.setIf_sysname(sysname);

            File file = new File(realPath + sysname);
            mf.transferTo(file);

            dDao.insertImageFile(dfd);
        }
    }

    //게시글 작성시 파일 업로드 기능
    private void fileUpload(List<MultipartFile> files,
                            HttpSession session,
                            int d_num)throws Exception {
        log.info("fileUpload()");

        String realPath = session.getServletContext().getRealPath("/");
        log.info(realPath);
        realPath += "upload/";

        File folder = new File(realPath);
        if (folder.isDirectory() == false){
            folder.mkdir();
        }
        for (MultipartFile mf : files){
            String oriname = mf.getOriginalFilename();
            if (oriname.equals("")){
                return;
            }

            DanceFileDto dfd = new DanceFileDto();
            dfd.setDf_dnum(d_num);
            dfd.setDf_oriname(oriname);
            String sysname = System.currentTimeMillis() + oriname.substring(oriname.lastIndexOf("."));
            dfd.setDf_sysname(sysname);

            File file = new File(realPath + sysname);
            mf.transferTo(file);

            dDao.insertFile(dfd);
        }
    }
    //게시글을 누르면 상세페이지로
    public ModelAndView getDance(DanceDto dDto, HttpSession session){
        log.info("getDance()");
        ModelAndView mv = new ModelAndView();

        MemberDto member = (MemberDto) session.getAttribute("member");

        int views = dDto.getD_views();
        if (member.getM_id().equals(dDto.getD_id())) {
            views+=0;
            dDto.setD_views(views);
            dDao.updateViewPoint(dDto);
        }else {
            views++;
            dDto.setD_views(views);
            dDao.updateViewPoint(dDto);
        }

        //조회수가 증가하게 세션에 저장
        dDto = dDao.selectDance(dDto.getD_num());
        session.setAttribute("dance", dDto);

        //게시글 번호로 선택한 게시글 가져오기
        DanceDto dance = dDao.selectDance(dDto.getD_num());//dance객체
        mv.addObject("dance", dance);

        //게시글 파일목록 가져오기
        List<DanceFileDto> dfList = dDao.selectFileList(dDto.getD_num());
        mv.addObject("dfList", dfList);

        //게시글의 댓글목록 가져오기
        List<ReplyDto> rList = dDao.selectReplyList(dance.getD_num());
        mv.addObject("rList", rList);

        mv.setViewName("danceDetail");//html로 이동

        return mv;
    }
    //파일 다운로드 기능 처리
    public ResponseEntity<Resource> fileDownload(DanceFileDto dfile,
                                                 HttpSession session) throws IOException {
        log.info("fileDownload()");
        //파일 저장 경로 및 경로와 파일명을 조합
        String realpath = session.getServletContext().getRealPath("/");
        realpath += "upload/" + dfile.getDf_sysname();

        //실제 파일이 저장된 하드디스크까지의 경로를 수립. //자원을 가져오기 위한 통로 객체
        InputStreamResource fResource = new InputStreamResource(new FileInputStream(realpath));

        //파일명이 한글인 경우의 처리(UTF-8로 인코딩)
        String fileName = URLEncoder.encode(dfile.getDf_oriname(), "UTF-8");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename="+fileName)
                .body(fResource);
    }
    //댄스 게시글 삭제 기능 처리
    public String deleteDance(int d_num, RedirectAttributes rttr, HttpSession session) {
        log.info("deleteDance()");
        String view = null;
        String msg = null;

        TransactionStatus status = manager.getTransaction(definition);

        try {
            //파일 삭제 목록 구하기
            List<String> fList = dDao.selectFnameList(d_num);
            List<String> ifList = dDao.selectImgFnameList(d_num);

            //섬네일 삭제
            dDao.deleteThum(d_num);
            //영상파일 삭제
            dDao.deleteFiles(d_num);
            //댓글 목록 삭제
            dDao.deleteReplyList(d_num);
            //게시글 삭제
            dDao.deleteDance(d_num);
            //게시글 안에있는 파일 삭제 처리
            deleteFiles(fList,session);
            if (fList.size() != 0){
                deleteFiles(fList, session);
            }
            //게시글 섬네일 파일 삭제 처리
            deleteImgFiles(ifList,session);
            if (ifList.size() != 0){
                deleteFiles(ifList, session);
            }

            manager.commit(status);
            view = "redirect:danceList?pageNum=1";
            msg = "삭제 성공";
        }catch (Exception e){
            e.printStackTrace();
            manager.rollback(status);
            view = "redirect:danceDetail?d_num=" + d_num;
            msg = "삭제 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }
    //영상폴더 안에 저장된 파일 삭제 처리
    private void deleteFiles(List<String> fList, HttpSession session) {
        log.info("deleteFiles()");
        //파일위치
        String realPath = session.getServletContext().getRealPath("/");
        realPath += "upload/";

        for (String sn : fList){
            File file = new File(realPath + sn);
            if (file.exists() == true){//파일 유무 확인
                file.delete();//파일을 삭제
            }
        }
    }
    //섬네일폴더 안에 저장된 파일 삭제 처리
    private void deleteImgFiles(List<String> ifList, HttpSession session) {
        log.info("deleteImgFiles()");
        //파일위치
        String realPath = session.getServletContext().getRealPath("/");
        realPath += "imgupload/";

        for (String sn : ifList){
            File file = new File(realPath + sn);
            if (file.exists() == true){//파일 유무 확인
                file.delete();//파일을 삭제
            }
        }
    }
    //수정 페이지 기능 처리
    public ModelAndView correctionBoard(int d_num) {
        log.info("correction");
        ModelAndView mv = new ModelAndView();
        //게시글 내용 가져오기
        DanceDto dance = dDao.selectDance(d_num);
        //파일목록 가져오기
        List<DanceFileDto> fList = dDao.selectFileList(d_num);
        //mv에 담기
        mv.addObject("dance", dance);
        mv.addObject("fList", fList);
        mv.setViewName("correction");
        return mv;
    }
    //수정시 파일 삭제
    public List<DanceFileDto> delFile(DanceFileDto dFile, HttpSession session) {
        log.info("delFile()");
        List<DanceFileDto> fList = null;

        //파일 경로 설정
        String realPath = session.getServletContext().getRealPath("/");
        realPath += "upload/"+dFile.getDf_sysname();

        try {
            //파일 삭제
            File file = new File(realPath);
            if (file.exists()){
                if (file.delete()){
                    //해당 파일 정보 삭제(DB)
                    dDao.deleteFile(dFile.getDf_sysname());
                    //나머지 파일 목록 다시 가져오기
                    fList = dDao.selectFileList(dFile.getDf_dnum());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return fList;
    }
    //게시글 수정 후 처리
    public String updateDance(List<MultipartFile> files, DanceDto dance,
                              HttpSession session, RedirectAttributes rttr) {

        TransactionStatus status = manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try {
            dDao.correction(dance);
            fileUpload(files, session, dance.getD_num());

            manager.commit(status);
            view = "redirect:danceDetail?d_num="+dance.getD_num();
            msg = "수정 성공";
        }catch (Exception e){
            e.printStackTrace();
            manager.rollback(status);
            view = "redirect:correction?d_num="+dance.getD_num();
            msg = "수정 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //댓글 작성 기능 처리
    public ReplyDto replyInsert(ReplyDto reply, DanceDto dDto) {
        log.info("replyInsert()");
        String view = null;
        try {
            dDao.insertReply(reply);
            reply = dDao.selectLastReply(reply.getR_num());
            view = "redirect:danceDetail?d_num=" + dDto.getD_num();
        }catch (Exception e){
            e.printStackTrace();
            reply = null;
        }
        return reply;
    }

    //댓글 1개씩 삭제 기능 처리
    public String replyDelete(int r_num, int d_num, String r_id, RedirectAttributes rttr){
        log.info("replyDelete()");
        String view = null;
        String msg = null;

        try {
            dDao.deleteReply(r_num);
            msg = "삭제성공";
        }catch(Exception e){
            e.printStackTrace();
            msg = "삭제실패";
        }
        view = "redirect:danceDetail?d_num="+d_num;
        rttr.addFlashAttribute("msg", msg);
        return view;
    }

    //댓글 수정
    public ModelAndView updateReplyBoard(int r_num) {
        log.info("updateReplyBoard()");
        ModelAndView mv = new ModelAndView();
        //댓글 내용 가져오기
        ReplyDto reply = dDao.selectLastReply(r_num);
        mv.addObject("reply", reply);
        mv.setViewName("updateReply");
        return mv;
    }

    //댓글 수정 완료처리
    public String updateReply(ReplyDto reply, RedirectAttributes rttr){
        log.info("updateReply");

        TransactionStatus status = manager.getTransaction(definition);

        String view = null;
        String msg = null;

        try{
            dDao.updateReplyForm(reply);

            manager.commit(status);
            view = "redirect:danceDetail?d_num=" + reply.getR_dnum();
            msg = "수정 완료";
        }catch (Exception e){
            e.printStackTrace();
            manager.rollback(status);
            view = "redirect:updateReplyForm?r_num=" + reply.getR_num();
            msg = "수정 실패";
        }
        rttr.addFlashAttribute("msg", msg);
        return view;
    }
}
















