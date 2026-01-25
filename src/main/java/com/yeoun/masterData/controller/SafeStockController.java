package com.yeoun.masterData.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.masterData.dto.SafeStockDTO;
import com.yeoun.masterData.service.SafeStockService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/safeStock")
@RequiredArgsConstructor
public class SafeStockController {
	private final SafeStockService safeStockService;
	
	// 안전재고 조회
	@GetMapping("/list")
	public ResponseEntity<List<SafeStockDTO>> safeStock() {
		List<SafeStockDTO> safeStockList = safeStockService.getSafeStockList();
		
		return ResponseEntity.ok(safeStockList);
	}
	
	// 안전재고 등록
	@PostMapping("/list/modify")
	public ResponseEntity<String> modifySafeStock(@RequestBody Map<String, List<SafeStockDTO>> data) {
		List<SafeStockDTO> createdRows = data.get("created");
		List<SafeStockDTO> updatedRows = data.get("updated");
		
		// 신규등록
		if (createdRows != null && !createdRows.isEmpty()) {
			safeStockService.createSafeStock(createdRows);
		}
		
		// 수정
		if (updatedRows != null && !updatedRows.isEmpty()) {
			safeStockService.updateSafeStock(updatedRows);
		}
		
		return ResponseEntity.ok("저장 완료");
	}
	
	// 안전재고 삭제
	@PostMapping("/data/delete")
	public ResponseEntity<String> deleteSafeStock(@RequestBody List<String> data) {
		if (data == null || data.isEmpty()) {
			return ResponseEntity.badRequest().body("삭제할 항목이 없습니다.");
		}
		
		try {
			safeStockService.deleteSafeStock(data);
			
			return ResponseEntity.ok().body("삭제되었습니다.");
			
		} catch (Exception e) {
			e.printStackTrace();
			 return ResponseEntity.status(HttpStatus.NOT_FOUND)
		 		      .body("falid");
		}
		
	}
	
}
