package com.yeoun.masterData.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.service.ItemService;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@RequestMapping("masterData1")
@Log4j2
public class ItemController {
	
	private final ItemService itemService;
	
	// 원재료/완제품 기준정보 페이지
	@GetMapping("")
	public String itemList(Model model) {
		model.addAttribute("activeTab", "mat");
		return "masterData/item_list";
	}
	
	// 원재료 페이지
	@GetMapping("/material")
	public String material(Model model) {
		model.addAttribute("activeTab", "mat");
		return "masterData/item_list";
	}
	
	// 완제품 페이지
	@GetMapping("/product")
	public String product(Model model) {
		model.addAttribute("activeTab", "pro");
		return "masterData/item_list";
	}
	
	// 원재료 조회
	@GetMapping("/data/materialList")
	public ResponseEntity<List<MaterialDTO>> materialList() {
		
		List<MaterialDTO> materialList = itemService.getMaterialList();
		
		return ResponseEntity.ok(materialList);
	}
}
