package com.yeoun.masterData.dto;

import org.modelmapper.ModelMapper;

import com.yeoun.masterData.entity.Bom;
import com.yeoun.masterData.entity.BomItem;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class BomItemDTO {
	private Long bomItemId;
	private Long bomId;
	private Long matId;
	private String matName;
	private String bomUnit;
	private Double bomQty;
	
	@Builder
	public BomItemDTO(Long bomItemId, Long bomId, Long matId, String matName, String bomUnit, Double bomQty) {
		this.bomItemId = bomItemId;
		this.bomId = bomId;
		this.matId = matId;
		this.matName = matName;
		this.bomUnit = bomUnit;
		this.bomQty = bomQty;
	}
	
	// ----------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	public BomItem toEntity() {
		return modelMapper.map(this, BomItem.class);
	}
	
	public static BomItemDTO fromEntity(BomItem bomItem) {
		Bom bom = bomItem.getBom();
		
		return BomItemDTO.builder()
				.bomItemId(bomItem.getBomItemId())
				.bomId(bom.getBomId())
				.matId(bomItem.getMaterial().getMatId())
				.matName(bomItem.getMaterial().getMatName())
				.bomUnit(bomItem.getBomUnit())
				.bomQty(bomItem.getBomQty())
				.build();
	}
}
