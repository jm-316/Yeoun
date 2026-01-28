package com.yeoun.masterData.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.common.entity.CommonCode;
import com.yeoun.common.repository.CommonCodeRepository;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.masterData.dto.QcItemDTO;
import com.yeoun.masterData.entity.QcItem;
import com.yeoun.masterData.repository.MaterialMstRepository;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.QcItemRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class QcItemService {
	private final QcItemRepository qcItemRepository;
	private final MaterialRepository materialRepository;
	private final CommonCodeRepository commonCodeRepository;
	private final EmpRepository empRepository;
//	private final MaterialMstRepository materialMstRepository;
	
	//품질 항목 기준 qcId 목록 조회 (distinct)
	@Transactional(readOnly = true)
	public List<String> qcIdList() {
		return qcItemRepository.qcIdList();
	}
	
	//대상구분 드롭다운
	@Transactional
	public List<CommonCodeIdAndNameDTO> targetTypeList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("MAT_TYPE", "Y")
				.stream()
				.map(CommonCodeIdAndNameDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	//품질 단위 드롭다운
	@Transactional(readOnly = true)
	public List<CommonCodeIdAndNameDTO> unitTypeList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("QCITEM_UNIT", "Y")
				.stream()
				.map(CommonCodeIdAndNameDTO::fromEntity)
				.collect(Collectors.toList());
	}

	//품질 항목 기준 조회
	@Transactional(readOnly = true)
	public List<Map<String, Object>> qcItemList(String qcItemId) {
		return qcItemRepository.findByQcItemList(qcItemId);
	}
	//품질 항목 기준 저장
	@Transactional
	public String saveQcItem(String empId, Map<String,Object> param) {
		log.info("qcItem param: {}", param);
		
		// null 체크
		if(param == null || param.get("mode") == null || param.get("qcItemId") == null) {
			return "error: 필수 데이터가 누락되었습니다.";
		}
		
		String mode = param.get("mode").toString().trim();
		String qcItemId = param.get("qcItemId").toString().trim();
		
		// 빈 문자열 체크
		if(mode.isEmpty() || qcItemId.isEmpty()) {
			return "error: mode 또는 qcItemId가 비어있습니다.";
		}

		try {
			QcItem qcItem = new QcItem();;
			
			if("new".equals(mode)) {
				log.info("------------------new----------------------------------");
				
				// 신규 등록 - 중복검사
				if(qcItemRepository.existsById(qcItemId)) {
					return "error: 중복되는 QC 항목 ID가 이미 존재합니다. (qcItemId=" + qcItemId + ")";
				}
				
				qcItem.setQcItemId(qcItemId);
				qcItem.setCreatedId(empId);
				qcItem.setCreatedDate(LocalDate.now());
				qcItem.setUseYn("Y");
				
			} else if("modify".equals(mode)) {
				log.info("------------------modify----------------------------------");

				// 수정 - 기존 데이터 가져와서 수정
				Optional<QcItem> existingItem = qcItemRepository.findById(qcItemId);
				if(!existingItem.isPresent()) {
					return "error: 수정할 QC 항목을 찾을 수 없습니다.";
				}
				
				qcItem = existingItem.get();
				// 수정 정보 설정
				qcItem.setUpdatedId(empId);
				qcItem.setUpdatedDate(LocalDate.now());
				
			} else {
				return "error: 잘못된 모드입니다. mode=" + mode;
			}


			// 필드 설정 (null 체크만, 빈 값도 저장)
			if(param.get("itemName") != null) {
				qcItem.setItemName(param.get("itemName").toString());
			}
			if(param.get("targetType") != null) {
				qcItem.setTargetType(param.get("targetType").toString());
			}
			if(param.get("unit") != null) {
				qcItem.setUnit(param.get("unit").toString());
			}
			if(param.get("stdText") != null) {
				qcItem.setStdText(param.get("stdText").toString());
			}
			
			// minValue 처리
			if(param.get("minValue") != null && !param.get("minValue").toString().trim().isEmpty()) {
				try {
					qcItem.setMinValue(new java.math.BigDecimal(param.get("minValue").toString()));
				} catch(NumberFormatException e) {
					qcItem.setMinValue(null);
				}
			} else {
				qcItem.setMinValue(null);
			}
			
			// maxValue 처리
			if(param.get("maxValue") != null && !param.get("maxValue").toString().trim().isEmpty()) {
				try {
					qcItem.setMaxValue(new java.math.BigDecimal(param.get("maxValue").toString()));
				} catch(NumberFormatException e) {
					qcItem.setMaxValue(null);
				}
			} else {
				qcItem.setMaxValue(null);
			}
			
			// sortOrder 처리 (빈 값이면 기본값 1)
			if(param.get("sortOrder") != null && !param.get("sortOrder").toString().trim().isEmpty()) {
				try {
					qcItem.setSortOrder(Integer.parseInt(param.get("sortOrder").toString()));
				} catch(NumberFormatException e) {
					qcItem.setSortOrder(1);
				}
			} else {
				qcItem.setSortOrder(1);
			}
		
		
			// DB 저장
			qcItemRepository.save(qcItem);
			return "success";
			
		} catch(Exception e) {
			log.error("saveQcItem error", e);
			return "error: " + e.getMessage();
		}
	}
	// 품질 항목 기준 삭제 (사용여부 N으로 변경)
	@Transactional
	public String deleteQcItem(List<String> qcItemIds) {
		log.info("qcItemRepository deleteQcItem------------->{}", qcItemIds);
		try {
			// 사용여부를 'N'으로 변경 (soft delete)
			for(String qcItemId : qcItemIds) {
				Optional<QcItem> optionalQcItem = qcItemRepository.findById(qcItemId);
				if(optionalQcItem.isPresent()) {
					QcItem qcItem = optionalQcItem.get();
					qcItem.setUseYn("N");
					qcItem.setUpdatedDate(LocalDate.now());
					qcItemRepository.save(qcItem);
				} else {
					log.warn("삭제하려는 QC 항목을 찾을 수 없습니다: {}", qcItemId);
				}
			}
			// 즉시 flush 하여 DB 업데이트 확실히 적용
			qcItemRepository.flush();
			return "success";
		} catch (Exception e) {
			log.error("deleteQcItem error", e);
			return "error: " + e.getMessage();
		}
	}

	// QC ITEM 조회(전체)
	public List<QcItemDTO> getAllQcItem() {
		return qcItemRepository.findAll()
				.stream()
				.map(QcItemDTO::fromEntity)
				.collect(Collectors.toList());
	}

	// QC ITEM 상세 조회
	public QcItemDTO findQcItem(String qcItemId) {
		QcItem qcItem = qcItemRepository.findByQcItemId(qcItemId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QC입니다."));
		
		return QcItemDTO.fromEntity(qcItem);
	}

	// QC ITEM 신규 등록
	public void insertQcItem(QcItemDTO data, String empId) {
		Emp emp = empRepository.findByEmpId(empId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 입니다."));
		
		String qcItemId = data.getQcItemId();
		
		qcItemId = normalizeQcItemId(qcItemId);
		
		QcItem qcItem = data.toEntity();
		
		// string으로 받은 minValue와 maxValue의 타입 변경
		qcItem.toBigDeciaml(data.getMinValue(), data.getMaxValue());
		
		qcItem.setQcItemId(qcItemId);
		qcItem.setCreatedId(emp.getEmpId());
		
		qcItemRepository.save(qcItem);
	}
	
	// QC ITEM 수정
	public void updateQcItem(String qcItemId, QcItemDTO data, String empId) {
		QcItem qcItem = qcItemRepository.findByQcItemId(qcItemId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 QC입니다."));
		
		// 업데이트 한 사람의 emdpId 저장
		data.setUpdateId(empId);
		
		qcItem.updateQcItem(data);
	}

	// QC ITEM ID 중복 검사
	public boolean existsByQcItemId(String qcItemId) {
		qcItemId = normalizeQcItemId(qcItemId);
		return qcItemRepository.existsByQcItemId(qcItemId);
	}
	
	private String normalizeQcItemId(String qcItemId) {
		if (qcItemId == null || qcItemId.isBlank()) {
			 throw new IllegalArgumentException("QC Item ID는 필수입니다.");
		}
		
		return "QC-" + qcItemId;
	}
}
