package com.yeoun.leave.entity;

import java.time.LocalDate;

import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "ANNUAL_LEAVE_HISTORY")
@SequenceGenerator(
		name = "ANNUAL_LEAVE_HISTORY_GENERATOR",
		sequenceName = "ANNUAL_LEAVE_HISTORY_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnnualLeaveHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "ANNUAL_LEAVE_HISTORY_GENERATOR")
	private Long leaveHistId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EMP_ID", nullable = false)
	private Emp emp;
	 
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DEPT_ID", nullable = false)
	private Dept dept;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LEAVE_ID", nullable = false)
	private AnnualLeave annualLeave;
	
	@Column(nullable = false)
	private String leaveType; // 연차 종류 (연차/반차/병가) / (ANNUAL / HALF / SICK)
	
	@Column(nullable = false)
	private LocalDate startDate; // 사용 시작일
	
	@Column(nullable = false)
	private LocalDate endDate; // 사용 종료일
	
	@Column(nullable = false)
	private Double usedDays; //  사용 일수
	
	@Column(nullable = false)
	private String reason; // 연차 사용 이유
	
	private Long approvalId; // 결재문서 Id
	
	@Builder
	public AnnualLeaveHistory(Emp emp, AnnualLeave annualLeave, String leaveType, LocalDate startDate,
			LocalDate endDate, Double usedDays, String reason, Long paymentId) {
		this.emp = emp;
		this.annualLeave = annualLeave;
		this.leaveType = leaveType;
		this.startDate = startDate;
		this.endDate = endDate;
		this.usedDays = usedDays;
		this.reason = reason;
		this.approvalId = paymentId;
	}
	
}
