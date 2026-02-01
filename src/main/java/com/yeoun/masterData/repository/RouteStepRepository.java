package com.yeoun.masterData.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;

public interface RouteStepRepository extends JpaRepository<RouteStep, String> {
	
	// 해당 라우트의 공정단계를 순서대로
	List<RouteStep> findByRouteHeaderOrderByStepSeqAsc(RouteHeader routeHeader);

	// 활성화된 ROUTE_HEADER에 포함된 ROUTE_STEP이 있는지 확인
	@Query("""
		    select count(rs) > 0
		    from RouteStep rs
		    join rs.routeHeader rh
		    where rs.process.processId = :processId
		      and rh.useYn = 'Y'
		""")                 
	boolean existsActiveRouteUsingProcess(@Param("processId") String processId);

	// 활성여부에 따라 ROUTE_STEP 조회
	@Query("""
		    select rs
		    from RouteStep rs
		    where rs.routeHeader.routeId = :routeId
		      and rs.useYn = :useYn
		    order by rs.stepSeq asc
		""")
	List<RouteStep> findByRouteIdAndUseYn(@Param("routeId") String routeId, @Param("useYn") String useYn);

	// ROUTE_STEP 조회
	@Query("""
		    select rs
		    from RouteStep rs
		    where rs.routeHeader.routeId = :routeId
		    order by rs.stepSeq asc
		""")
	List<RouteStep> findAllByRouteId(@Param("routeId") String routeId);

	// ROUTE_STEP ID로 조회
	List<RouteStep> findAllByRouteStepIdIn(List<String> stepIds);
}
