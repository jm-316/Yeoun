package com.yeoun.common.dto;

import org.modelmapper.ModelMapper;

import com.yeoun.common.entity.CommonCode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommonCodeIdAndNameDTO {
	private String codeId; // 코드 고유ID
	private String codeName; // 코드명
	
	// -----------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public CommonCode toEntity() {
		return modelMapper.map(this, CommonCode.class);
	}
	
	// DTO 타입으로 변환
	public static CommonCodeIdAndNameDTO fromEntity(CommonCode commonCode) {
		return modelMapper.map(commonCode, CommonCodeIdAndNameDTO.class);
	}
}
