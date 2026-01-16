package com.yeoun.masterData.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.masterData.entity.Material;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MaterialDTO {
	private Long matId;
	
	@NotBlank(message = "원재료 코드는 필수 입력값입니다.")
	private String matCode; // 원재료코드
	
	@NotBlank(message = "원재료 유형은 필수 입력값입니다.")
	private String matType; // 원재료타입
	
	@NotBlank(message = "원재료 품목명은 필수 입력값입니다.")
	private String matName; // 원재료명
	
	@NotBlank(message = "단위는 필수 입력값입니다.")
	private String matUnit; // 원재료단위
	
	@NotBlank(message = "유효일자는 필수 입력값입니다.")
	private Integer effectiveDate;  // 유효일자(개월)
	
	private char useYn; // 사용여부
	
	private String empId; // 생성자
	
	private LocalDateTime createdDate;
	
	// -----------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// Entity 타입으로 변환
	public Material toEntity() {
		return modelMapper.map(this, Material.class);
	}
	
	// DTO 타입으로 변환
	public static MaterialDTO fromEntity(Material material) {
		MaterialDTO materialDTO = modelMapper.map(material, MaterialDTO.class);
		
		materialDTO.setEmpId(material.getEmp().getEmpId());
		
		return materialDTO;
	}
}
