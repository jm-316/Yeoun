package com.yeoun.approval_new.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_expense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalExpense {
    
    @Id
    @Column(name = "approval_id")
    private Long approvalId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "approval_id")
    private ApprovalDocument approvalDocument;
    
    @Column(name = "expense_type", length = 20, nullable = false)
    private String expenseType;  // "출장비", "접대비", "비품구매", "교육비"
    
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    
    @Column(name = "expense_vendor", length = 100, nullable = false)
    private String expenseVendor;
    
    @Column(name = "expense_amount", nullable = false)
    private Long expenseAmount;
}
