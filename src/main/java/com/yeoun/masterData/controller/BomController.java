package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

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
		
		log.info(">>>>>>>>>>>>>>>> bomId" + bomId);
		
		List<BomItemDTO> bomItemList = bomService.getBomItemList(bomId);
		
		return ResponseEntity.ok(bomItemList);
	}

}
