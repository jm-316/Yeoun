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
@Table(name = "APPROVAL_LEAVE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalLeave {
    @Id
    @Column(name = "approval_id")
    private Long approvalId;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "approval_id")
    private ApprovalDocument approvalDocument;
    
    @Column(name = "leave_type", length = 20, nullable = false)
    private String leaveType;  // "연차", "반차", "병가", "경조사"
    
    @Column(name = "leave_start_date", nullable = false)
    private LocalDate leaveStartDate;
    
    @Column(name = "leave_end_date", nullable = false)
    private LocalDate leaveEndDate;
    
    @Column(name = "leave_days", precision = 3, nullable = false)
    private Double leaveDays;
}
