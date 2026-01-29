package com.yeoun.masterData.dto;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;

import com.yeoun.masterData.entity.ProcessMst;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProcessMstDTO {
	
	// 공정ID 
	@NotBlank(message = "공정 ID는 필수 입력값입니다.")
	private String processId;
	
	// 공정명 
	@NotBlank(message = "공정명은 필수 입력값입니다.")
	private String processName;
	
	// 공정유형 
	@NotBlank(message = "공정유형은 필수 입력값입니다.")
	private String processType;
	
	// 설명 
	private String description;
	
	// 공정순서 
	private String stepNo;

	// 사용여부
	private String useYn;
	
	// 생성자
	@NotBlank(message = "생성자는 필수 입력값입니다.")
	private String createdId;
	
	// 생성일시
	@NotBlank(message = "생성일시는 필수 입력값입니다.")
	private LocalDateTime createdDate;
	
	// 수정자
	private String updatedId;
	
	// 수정일시
	private LocalDateTime updatedDate;
	
	// ---------------------------------------------------
	// DTO <-> Entity 변환
	private static ModelMapper modelMapper = new ModelMapper();
	
	// 엔티티 타입으로 변환
	public ProcessMst toEntity() {
		return modelMapper.map(this, ProcessMst.class);
	}
	
	public static ProcessMstDTO fromEntity(ProcessMst processMst) {
		return modelMapper.map(processMst, ProcessMstDTO.class);
	}

}
