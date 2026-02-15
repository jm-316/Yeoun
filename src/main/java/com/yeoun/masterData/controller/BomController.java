package com.yeoun.masterData.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.dto.BomDTO;
import com.yeoun.masterData.dto.BomItemDTO;
import com.yeoun.masterData.entity.Bom;
import com.yeoun.masterData.entity.BomItem;
import com.yeoun.masterData.service.BomService;
import com.yeoun.outbound.dto.OutboundOrderItemDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/bomMst")
@RequiredArgsConstructor
@Log4j2
public class BomController {
	
	private final BomService bomService;
	
	//BOM 연결페이지
	@GetMapping("/bomStock")
	public String bomStock() {
		return "masterData/bom_stock";
	}
	
	// BOM 조회
	@GetMapping("/bomList")
	public ResponseEntity<List<BomDTO>> bomList() {
		List<BomDTO> bomList = bomService.getBomList();
		
		return ResponseEntity.ok(bomList);
	}
	
	// BOM 등록
	@PostMapping("/data/bom/add")
	public ResponseEntity<String> registBom(@RequestBody BomDTO bomDTO, @AuthenticationPrincipal LoginDTO loginDTO) {
		bomService.createBom(bomDTO, loginDTO.getEmpId());
		
		return ResponseEntity.ok("등록 완료");
	}
	
	// BOM 수정
	@PostMapping("/data/bom/modify")
	public ResponseEntity<String> modifyBom(@RequestBody List<BomDTO> data) {
		bomService.modifyBom(data);
		return ResponseEntity.ok("저장 완료");
	}
	
	// BOM Item 조회
	@GetMapping("/bomItems/{bomId}")
	public ResponseEntity<List<BomItemDTO>> bomItemList(@PathVariable("bomId") Long bomId) {
		
		List<BomItemDTO> bomItemList = bomService.getBomItemList(bomId);
		
		return ResponseEntity.ok(bomItemList);
	}
	
	// Bom Item 등록
	@PostMapping("/data/bomItem/add")
	public ResponseEntity<String> modifyBomItem(@RequestBody Map<String, List<BomItemDTO>> data, @AuthenticationPrincipal LoginDTO loginDTO) {
		List<BomItemDTO> createdRows = data.get("created");
		List<BomItemDTO> updatedRows = data.get("updated");
		
		if (createdRows != null && !createdRows.isEmpty()) {
			bomService.createBomItem(createdRows, loginDTO.getEmpId());
		} else if (updatedRows != null && !updatedRows.isEmpty()) {
			bomService.updateMaterial(updatedRows);
		}
		return ResponseEntity.ok("저장 완료");
	}
	
	// BOM Item 삭제
	@PostMapping("/data/bomItem/delete")
	public ResponseEntity<String> deleteBomItem(@RequestBody List<String> data) {
		
		if (data == null || data.isEmpty()) {
			return ResponseEntity.badRequest().body("삭제할 항목이 없습니다.");
		}
	
		bomService.deleteBomItems(data);
		return ResponseEntity.ok().body("삭제되었습니다.");
	}
	
	// Bom Name 중복 검사
	@GetMapping("/data/checkDuplicate")
	public ResponseEntity<Map<String, Boolean>> checkDuplicate(@RequestParam("bomName") String bomName) {
		boolean isDuplicate = bomService.existsByBomName(bomName);
		
		Map<String, Boolean> result = new HashMap();
		result.put("isDuplicate", isDuplicate);
		
		return ResponseEntity.ok(result);
	}
	
 	@GetMapping("/list/data/{prdId}")
 	@ResponseBody
// 	public ResponseEntity<List<OutboundOrderItemDTO>> outboundBomList(@PathVariable("prdId") String prdId) {
 	public ResponseEntity<?> outboundBomList(@PathVariable("prdId") String prdId) {
 		
 		
 		try {
 			List<OutboundOrderItemDTO> bomList = bomService.getBomListByPrdId(prdId);
 			return ResponseEntity.ok(bomList);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					 .body(Map.of("message", e.getMessage()));
 		}
 		
 	}

}
