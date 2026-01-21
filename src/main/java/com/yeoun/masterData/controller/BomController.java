package com.yeoun.masterData.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.dto.BomDTO;
import com.yeoun.masterData.dto.BomItemDTO;
import com.yeoun.masterData.entity.BomItem;
import com.yeoun.masterData.service.BomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/bomMst")
@RequiredArgsConstructor
@Log4j2
public class BomController {
	
	private final BomService bomService;
	
	// BOM 조회
	@GetMapping("/bomList")
	public ResponseEntity<List<BomDTO>> bomList() {
		List<BomDTO> bomList = bomService.getBomList();
		
		return ResponseEntity.ok(bomList);
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
		
		try {
			if (createdRows != null && !createdRows.isEmpty()) {
				bomService.createBomItem(createdRows, loginDTO.getEmpId());
			} else if (updatedRows != null && !updatedRows.isEmpty()) {
				bomService.updateMaterial(updatedRows);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return ResponseEntity.ok("저장 완료");
	}
	
	// BOM Item 삭제
	@DeleteMapping("/data/bomItem/delete")
	public ResponseEntity<String> deleteBomItem(@RequestBody Map<String, List<String>> data) {
		List<String> bomItemIds = data.get("bomItemIds");
		
		if (bomItemIds == null || bomItemIds.isEmpty()) {
			return ResponseEntity.badRequest().body("삭제할 항목이 없습니다.");
		}
		
		bomService.deleteBomItems(bomItemIds);
		
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

}
