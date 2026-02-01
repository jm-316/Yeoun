package com.yeoun.masterData.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.dto.ProcessMstDTO;
import com.yeoun.masterData.dto.RouteHeaderDTO;
import com.yeoun.masterData.dto.RouteStepDTO;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;
import com.yeoun.masterData.repository.ProcessMstRepository;
import com.yeoun.masterData.repository.ProductRepository;
import com.yeoun.masterData.repository.RouteHeaderRepository;
import com.yeoun.masterData.repository.RouteStepRepository;
import com.yeoun.order.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProcessMstService {
	
	private final ProcessMstRepository processMstRepository;
	private final RouteHeaderRepository routeHeaderRepository;
	private final RouteStepRepository routeStepRepository;
	private final ProductRepository productRepository;
	private final WorkOrderRepository workOrderRepository;
	
	// 라우트 조회
	public List<RouteHeaderDTO> getRouteList() {
		return routeHeaderRepository.findAllWithProduct()
				.stream()
				.map(RouteHeaderDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 라우트 상세 조회
	public RouteHeaderDTO getRouteInfo(String routeId) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		RouteHeaderDTO headerDTO = RouteHeaderDTO.fromEntity(routeHeader);
		
		return headerDTO;
	}
	
	// 라우트 단계 조회(전체)
	public List<RouteStepDTO> getAllRouteStep(String routeId) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		List<RouteStep> routeSteps = routeStepRepository.findAllByRouteId(routeHeader.getRouteId());
		
		return routeSteps.stream()
				.map(RouteStepDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 라우트 조회 (활성여부)
	public List<RouteStepDTO> getRouteStepListWithUseYn(String routeId, String useYn) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		List<RouteStep> routeSteps = routeStepRepository.findByRouteIdAndUseYn(routeHeader.getRouteId(), useYn);
		
		return routeSteps.stream()
				.map(RouteStepDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 신규 라우트 추가
	public void saveRouteHeader(RouteHeaderDTO data, String empId) {
		Product product = productRepository.findByPrdCode(data.getPrdId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 제품 입니다."));
		
		String routeId = normalizeRouteId(data.getRouteId());
		
		RouteHeader routeHeader = RouteHeader.builder()
				.routeId(routeId)
				.routeName(data.getRouteName())
				.product(product)
				.description(data.getDescription())
				.useYn(data.getUseYn())
				.createdId(empId)
				.build();
		
		for (RouteStepDTO item : data.getItems()) {
			ProcessMst processMst = processMstRepository.findByProcessId(item.getProcessId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공정 코드 입니다."));
			
			String routeStepId = normalizeRouteStepId(product.getPrdCode());
			
			RouteStep routeStep = RouteStep.builder()
					.routeStepId(routeStepId)
					.process(processMst)
					.qcPointYn(item.getQcPointYn())
					.remark(item.getRemark())
					.useYn(item.getUseYn())
					.stepSeq(item.getStepSeq())
					.createdId(empId)
					.build();
			
			routeHeader.addItem(routeStep);
		}
		
		routeHeaderRepository.save(routeHeader);
		
	}
	
	// 라우트 헤더 수정
	public void modifyRouteHeader(String routeId, RouteHeaderDTO data, String empId) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		if ("N".equals(data.getUseYn()) && "Y".equals(routeHeader.getUseYn())) {
			List<String> statuses = List.of("CREATED", "RELEASED", "IN_PROGRESS");
			
			boolean useInWorkOrder = workOrderRepository.existsByRouteIdAndStatusIn(routeHeader.getRouteId(), statuses);
			
			if (useInWorkOrder) {
				throw new IllegalStateException("해당 ROUTE가 사용 중임으로 비활성을 하지 못합니다.");
			}
		}

		routeHeader.update(data.getRouteName(), data.getDescription(), data.getUseYn(), empId);
	}
	
	// 라우트 단계 추가 (수정 모달에서 사용)
	public void createReouteStep(String routeId, List<RouteStepDTO> createdRows, String empId) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		for (RouteStepDTO item : createdRows) {
			ProcessMst processMst = processMstRepository.findByProcessId(item.getProcessId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공정 코드 입니다."));
			
			String routeStepId = normalizeRouteStepId(routeHeader.getProduct().getPrdCode());
			
			RouteStep routeStep = RouteStep.builder()
					.routeStepId(routeStepId)
					.routeHeader(routeHeader)
					.process(processMst)
					.qcPointYn(item.getQcPointYn())
					.remark(item.getRemark())
					.useYn(item.getUseYn())
					.stepSeq(item.getStepSeq())
					.createdId(empId)
					.build();
			
			routeStepRepository.save(routeStep);
		}
		
	}
	
	// 라우트 단계 수정
	public void modifyRouteStep(String routeId, List<RouteStepDTO> data, String empId) {
		RouteHeader routeHeader = routeHeaderRepository.findByRouteId(routeId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 라우트 입니다."));
		
		List<String> statuses = List.of("CREATED", "RELEASED", "IN_PROGRESS");
		
		List<String> stepIds = data.stream()
				.map(RouteStepDTO::getRouteStepId)
				.toList();
		
		Map<String, RouteStep> stepMap = routeStepRepository.findAllByRouteStepIdIn(stepIds).stream()
				.collect(Collectors.toMap(RouteStep::getRouteStepId, Function.identity()));
		
		for (RouteStepDTO dto : data) {
			// dto에 있는 routeStepId로 Map에서 RouteStep을 찾고, 없으면 예외를 던짐
			RouteStep routeStep = stepMap.get(dto.getRouteStepId());
			
			if (routeStep == null) {
			    throw new IllegalArgumentException("존재하지 않는 라우트 단계 입니다.");
			}
			
			if (isDeactivation(dto, routeStep)) {
				boolean useInWorkOrder = workOrderRepository.existsByRouteIdAndStatusIn(routeHeader.getRouteId(), statuses);
				 
				if (useInWorkOrder) {
					 throw new IllegalStateException("해당 ROUTE가 사용 중임으로 비활성을 하지 못합니다.");
				}
			 }
			
			routeStep.changeQcPointYn(dto.getQcPointYn());
			routeStep.changeUseYn(dto.getUseYn());
		}
	}
	
	/**
	 * QC활성화 및 ROUTE STEP가 활성 단계에서 비활성화로 전환되는지 판단하는 메서드
	 * 
	 * @param dto
	 * @param routeStep
	 * @return
	 */
	private boolean isDeactivation(RouteStepDTO dto, RouteStep routeStep) {
		return ("N".equals(dto.getQcPointYn()) && "Y".equals(routeStep.getQcPointYn()))
				|| ("N".equals(dto.getUseYn())     && "Y".equals(routeStep.getUseYn()));
	}
	
	// ---------------------------------------
	// 공정코드 전체 조회
	public List<ProcessMstDTO> getProcessCodeList() {
		return processMstRepository.findAll()
				.stream()
				.map(ProcessMstDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 공정 코드 활성화된 목록만 조회
	public List<ProcessMstDTO> getProcessCodeListWithUseYn(String useYn) {
		
		return processMstRepository.findByUseYn(useYn)
				.stream()
				.map(ProcessMstDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 공정코드 신규 등록
	public void createProcessCode(List<ProcessMstDTO> createdRows, String empId) {
		List<ProcessMst> processes = createdRows
				.stream()
				.map(dto -> {
					ProcessMst entity = dto.toEntity();
					entity.setProcessId(normalizeProcessCodeId(dto.getProcessId()));
					entity.setCreatedId(empId);
					return entity;
				})
				.collect(Collectors.toList());
		
		processMstRepository.saveAll(processes);
	}
	
	// 공정코드 수정
	public void updateProcessCode(List<ProcessMstDTO> updatedRows, String empId) {
		for (ProcessMstDTO dto : updatedRows) {
			ProcessMst processMst = processMstRepository.findByProcessId(dto.getProcessId())
					.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공정 코드입니다."));
			
			if ("N".equals(dto.getUseYn()) && "Y".equals(processMst.getUseYn())) {
				// 활성화된 ROUTE_HEADER에 포함된 ROUTE_STEP이 있는지 확인
				 boolean isUsedInActiveRoute = routeStepRepository.existsActiveRouteUsingProcess(dto.getProcessId());
				 
				 if (isUsedInActiveRoute) {
		                throw new IllegalStateException("해당 공정은 현재 사용 중인 공정 라우트에 포함되어 있어 비활성화할 수 없습니다.");
				 }
			}
			
			processMst.updateProcess(dto.getProcessName(), dto.getDescription(), dto.getStepNo(), dto.getUseYn(), empId);
		}
	}
	
	private String normalizeProcessCodeId(String processId) {
		if (processId == null || processId.isBlank()) {
			throw new IllegalArgumentException("공정 ID는 필수입니다.");
		}
		
		return "PRC-" + processId;
	}
	
	private String normalizeRouteId(String routeId) {
		if (routeId == null || routeId.isBlank()) {
			throw new IllegalArgumentException("라우트 ID는 필수입니다.");
		}
		
		return "RT-" + routeId;
	}
	
	private String normalizeRouteStepId(String prdCode) {
		return "RS-" + prdCode + "-" +
				UUID.randomUUID().toString().substring(0, 3).toUpperCase();
	}
}