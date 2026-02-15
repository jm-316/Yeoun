package com.yeoun.masterData.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.dto.MaterialDTO;
import com.yeoun.masterData.dto.ProductDTO;
import com.yeoun.masterData.dto.SafeStockDTO;
import com.yeoun.masterData.service.ItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@RequiredArgsConstructor
@RequestMapping("masterData")
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
	public ResponseEntity<List<MaterialDTO>> materialList(@RequestParam (value = "useYn", defaultValue = "all") String useYn) {
		List<MaterialDTO> materialList = new ArrayList<>();
		
		if ("all".equals(useYn)) {
			materialList = itemService.getMaterialList();
		} else if ("Y".equals(useYn)) {
			materialList = itemService.getMaterialListWithUseYn(useYn);
		}
		
		return ResponseEntity.ok(materialList);
	}
	
	// 원재료 등록
	@PostMapping("/data/material/add")
	public ResponseEntity<String> modifyMaterial(@RequestBody Map<String, List<MaterialDTO>> data, @AuthenticationPrincipal LoginDTO loginDTO) {
		List<MaterialDTO> createdRows = data.get("created");
		List<MaterialDTO> updatedRows = data.get("updated");
		
		// 신규 등록
		if (createdRows != null && !createdRows.isEmpty()) {
			itemService.createMaterial(createdRows, loginDTO.getEmpId());
		}
		
		// 수정
		if (updatedRows != null && !updatedRows.isEmpty()) {
			itemService.updateMaterial(updatedRows);
		}
		
		return ResponseEntity.ok("저장 완료");
	}
	
	// 원재료 코드 수정 가능 여부 확인
	@GetMapping("/data/check")
	public ResponseEntity<Map<String, Boolean>> checkMatCode(@RequestParam("matCode") String matCode) {
		boolean isUsed = itemService.isMatCodeInUse(matCode);
		
		Map<String, Boolean> data = new HashMap<>();
		data.put("isUsed", isUsed);
		
		return ResponseEntity.ok(data);
	}
	
	// ==========================================
	// 완제품 조회
	@GetMapping("/data/productList")
	public ResponseEntity<List<ProductDTO>> productList(@RequestParam (value = "useYn", defaultValue = "all") String useYn) {
		List<ProductDTO> productList = new ArrayList<>();
		
		if ("all".equals(useYn)) {
			productList = itemService.getProductList();
		} else if ("Y".equals(useYn)) {
			productList = itemService.getProductListWithUseYn(useYn);
		}
		
		
		return ResponseEntity.ok(productList);
	}
	
	// 완제품 등록
	@PostMapping("/data/product/add")
	public ResponseEntity<String> modifyProduct(@RequestBody Map<String, List<ProductDTO>> data, @AuthenticationPrincipal LoginDTO loginDTO) {
		List<ProductDTO> createdRows = data.get("created");
		List<ProductDTO> updatedRows = data.get("updated");
		
		// 신규 등록
		if (createdRows != null && !createdRows.isEmpty()) {
			itemService.createProduct(createdRows, loginDTO.getEmpId());
		}
		
		// 수정
		if (updatedRows != null && !updatedRows.isEmpty()) {
			itemService.updateProduct(updatedRows);
		}
		
		return ResponseEntity.ok("저장 완료");
	}
	
	// 완제품 코드 수정 가능 여부 확인
	@GetMapping("/data/check/prd")
	public ResponseEntity<Map<String, Boolean>> checkPrdCode(@RequestParam("prdCode") String prdCode) {
		boolean isUsed = itemService.isPrdCodeInUse(prdCode);
		
		Map<String, Boolean> data = new HashMap<>();
		data.put("isUsed", isUsed);
		
		return ResponseEntity.ok(data);
	}
	
	// 원재료 및 완제품 모든 데이터 가져오기
	@GetMapping("/items")
	public ResponseEntity<List<SafeStockDTO>> itemList() {
		List<SafeStockDTO> itemList = itemService.findAllItems();
		
		return ResponseEntity.ok(itemList);
	}
}
