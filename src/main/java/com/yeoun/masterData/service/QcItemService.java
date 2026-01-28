package com.yeoun.masterData.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.common.repository.CommonCodeRepository;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.masterData.dto.QcItemDTO;
import com.yeoun.masterData.entity.QcItem;
import com.yeoun.masterData.repository.MaterialRepository;
import com.yeoun.masterData.repository.QcItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class QcItemService {
	private final QcItemRepository qcItemRepository;
	private final MaterialRepository materialRepository;
	private final CommonCodeRepository commonCodeRepository;
	private final EmpRepository empRepository;
//	private final MaterialMstRepository materialMstRepository;
	
	//품질 항목 기준 qcId 목록 조회 (distinct)
	public List<String> qcIdList() {
		return qcItemRepository.qcIdList();
	}
	
	//대상구분 드롭다운
	public List<CommonCodeIdAndNameDTO> targetTypeList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("MAT_TYPE", "Y")
				.stream()
				.map(CommonCodeIdAndNameDTO::fromEntity)
				.collect(Collectors.toList());
	}
	
	//품질 단위 드롭다운
	public List<CommonCodeIdAndNameDTO> unitTypeList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("QCITEM_UNIT", "Y")
				.stream()
				.map(CommonCodeIdAndNameDTO::fromEntity)
				.collect(Collectors.toList());
	}

	//품질 항목 기준 조회
//	public List<Map<String, Object>> qcItemList(String qcItemId) {
//		return qcItemRepository.findByQcItemList(qcItemId);
//	}
	
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
	@Transactional
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
	@Transactional
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
