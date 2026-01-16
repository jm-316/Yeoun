package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.common.service.CommonCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@Log4j2
public class RestController {
	private final CommonCodeService commonCodeService;
	
	// 원재료 타입 공통코드 조회
	@GetMapping("/commomCode/matType")
	public ResponseEntity<List<CommonCodeIdAndNameDTO>> matTypeList() {
		List<CommonCodeIdAndNameDTO> matTypeList = commonCodeService.getMatType("MAT_TYPE");
	
		return ResponseEntity.ok(matTypeList);
	}
	
	// 원재료 단위 공통코드 조회
	@GetMapping("/commomCode/unit")
	public ResponseEntity<List<CommonCodeIdAndNameDTO>> matUnitList() {
		List<CommonCodeIdAndNameDTO> matTypeList = commonCodeService.getMatType("UNIT_TYPE");
		
		return ResponseEntity.ok(matTypeList);
	}
}
