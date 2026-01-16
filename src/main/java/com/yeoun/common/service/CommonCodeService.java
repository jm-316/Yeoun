package com.yeoun.common.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yeoun.common.dto.CommonCodeDTO;
import com.yeoun.common.dto.CommonCodeIdAndNameDTO;
import com.yeoun.common.entity.CommonCode;
import com.yeoun.common.repository.CommonCodeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonCodeService {
	
	private final CommonCodeRepository commonCodeRepository;
	
	// ------------ 인사 관리 -------------
	// 재직 상태
	public List<CommonCode> getCodes(String parentCodeId) {
	    return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq(parentCodeId, "Y");
	}
	
	// 은행 코드 목록 조회
	public List<CommonCode> getBankList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("BANK", "Y");
	}
	
	// 인사 발령 유형 조회
	public List<CommonCode> getHrActionTypeList() {
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq("HR_ACTION_TYPE", "Y");
	}

	
	// ------------ 근태 관리 -------------
	// 연차 산정 기준 조회
	public List<CommonCodeDTO> getAnnualBasis(String parentCodeId) {
		List<CommonCode> codeList = commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq(parentCodeId, "Y");
		
		if (codeList.isEmpty()) {
			throw new NoSuchElementException("해당하는 코드가 없습니다.");
		}
		
		return codeList.stream()
				.map(CommonCodeDTO::fromEntity)
				.collect(Collectors.toList());
	}

	// 원재료 유형
	public List<CommonCodeIdAndNameDTO> getMatType(String parentCodeId) {
	
		return commonCodeRepository.findByParentCodeIdAndUseYnOrderByCodeSeq(parentCodeId, "Y")
				.stream()
				.map(CommonCodeIdAndNameDTO::fromEntity)
				.collect(Collectors.toList());
	}
	

	

}
