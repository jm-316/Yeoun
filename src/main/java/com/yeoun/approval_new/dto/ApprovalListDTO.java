package com.yeoun.approval_new.dto;

import java.time.LocalDate;

import com.yeoun.approval_new.entity.ApprovalDocument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListDTO {
    // Grid 표시용 (최소)
    private Long approvalId;
    private String formType;
    private String approvalTitle;
    private String empId;
    private String empName;
    private LocalDate createdDate;
    private LocalDate finishDate;
    private String status;
    
    
    public static ApprovalListDTO fromEntity(ApprovalDocument entity) {
        return ApprovalListDTO.builder()
            .approvalId(entity.getApprovalId())
            .formType(entity.getFormType())
            .approvalTitle(entity.getApprovalTitle())
            .empId(entity.getEmployee() != null ? entity.getEmployee().getEmpId() : null)
            .empName(entity.getEmployee() != null ? entity.getEmployee().getEmpName() : null)
            .createdDate(entity.getCreatedDate())
            .finishDate(entity.getFinishDate())
            .status(entity.getStatus())
            .build();
    }
}
