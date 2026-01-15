package com.yeoun.approval_new.dto;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalLeave;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class ApprovalLeaveDTO {
    private String leaveType;
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;
    
    @Min(value = 0, message = "휴가 일수는 0 이상이어야 합니다.")
    private Double leaveDays;
    
    private static ModelMapper modelMapper = new ModelMapper();
    
    /**
     * Entity → DTO
     */
    public static ApprovalLeaveDTO fromEntity(ApprovalLeave entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, ApprovalLeaveDTO.class);
    }
    
    /**
     * DTO → Entity
     */
    public ApprovalLeave toEntity(ApprovalDocument document) {
        ApprovalLeave entity = modelMapper.map(this, ApprovalLeave.class);
        entity.setApprovalDocument(document);
        return entity;
    }
}
