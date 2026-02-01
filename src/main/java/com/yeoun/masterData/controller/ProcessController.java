package com.yeoun.masterData.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.masterData.dto.ProcessMstDTO;
import com.yeoun.masterData.dto.RouteHeaderDTO;
import com.yeoun.masterData.dto.RouteStepDTO;
import com.yeoun.masterData.service.ProcessMstService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import oracle.jdbc.proxy.annotation.Post;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/processMst")
@RequiredArgsConstructor
@Log4j2
public class ProcessController {
	
	private final ProcessMstService processMstService;
	
	// 제품별 공정 라우트 및 공정 코드 관리 페이지
	@GetMapping("")
	public String processList(Model model) {
		model.addAttribute("activeTab", "route");
		return "masterData/process_list";
	}
	
	// 제품별 공정 라우트 페이지
	@GetMapping("/route")
	public String productRoute(Model model) {
		model.addAttribute("activeTab", "route");
		return "masterData/process_list";
	}
	
	// 공정코드 페이지
	@GetMapping("/process")
	public String processCode(Model model) {
		model.addAttribute("activeTab", "process");
		return "masterData/process_list";
	}
	
	// 라우트 조회
	@GetMapping("/routes")
	public ResponseEntity<List<RouteHeaderDTO>> routeList() {
		List<RouteHeaderDTO> routeHeaderList = processMstService.getRouteList();
		
		return ResponseEntity.ok(routeHeaderList);
	}
	
	// 라우트 상세 조회
	@GetMapping("route/{routeId}")
	public ResponseEntity<RouteHeaderDTO> routeInto(@PathVariable("routeId") String routeId) {
		RouteHeaderDTO routeHeaderDTO = processMstService.getRouteInfo(routeId);
		
		return ResponseEntity.ok(routeHeaderDTO);
	}
	
	// 라우트 단계 조회
	@GetMapping("routeStep")
	public ResponseEntity<List<RouteStepDTO>> routeStepList(@RequestParam (value = "useYn", defaultValue = "all") String useYn,
			@RequestParam (value = "routeId") String routeId) {
		List<RouteStepDTO> routeStepDTOs = new ArrayList<>();
		
		if ("all".equals(useYn)) {
			routeStepDTOs = processMstService.getAllRouteStep(routeId);
		} else if ("Y".equals(useYn)) {
			routeStepDTOs = processMstService.getRouteStepListWithUseYn(routeId, useYn);
		}
		
		return ResponseEntity.ok(routeStepDTOs);
	}
	
	// 라우트 신규 등록
	@PostMapping("/route/add")
	public ResponseEntity<String> createRoute(@RequestBody RouteHeaderDTO data, @AuthenticationPrincipal LoginDTO loginDTO) {
		processMstService.saveRouteHeader(data, loginDTO.getEmpId());
		
		return ResponseEntity.ok("저장되었습니다.");
	}
	
	// 라우트 헤더 수정
	@PostMapping("/route/modify/{routeId}")
	public ResponseEntity<String> modifyRoute(@PathVariable("routeId") String routeId, @RequestBody RouteHeaderDTO data, 
			@AuthenticationPrincipal LoginDTO loginDTO) {
		processMstService.modifyRouteHeader(routeId, data, loginDTO.getEmpId());
		
		return ResponseEntity.ok("저장되었습니다.");
	}
	
	// 라우트 단계 수정
	@PostMapping("/routeStep/modify/{routeId}")
	public ResponseEntity<String> modifyRouteStep(@PathVariable("routeId") String routeId, @RequestBody Map<String, List<RouteStepDTO>> data, 
			@AuthenticationPrincipal LoginDTO loginDTO) {
		List<RouteStepDTO> createdRows = data.get("created");
		List<RouteStepDTO> updatedRows = data.get("updated");
		
		if (createdRows != null && !createdRows.isEmpty()) {
			processMstService.createReouteStep(routeId, createdRows, loginDTO.getEmpId());
		}
		
		if (updatedRows != null && !updatedRows.isEmpty()) {
			processMstService.modifyRouteStep(routeId, updatedRows, loginDTO.getEmpId());
		}
		
		
		return ResponseEntity.ok("저장되었습니다.");
	} 
	// ------------------------------------------------------------
	// 공정코드 조회
	@GetMapping("/processCodes")
	public ResponseEntity<List<ProcessMstDTO>> processCodeList(@RequestParam (value = "useYn", defaultValue = "all") String useYn) {
		List<ProcessMstDTO> processCodeList = new ArrayList<>();
		
		if ("all".equals(useYn)) {
			processCodeList = processMstService.getProcessCodeList();
		} else if ("Y".equals(useYn)) {
			processCodeList = processMstService.getProcessCodeListWithUseYn(useYn);
		}
		
		return ResponseEntity.ok(processCodeList);
	}
	
	// 공정코드 등록(추가 및 수정)
	@PostMapping("/processCode/modify")
	public ResponseEntity<String> modifyProcessCode(@RequestBody Map<String, List<ProcessMstDTO>> data, @AuthenticationPrincipal LoginDTO loginDTO) {
		List<ProcessMstDTO> createdRows = data.get("created");
		List<ProcessMstDTO> updatedRows = data.get("updated");
		
		// 신규 등록
		if (createdRows != null && !createdRows.isEmpty()) {
			processMstService.createProcessCode(createdRows, loginDTO.getEmpId());
		}
		
		// 수정
		if (updatedRows != null && !updatedRows.isEmpty()) {
			processMstService.updateProcessCode(updatedRows, loginDTO.getEmpId());
		}
		
		return ResponseEntity.ok("저장되었습니다.");
	}

}
