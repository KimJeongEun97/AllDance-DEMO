package com.aidata.alldance.dao;

import com.aidata.alldance.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberDao {
    //아이디 확인
    int selectId(String mid);
    //회원가입 정보를 DB에
    void insertMember(MemberDto member);
    //로그인 비밀번호 비교
    String selectPwd(String mid);
    //DTO에 있는 아이디 조회
    MemberDto selectMember(String mid);
}
