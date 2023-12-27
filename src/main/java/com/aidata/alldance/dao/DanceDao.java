package com.aidata.alldance.dao;

import com.aidata.alldance.dto.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DanceDao {
    //댄스 게시글 조회
    List<DanceDto> selectDanceList(SearchDto sdto);

    //댄스 게시글이 몇개인지 조회
    int selectDanceCnt(SearchDto sdto);

    //댄스 게시글 작성
    void insertDance(DanceDto dance);

    //댄스 게시글 파일
    void insertFile(DanceFileDto dFile);
    //섬네일 파일
    void insertImageFile(DanceImgFileDto imgFile);

    //댄스 게시글 하나 가져오기
    DanceDto selectDance(int d_num);

    //글 번호에 해당하는 파일 목록을 가져오는 메소드
    List<DanceFileDto> selectFileList(int d_num);

    //게시글 삭제
    void deleteDance(int d_num);

    //게시글 번호에 해당하는 영상파일목록 삭제
    void deleteFiles(int d_num);
    //게시글 번호에 해당하는 섬네일 삭제
    void deleteThum(int d_num);

    //게시글 번호에 해당하는 댓글 목록 삭제
    void deleteReplyList(int d_num);

    //파일의 저장 이름 목록 구하는 메소드
    List<String> selectFnameList(int dNum);
    //섬네일 파일의 저장 이름 목록 구하는 메소드
    List<String> selectImgFnameList(int dNum);

    //게시글 수정시 파일삭제
    void deleteFile(String sysname);

    //게시글 수정 처리
    void correction(DanceDto dance);

    //다른 사용자가 게시글 조회시 view 1씩 증가
    void updateViewPoint(DanceDto dDto);

    //게시글 번호에 해당하는 댓글목록을 가져오는 메소드
    List<ReplyDto> selectReplyList(int d_num);

    //댓글 저장
    void insertReply(ReplyDto reply);
    //댓글 한개 삭제
    void deleteReply(int r_num);

    //저장 시 생성된 댓글 번호로 댓글 정보 가져오는 메소드
    ReplyDto selectLastReply(int r_dnum);

    //댓글 수정 처리
    void updateReplyForm(ReplyDto reply);

}
