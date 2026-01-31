package com.yeoun.masterData.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.masterData.dto.ProcessMstDTO;
import com.yeoun.masterData.dto.RouteHeaderDTO;
import com.yeoun.masterData.dto.RouteStepDTO;
import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.Product;
import com.yeoun.masterData.entity.ProductMst;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;
import com.yeoun.masterData.repository.ProcessMstRepository;
import com.yeoun.masterData.repository.ProductMstRepository;
import com.yeoun.masterData.repository.RouteHeaderRepository;
import com.yeoun.masterData.repository.RouteStepRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class ProcessMstService {
	
	private final ProductMstRepository productMstRepository;
	private final ProcessMstRepository processMstRepository;
	private final RouteHeaderRepository routeHeaderRepository;
	private final RouteStepRepository routeStepRepository;
	
	//제품별공정 라우트 제품코드 드롭다운
	@Transactional(readOnly = true)
	public List<ProductMst> getPrdMst() {
		return routeHeaderRepository.findAllPrd();
	}
	// 제품별 공정 라우트 그리드 조회
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRouteHeaderList(String prdId, String routeName) {
		
		return routeHeaderRepository.findByPrdIdAndRouteName(prdId, routeName);
	}
	//공정코드 드롭다운
	@Transactional(readOnly = true)
	public List<String> processIdList() {
		return processMstRepository.processIdList();
	}
	// 공정코드 그리드 조회
	@Transactional(readOnly = true)
	public List<Map<String, Object>> findByprocesslList(String processId, String processName) {
		return processMstRepository.findByprocesslList(processId, processName);
	}
	// 공정단계 그리드 조회
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getProcessStepList(String routeId) {
		log.info("getProcessStepList 조회 - {}", routeId);
		return routeStepRepository.findRouteStepByRouteId(routeId);
	}
	// 공정코드 그리드 저장
	public String saveProcessCode(String empId, Map<String,Object> param) {
		log.info("processCodeSave-Service------------>{}",param);
		try {

			// createdRows
			Object createdObj = param.get("createdRows");
			if (createdObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> created = (List<Map<String,Object>>) createdObj;
				for (Map<String,Object> row : created) {
					// 기본값 및 유효성 검사: null로 인한 NPE/DB 제약 위반을 방지
					Object idObj = row.get("processId");
					Object nameObj = row.get("processName");
					if (idObj == null || nameObj == null || String.valueOf(idObj).trim().isEmpty() || String.valueOf(nameObj).trim().isEmpty()) {
						throw new IllegalArgumentException("processId 및 processName은 필수입니다: " + row);
					}
					String processId = String.valueOf(idObj).trim();
					String processName = String.valueOf(nameObj).trim();
					
					// 신규 등록 - processId 중복검사
					if(processMstRepository.existsById(processId)) {
						return "error: 중복되는 공정 ID가 이미 존재합니다. (processId=" + processId + ")";
					}
					
					String description = (row.get("description") == null) ? "" : String.valueOf(row.get("description"));
					String processType = (row.get("processType") == null || String.valueOf(row.get("processType")).trim().isEmpty()) ? "GENERAL" : String.valueOf(row.get("processType")).trim();
					String useYn = (row.get("useYn") == null || String.valueOf(row.get("useYn")).trim().isEmpty()) ? "Y" : String.valueOf(row.get("useYn")).trim();

					ProcessMst processMst = ProcessMst.builder()
							.processId(processId)
							.processName(processName)
							.description(description)
							.processType(processType)
							.stepNo(row.get("stepNo").toString())
							.useYn(useYn)
							.createdId(empId)
							.createdDate(LocalDateTime.now())
							.build();
					processMstRepository.save(processMst);
				}
				
			}
			//updatedRows
			Object updatedObj = param.get("updatedRows");
			if (updatedObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> updated = (List<Map<String,Object>>) updatedObj;
				for (Map<String,Object> row : updated) {
					// 기본값 및 유효성 검사: null로 인한 NPE/DB 제약 위반을 방지
					Object idObj = row.get("processId");
					Object nameObj = row.get("processName");
					if (idObj == null || nameObj == null || String.valueOf(idObj).trim().isEmpty() || String.valueOf(nameObj).trim().isEmpty()) {
						throw new IllegalArgumentException("processId 및 processName은 필수입니다: " + row);
					}
					String processId = String.valueOf(idObj).trim();//Java에서 객체를 문자열로 변환하고, 그 문자열의 앞뒤 공백을 제거
					String processName = String.valueOf(nameObj).trim();
				
					ProcessMst processMst = ProcessMst.builder()
							.processId(processId)
							.processName(processName)
							.description(row.get("description").toString())
							.processType(row.get("processType").toString())
							.stepNo(row.get("stepNo").toString())
							.useYn(row.get("useYn").toString())
							.createdId(row.get("createdId").toString())
							.createdDate(
								java.time.OffsetDateTime.parse(row.get("createdDate").toString()).toLocalDateTime()
							)
							.updatedId(empId)
							.updatedDate(LocalDateTime.now())
							.build();
					processMstRepository.save(processMst);
				}
				
			}

			return "success";
		} catch (Exception e) {
			log.error("saveProcessCode error", e);
			return "error: " + e.getMessage();
		}
	}
	
	// 공정단계 그리드 저장
	public String saveProcess(String empId, Map<String,Object> param) {
		log.info("productMstSaveList-Service------------>{}",param);
		try {
			//routeInfo created만저장
			RouteHeader routeHeader = null;
			Object routeInfoObj = param.get("routeInfo");
			if (routeInfoObj instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String,Object> routeInfo = (Map<String,Object>) routeInfoObj;
				
				// 1. 필요한 PK 및 연관 객체 조회 (routeId로 RouteHeader 조회)
	            String routeId = routeInfo.get("routeId").toString();
	            String mode = routeInfo.get("mode") != null ? routeInfo.get("mode").toString() : "new";
	            
	            // routeId 중복 검사 (신규 등록 시에만)
	            if ("new".equals(mode)) {
	            	Optional<RouteHeader> duplicateCheck = routeHeaderRepository.findById(routeId);
	            	if (duplicateCheck.isPresent()) {
	            		return "error: 중복되는 라우트 ID가 이미 존재합니다. (routeId=" + routeId + ")";
	            	}
	            }
	            
	            Optional<RouteHeader> existingHeaderOpt = routeHeaderRepository.findById(routeId);
//				Optional<Product> pmPrdId = productMstRepository.findById(routeInfo.get("prdId").toString());
//				
//
//				if (pmPrdId.isEmpty()) {
//	                return "FAIL: ProductMst (prdId) not found.";
//	            }
//				
//				Product product = pmPrdId.get(); // Optional에서 실제 엔티티 추출
//				//수정시 저장
//				if(existingHeaderOpt.isPresent()) {
//					RouteHeader existingHeader = existingHeaderOpt.get();
//					
//					routeHeader = RouteHeader.builder()
//							.routeId(routeInfo.get("routeId").toString())
//							.product(product)// Optional<ProductMst> 대신 ProductMst 객체 사용
//							.routeName(routeInfo.get("routeName").toString())
//							.useYn(routeInfo.get("useYn").toString())
//							.description(routeInfo.get("description").toString())
//							.createdId(existingHeader.getCreatedId())
//							.createdDate(existingHeader.getCreatedDate())
//							.updatedId(empId)
//							.updatedDate(LocalDateTime.now())
//							.build();
//					
//					
//				}else {
//					// --- 🅱️ 신규 등록 로직 (INSERT) ---
//	                routeHeader = RouteHeader.builder()
//	                        .routeId(routeId)
//	                        .product(productMst)
//	                        .routeName(routeInfo.get("routeName").toString())
//	                        .useYn(routeInfo.get("useYn").toString())
//	                        .description(routeInfo.get("description").toString())
//	                        
//	                        // 신규 등록 필드 설정
//	                        .createdId(empId)
//	                        .createdDate(LocalDateTime.now())
//	                        .build();
//					
//				}
				routeHeaderRepository.save(routeHeader);
			}

			// createdRows
			Object createdObj = param.get("createdRows");
			if (createdObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> created = (List<Map<String,Object>>) createdObj;
				for (Map<String,Object> row : created) {
					RouteStep routeStep = RouteStep.builder()
							.routeStepId(row.get("routeStepId").toString())
							.routeHeader(routeHeaderRepository.findById(row.get("routeId").toString())
											.orElseThrow(() -> new IllegalArgumentException("routeHeader-routeId 없음: " + row.get("routeId").toString())))
							.stepSeq(Integer.parseInt(row.get("stepSeq").toString()))
							.process(processMstRepository.findById(row.get("processId").toString())
											.orElseThrow(() -> new IllegalArgumentException("process-processId 없음: " + row.get("processId").toString())))
							.qcPointYn(row.get("qcPointYn").toString())
							.remark(row.get("remark").toString())
							.createdId(empId)
							.createdDate(LocalDateTime.now())
							.build();
					routeStepRepository.save(routeStep);
				}
				
			}

			// updatedRows
			Object updatedObj = param.get("updatedRows");
			if (updatedObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> updated = (List<Map<String,Object>>) updatedObj;
				for (Map<String,Object> row : updated) {
					
					String routeStepId  = row.get("routeStepId").toString();
					Optional<RouteStep> existingStepOpt = routeStepRepository.findById(routeStepId);
					if(existingStepOpt.isPresent()) {
						RouteStep existingStep = existingStepOpt.get();
					
						RouteStep routeStep = RouteStep.builder()
								.routeStepId(row.get("routeStepId").toString())
								.routeHeader(routeHeaderRepository.findById(row.get("routeId").toString())
												.orElseThrow(() -> new IllegalArgumentException("routeHeader-routeId 없음: " + row.get("routeId").toString())))
								.stepSeq(Integer.parseInt(row.get("stepSeq").toString()))
								.process(processMstRepository.findById(row.get("processId").toString())
												.orElseThrow(() -> new IllegalArgumentException("process-processId 없음: " + row.get("processId").toString())))
								.qcPointYn(row.get("qcPointYn").toString())
								.remark(row.get("remark").toString())
								.createdId(existingStep.getCreatedId())
								.createdDate(existingStep.getCreatedDate())
								.updatedId(empId)
								.updatedDate(LocalDateTime.now())
								.build();
						routeStepRepository.save(routeStep);
						
					}
				
				}
			}
				
			

			return "success";
		} catch (Exception e) {
			log.error("saveProductMst error", e);
			return "error: " + e.getMessage();
		}
	}

	// 제품별공정 라우트 삭제수정 useYn='N' 처리 
	public String modifyProcess(String empId, Map<String,Object> param) {
		log.info("modifyProcess-Service------------>{}",param);
		try {

			// routes
			Object routesObj = param.get("routes");
			if (routesObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> routes = (List<Map<String,Object>>) routesObj;
				for (Map<String,Object> row : routes) {
					Object idObj = row.get("routeId");
					if (idObj == null) continue;
					String routeId = String.valueOf(idObj);
					RouteHeader routeHeader = routeHeaderRepository.findById(routeId)
							.orElseThrow(() -> new IllegalArgumentException("routeHeader-routeId 없음: " + routeId));
					routeHeader.setUseYn("N");
					routeHeader.setUpdatedId(empId);
					routeHeader.setUpdatedDate(LocalDateTime.now());
					routeHeaderRepository.save(routeHeader);
				}
			}

			return "success";
		} catch (Exception e) {
			log.error("deleteProcess error", e);
			return "error: " + e.getMessage();
		}
	}
	// 공정코드 삭제수정 useYn='N' 처리
	public String modifyProcessCode(String empId, Map<String,Object> param) {
		log.info("modifyProcessCode-Service------------>{}",param);
		try {

			// processCodes
			Object processCodesObj = param.get("processCodes");
			if (processCodesObj instanceof List) {
				List<?> rawList = (List<?>) processCodesObj;
				for (Object elem : rawList) {
					Object idObj = null;
					if (elem instanceof Map) {
						@SuppressWarnings("unchecked")
						Map<String,Object> row = (Map<String,Object>) elem;
						idObj = row.get("processId");
					} else if (elem instanceof String || elem instanceof Number) {
						idObj = elem;
					}
					if (idObj == null) continue;
					String processId = String.valueOf(idObj);
					ProcessMst processMst = processMstRepository.findById(processId)
							.orElseThrow(() -> new IllegalArgumentException("process-processId 없음: " + processId));
					processMst.setUseYn("N");
					processMst.setUpdatedId(empId);
					processMst.setUpdatedDate(LocalDateTime.now());
					processMstRepository.save(processMst);
				}
				// 강제 flush로 즉시 DB에 반영하여 트랜잭션 중 에러를 빠르게 확인
				processMstRepository.flush();
			}

			return "success";
		} catch (Exception e) {
			log.error("deleteProcessCode error", e);
			return "error: " + e.getMessage();
		}
	}
	
	//라우트모달 - 공정단계 삭제
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String deleteRouteStep(String empId,List<String> param) {
		log.info("deleteRouteStep------------->{}",param);
		try {
			for (String routeStepId : param) {
				if (routeStepRepository.existsById(routeStepId)) {
					routeStepRepository.deleteById(routeStepId);
				}
			}
			return "success";
		} catch (Exception e) {
			log.error("deleteMaterialMst error", e);
			return "error: " + e.getMessage();
		}
	}
	
	// ======================================================
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
	
	// 라우트 활성 / 비활성화는 work_order에서 상태값과 routeHeaderId가 일치하는지 보고 가능 여부 판단
	
	// ---------------------------------------
	// 공정코드 전체 조회
	public List<ProcessMstDTO> getProcessCodeList() {
		return processMstRepository.findAll()
				.stream()
				.map(ProcessMstDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	// 공정 코드 활성화된 목록만 조회
	public List<ProcessMstDTO> getProcessCodeListWithUseYn(String useYnStr) {
		Character useYn = useYnStr.charAt(0);
		
		
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
}