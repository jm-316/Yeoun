package com.yeoun.masterData.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yeoun.masterData.entity.RouteHeader;

@Repository
public interface RouteHeaderRepository extends JpaRepository<RouteHeader, String> {

	// routeHeader 조회
	@Query("""
			select r
			from RouteHeader r
			join fetch r.product
			""")
	List<RouteHeader> findAllWithProduct();


	@Query("""
			select r
			from RouteHeader r
			join fetch r.product
			where r.routeId = :routeId
			""")
	Optional<RouteHeader> findByRouteId(@Param("routeId") String routeId);
}
