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
  		
		model.addAttribute("qcIdList", qcItemService.qcIdList());
		model.addAttribute("targetTypeList", targetTypeList);
		model.addAttribute("unitTypeList", qcItemService.unitTypeList());
		return "masterData/qc_item";
 	}

	// //대상구분 드롭다운
	// @ResponseBody
	// @GetMapping("/qcItem/targetTypeList")
	// public List<Map<String, Object>> targetTypeList() {
	// 	return qcItemService.targetTypeList();
	// }

	// //품질단위 드롭다운
	// @ResponseBody
	// @GetMapping("/qcItem/unitTypeList")
	// public List<Map<String, Object>> unitTypeList() {
	// 	return qcItemService.unitTypeList();
	// }

  	//품질의기준 조회
//  	@ResponseBody
//  	@GetMapping("/qc_item/list")
//  	public List<Map<String, Object>> qcItemList(Model model, @AuthenticationPrincipal LoginDTO loginDTO
//	  			,@RequestParam(value = "qcItemId", required = false) String qcItemId) {
//			log.info("qc_item/list called with qcItemId={}", qcItemId);
//			return qcItemService.qcItemList(qcItemId);
//  	}
  	
  	// QC ITEM 전체 조회
  	@GetMapping("/qc_item/list")
  	public ResponseEntity<List<QcItemDTO>> qcItemList() {
  		List<QcItemDTO> qcList = qcItemService.getAllQcItem();
  		
  		return ResponseEntity.ok(qcList);
  	}
  	
  	// QC ITEM 상세 조회
  	@GetMapping("/qc_item/{qcItemId}")
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
  	
  	
  	
  	
  	
  	//품질기준 저장 (AJAX 전용)
  	@ResponseBody
  	@PostMapping("/qcItem/save")
  	public String saveItem(@AuthenticationPrincipal LoginDTO loginDTO, @RequestParam Map<String,Object> params) {
  		log.info("saveItem params: {}", params);
  		return qcItemService.saveQcItem(loginDTO.getEmpId(), params);
  	}
	//품질기준 삭제 (AJAX 호출을 위한 응답: 텍스트 반환)
	@ResponseBody
	@PostMapping(value = "/qcItem/delete", consumes = "application/json")
	public String deleteItem(@RequestBody List<String> param) {
		return qcItemService.deleteQcItem(param);
	}
  	

}
