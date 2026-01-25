package com.yeoun.masterData.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "SAFE_STOCK")
@SequenceGenerator(
		name = "SAFE_STOCK_SEQ_GENERATOR",
		sequenceName = "SAFE_STOCK_SEQ", 
		initialValue = 1,
		allocationSize = 1
)
@Getter
@Setter
@ToString
public class SafeStock {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SAFE_STOCK_SEQ_GENERATOR")
	private Long safeId;
	
	@Column(nullable = false)
	private Long itemId; // 품목 ID
	
	@Column(nullable = false)
	private String itemType; // 품목타입, 원재료: MAT / 완제품 : PRD
	
	@Column(nullable = false)
	private String itemUnit; // 단위
	
	@Column(nullable = false)
	private Long dailyCapa; // 일 생산량
	
	@Column(nullable = false)
	private Long dailyReqQty; // 일일 소요량
	
	@Column(nullable = false)
	private Integer targetDays; // 목표 보관 일수
	
	@Column(nullable = false)
	private Long totalSafeQty; // 총 안전재고 수량 
	
	@Column(nullable = false)
	private char useYn; // 사용여부
	
	private String remark; // 비고
	
	// 총 안전재고 계산
	private void calcurateTotalSafeQty() {
		long reqQty = (this.dailyReqQty != null) ? this.dailyReqQty : 0L;
		int days = (this.targetDays != null) ? this.targetDays : 0;
		
		// 일일소요량 * 목표일수
		this.totalSafeQty = reqQty * days;
	}
	
	public void updateSafeCriteria(long dailyReqQty, Integer targetDays) {
		this.dailyReqQty = dailyReqQty;
		this.targetDays = targetDays;
		calcurateTotalSafeQty(); 
	}
}
