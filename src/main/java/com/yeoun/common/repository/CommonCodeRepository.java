package com.yeoun.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.common.entity.CommonCode;

@Repository
public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {
	
	// 특정 코드 그룹의 하위 코드 목록 조회
	List<CommonCode> findByParentCodeIdAndUseYnOrderByCodeSeq(String parentCodeId, String useYn);
	
	// 특정 코드 그룹의 하위 코드 목록 조회

	List<CommonCode> findByParentCodeId(String parentCodeId);
}
