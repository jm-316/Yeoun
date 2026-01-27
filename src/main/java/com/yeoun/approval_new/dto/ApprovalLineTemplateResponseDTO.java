package com.yeoun.approval_new.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

import com.yeoun.approval_new.entity.ApprovalLineTemplate;
import com.yeoun.approval_new.entity.ApprovalLineTemplateDetail;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ApprovalLineTemplateResponseDTO {
    private Long templateId;
    private String templateName;
    private String empId;
    private String isDefault;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private List<TemplateDetailInfo> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TemplateDetailInfo {
        private Long detailId;
        private String approverId;
        private String approverName;
        private String posName;
        private Integer stepOrder;
    }
    
    private static ModelMapper modelMapper = new ModelMapper();

    public static ApprovalLineTemplateResponseDTO fromEntity(ApprovalLineTemplate entity) {
        
        List<TemplateDetailInfo> detailList = entity.getDetails()
            .stream()
            .map(detail -> TemplateDetailInfo.builder()
                .detailId(detail.getDetailId())
                .approverId(detail.getApprover().getEmpId())
                .approverName(detail.getApprover().getEmpName())
                .posName(detail.getApprover().getPosition().getPosName())
                .stepOrder(detail.getStepOrder())
                .build())
            .collect(Collectors.toList());
        
        return ApprovalLineTemplateResponseDTO.builder()
            .templateId(entity.getTemplateId())
            .templateName(entity.getTemplateName())
            .empId(entity.getEmp().getEmpId())
            .isDefault(entity.getIsDefault())
            .createdDate(entity.getCreatedDate())
            .modifiedDate(entity.getModifiedDate())
            .details(detailList)
            .build();
    }
}

