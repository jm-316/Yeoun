package com.yeoun.qc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.qc.entity.QcResultDetail;

@Repository
public interface QcResultDetailRepository extends JpaRepository<QcResultDetail, String> {
	
	@Query("""
		    SELECT q
		    FROM QcResultDetail q
		    WHERE q.qcResultId = :qcResultId
		""")
	List<QcResultDetail> findByQcResultId(@Param("qcResultId") String QcResultId);

}
