package com.yeoun.masterData.dto;

import java.time.LocalDateTime;

import com.yeoun.masterData.entity.ProcessMst;
import com.yeoun.masterData.entity.RouteHeader;
import com.yeoun.masterData.entity.RouteStep;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RouteStepDTO {
	
	// 라우트 단계 ID
	@NotBlank(message = "라우트 단계 ID는 필수 입력값입니다.")
    private String routeStepId;
	
	// 라우트 ID
    @NotBlank(message = "라우트 ID는 필수 입력값입니다.")
    private String routeId;
	
	// 공정 순서
    @NotBlank(message = "공정순서는 필수 입력값입니다.")
    private Integer stepSeq;
	
	// 공정 ID (실행할 공정)
    @NotBlank(message = "공정 ID는 필수 입력값입니다.")
    private String processId;
	
	// QC 포인트 여부
	@NotBlank(message = "QC 포인트 여부는 필수 입력값입니다.")
    private String qcPointYn;
	
	// 비고
    private String remark;
    
    // 사용여부
    private String useYn;

	// 최초 등록자
	private String createdId;
	
	// 최초 등록일
	private LocalDateTime createdDate;
	
	// 수정자
	private String updatedId;
	
	// 수정일
	private LocalDateTime updatedDate;


	public static RouteStepDTO fromEntity(RouteStep routeStep) {
		return RouteStepDTO.builder()
				.routeStepId(routeStep.getRouteStepId())
				.routeId(routeStep.getRouteHeader().getRouteId())
				.processId(routeStep.getProcess().getProcessId())
				.qcPointYn(routeStep.getQcPointYn())
				.stepSeq(routeStep.getStepSeq())
				.remark(routeStep.getRemark())
				.useYn(routeStep.getUseYn())
				.build();
	}
}
