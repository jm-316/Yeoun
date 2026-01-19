package com.yeoun.approval_new.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApprovalSearchDTO {
    
    private String tab;             // all, my, pending, completed
    private String currentUserId;   // 현재 사용자
    
    // 상단 검색 조건
    private String searchStartDate;  // 결재생성일 시작
    private String searchEndDate;    // 결재생성일 끝
    private String searchKeyword;    // 기안자명 또는 문서제목
    
    // 정렬
    private String sortColumn;
    private Boolean sortAscending;
}
