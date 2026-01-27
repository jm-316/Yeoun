package com.yeoun.approval_new.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLineTemplateCreateRequestDTO {
    private String templateName;

    private List<ApproverInfo> approvers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApproverInfo {
        private String empId;
        private Integer stepOrder;
    }
}
