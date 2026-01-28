package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.QcItem;

@Repository
public interface QcItemRepository extends JpaRepository<QcItem, String> {

    //품질 항목 기준 qcId 목록 조회
    @Query(value = """
        SELECT DISTINCT QC_ITEM_ID 
        FROM QC_ITEM ORDER BY QC_ITEM_ID ASC
        """, nativeQuery = true)
    List<String> qcIdList();
    
	// 대상구분 + 사용여부 기준으로 항목 조회
    List<QcItem> findByTargetTypeAndUseYnOrderBySortOrderAsc(String targetType, String useYn);
    
    // QC ITEM 상세 조회
	Optional<QcItem> findByQcItemId(String qcItemId);
	
	// QC ITEM ID 중복 검사
	boolean existsByQcItemId(String qcItemId);
    
}
