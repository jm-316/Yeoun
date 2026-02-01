package com.yeoun.masterData.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.masterData.dto.QcItemDTO;
import com.yeoun.masterData.entity.QcItem;
import com.yeoun.masterData.service.QcItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/masterData")
@RequiredArgsConstructor
@Log4j2
public class QcItemController {
	private final QcItemService qcItemService;

	//품질항목관리 연결페이지(검사 X)
  	@GetMapping("/qc_item")
  	public String qcItem(Model model, @AuthenticationPrincipal LoginDTO loginDTO) {
  		List<CommonCodeIdAndNameDTO> targetTypeList = qcItemService.targetTypeList();
  		
		model.addAttribute("targetTypeList", targetTypeList);
		model.addAttribute("unitTypeList", qcItemService.unitTypeList());
		return "masterData/qc_item";
 	}

  	// QC ITEM 전체 조회
  	@GetMapping("/qcItem/list")
  	public ResponseEntity<List<QcItemDTO>> qcItemList() {
  		List<QcItemDTO> qcList = qcItemService.getAllQcItem();
  		
  		return ResponseEntity.ok(qcList);
  	}
  	
  	// QC ITEM 상세 조회
  	@GetMapping("/qcItem/{qcItemId}")
  	public ResponseEntity<QcItemDTO> qcItemInfo(@PathVariable("qcItemId") String qcItemId) {
  		QcItemDTO qcItemDTO = qcItemService.findQcItem(qcItemId);
  		
  		return ResponseEntity.ok(qcItemDTO);
  	}
  	
  	// QC ITEM 신규 등록
  	@PostMapping("/qcItem/add")
  	public ResponseEntity<String> saveQcItem(@RequestBody QcItemDTO data, @AuthenticationPrincipal LoginDTO loginDTO) {
  		qcItemService.insertQcItem(data, loginDTO.getEmpId());
  		
  		return ResponseEntity.ok("저장 완료");
  	}
  	
  	// QC ITEM 수정
  	@PostMapping("/qcItem/{qcItemId}")
  	public ResponseEntity<String> modifyQcItem(@PathVariable("qcItemId") String qcItemId, @RequestBody QcItemDTO data, @AuthenticationPrincipal LoginDTO loginDTO) {
  		qcItemService.updateQcItem(qcItemId, data, loginDTO.getEmpId());
  		
  		return ResponseEntity.ok("저장 완료");
  	}
  	
  	// QC ITEM ID 중복 검사
  	@GetMapping("/qcItem/checkDuplicate")
  	public ResponseEntity<Map<String, Boolean>> checkDuplicate(@RequestParam("qcItemId") String qcItemId) {
  		boolean isDuplicate = qcItemService.existsByQcItemId(qcItemId);
  		
  		Map<String, Boolean> result = new HashMap();
  		result.put("isDuplicate", isDuplicate);
  		
  		return ResponseEntity.ok(result);
  	}
}
