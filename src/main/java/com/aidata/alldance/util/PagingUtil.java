package com.aidata.alldance.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PagingUtil {
    private int maxNum; //전체 게시글 개수
    private int pageNum; //현재 보이는 페이지의 번호
    private int listCnt; //페이지 당 보여질 게시글 개수
    private int pageCnt; //보여질 페이지 번호의 개수
    private String listName; //게시판이 여러개일 때 페이징을 처리해야하는 목록 이름 저장

    //페이징용 url과 링크로 구성된 html 태그 문장을 작성하는 메소드
    public String makePaging() {
        String page = null;
        StringBuffer sb = new StringBuffer();
        //1. 전체 페이지 개수 구하기
        int totalPage = (maxNum % listCnt) > 0 ?
                maxNum / listCnt + 1 :
                maxNum / listCnt;
        if (totalPage == 0) totalPage = 1;
        //2. 현재 페이지가 속해 있는 그룹 번호 구하기
        int curGroup = (pageNum % pageCnt) > 0 ?
                pageNum / pageCnt + 1 :
                pageNum / pageCnt;
        //3. 현재 보이는 페이지 그룹의 시작 번호 구하기
        int start = (curGroup * pageCnt) - (pageCnt - 1);
        //4. 현재 보이는 페이지 그룹의 마지막 번호 구하기
        int end = (curGroup * pageCnt) >= totalPage ? totalPage : curGroup * pageCnt;
        //이전 버튼 처리. 시작번호가 1일때는 생성X
        if (start != 1) {
            sb.append("<a class='pno' href='/" + listName + "pageNum=" + (start - 1) + "'>◀</a>");
        }
        //중간 페이지 번호 처리
        for (int i = start; i <= end; i++) {
            if (pageNum != i) {//현재 페이지가 아닌 번호
                sb.append("<a class='pno' href='/" + listName + "pageNum=" + i + "'>" + i + "</a>");
                //<a class='pno' href='/boardList?pageNum=6'>6</a>
            } else { //현재 페이지(링크X)
                sb.append("<font class='pno' style='color : red'>" + i + "</font>");
                //<font class='pno' style='color: red'>8</font>
            }
        }
        //다음 버튼 처리
        if (end != totalPage) {
            sb.append("<a class='pno' href='/" + listName + "pageNum=" + (end + 1) + "'>▶</a>");
        }//<a class='pno' href='/boardList?pageNum=11'>다음</a>

        //StringBuffer -> String으로 변환
        page = sb.toString();
        return page;
    }
}
