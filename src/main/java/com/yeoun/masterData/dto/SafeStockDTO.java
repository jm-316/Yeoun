package com.yeoun.masterData.dto;

import org.modelmapper.ModelMapper;

import com.yeoun.attendance.entity.AccessLog;
import com.yeoun.masterData.entity.SafeStock;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class SafeStockDTO {
	private Long safeId;
	private Long itemId;
	private String itemName;
	private String itemCode;
	private String itemType;
	private String itemUnit;
	private Long dailyCapa;
	private Long dailyReqQty;
	private Integer targetDays;
	private Long totalSafeQty;
	private char useYn;
	private String remark;
	
	@Builder
	public SafeStockDTO(Long safeId, Long itemId, String itemName, String itemCode, String itemType, String itemUnit,
			Long dailyCapa, Long dailyReqQty, Integer targetDays, Long totalSafeQty, char useYn, String remark) {
		this.safeId = safeId;
		this.itemId = itemId;
		this.itemType = itemType;
		this.itemUnit = itemUnit;
		this.dailyCapa = dailyCapa;
		this.dailyReqQty = dailyReqQty;
		this.targetDays = targetDays;
		this.totalSafeQty = totalSafeQty;
		this.useYn = useYn;
		this.remark = remark;
	}
	
	// ---------------------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public SafeStock toEntity() {
		return modelMapper.map(this, SafeStock.class);
	}
	
	public static SafeStockDTO fromEntity(SafeStock safeStock) {
		return modelMapper.map(safeStock, SafeStockDTO.class);
	}
	
}
