package com.yeoun.masterData.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;

import com.yeoun.masterData.entity.Bom;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BomDTO {
	private Long bomId;
	private String bomName; // bom이름
	private Long prdId; // 제품ID
	private String prdName;
	private char useYn;
	private LocalDateTime createdDate;
	private String empId;
	
	List<BomItemDTO> items; // bom 품목

	@Builder
	public BomDTO(Long bomId, String bomName, Long prdId, char useYn, LocalDateTime createdDate, String empId,
			String prdName, List<BomItemDTO> items) {
		this.bomId = bomId;
		this.bomName = bomName;
		this.prdId = prdId;
		this.prdName = prdName;
		this.useYn = useYn;
		this.createdDate = createdDate;
		this.empId = empId;
		this.items = items;
	}
	
	// ----------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	public Bom toEntity() {
		Bom bom = new Bom();
		
		bom.setBomId(this.bomId);
		bom.setBomName(this.bomName);
		bom.setCreatedDate(this.createdDate);
		bom.setUseYn(this.useYn);
		
		return bom;
	}
	
	public static BomDTO fromEntity(Bom bom) {
		return BomDTO.builder()
		.bomId(bom.getBomId())
		.bomName(bom.getBomName())
		.createdDate(bom.getCreatedDate())
		.useYn(bom.getUseYn())
		.empId(bom.getEmp() != null ? bom.getEmp().getEmpId() : null)
		.prdId(bom.getProduct() != null ? bom.getProduct().getPrdId() : null)
		.prdName(bom.getProduct() != null ? bom.getProduct().getPrdName() : null)
		.build();
	}
}
