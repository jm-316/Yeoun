package com.yeoun.approval_new.dto;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;

import com.yeoun.approval_new.entity.ApprovalDocument;
import com.yeoun.approval_new.entity.ApprovalExpense;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
public class ApprovalExpenseDTO {
    private String expenseType;
    private LocalDate expenseDate;
    
    @Size(max = 100, message = "지출처는 100자를 초과할 수 없습니다.")
    private String expenseVendor;
    
    @Min(value = 0, message = "금액은 0 이상이어야 합니다.")
    private Long expenseAmount;
    
    private static ModelMapper modelMapper = new ModelMapper();
    /**
     * Entity → DTO
     */
    public static ApprovalExpenseDTO fromEntity(ApprovalExpense entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, ApprovalExpenseDTO.class);
    }
    
    /**
     * DTO → Entity
     */
    public ApprovalExpense toEntity(ApprovalDocument document) {
        ApprovalExpense entity = modelMapper.map(this, ApprovalExpense.class);
        entity.setApprovalDocument(document);
        return entity;
    }
}
