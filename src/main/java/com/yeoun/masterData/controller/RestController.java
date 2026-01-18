package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.common.service.CommonCodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
@RequestMapping("/commomCode")
@Log4j2
public class RestController {
	private final CommonCodeService commonCodeService;
	
	// 원재료 타입 공통코드 조회
	@GetMapping("/matType")
	public ResponseEntity<List<CommonCodeIdAndNameDTO>> matTypeList() {
		List<CommonCodeIdAndNameDTO> matTypeList = commonCodeService.getType("MAT_TYPE");
	
		return ResponseEntity.ok(matTypeList);
	}
	
	// 원재료 / 완제품 단위 공통코드 조회
	@GetMapping("/unit")
	public ResponseEntity<List<CommonCodeIdAndNameDTO>> matUnitList() {
		List<CommonCodeIdAndNameDTO> matTypeList = commonCodeService.getType("UNIT_TYPE");
		
		return ResponseEntity.ok(matTypeList);
	}
	
	// 완제품 타입 공통코드 조회
	@GetMapping("/prdType")
	public ResponseEntity<List<CommonCodeIdAndNameDTO>> prdTypeList() {
		List<CommonCodeIdAndNameDTO> prdTypeList = commonCodeService.getType("PERFUME_TYPE");
	
		return ResponseEntity.ok(prdTypeList);
	}
}
