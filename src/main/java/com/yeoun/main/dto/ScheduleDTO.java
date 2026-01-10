package com.yeoun.main.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.yeoun.emp.dto.EmpDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.main.entity.Schedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduleDTO {
	private Long scheduleId; // 일정ID

	@NotBlank(message = "일정 제목은 필수 입력값입니다.")
	private String scheduleTitle; // 일정 제목
	
	@NotBlank(message = "일정 내용은 필수 입력값입니다.")
	private String scheduleContent; // 일정 내용
	
	@NotBlank(message = "일정 종류는 필수 입력값입니다.")
	private String scheduleType; // 일정 종류
	
	private String createdUser; // 일정 등록자
	
	private String alldayYN; // 종일일정구분
	
	@NotNull(message = "일정 시작시간은 필수 입력값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime scheduleStart; // 일정 시작시간
	
	@NotNull(message = "일정 마감시간은 필수 입력값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime scheduleFinish; // 일정 마치는시간
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime createdDate; // 등록 일시
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private LocalDateTime updatedDate; // 수정 일시
	
	private String empId;
	private String empName;
	private String deptId;
	private String deptName;
	
	// --------------------------------------------------
	// 반복일정 속성 추가
	@NotBlank
    private String repeatType = "NONE";        // NONE/DAILY/WEEKLY/...
    @Min(1)@Max(12)
    private Long repeatInterval = 0l;   // 1~12
    @Min(0)@Max(127)
    private Long repeatWeekdays = 0l;   // 0~127
    private String repeatEndDate;
	
	// ----------------------------------------------------------
	private static ModelMapper modelMapper = new ModelMapper();
	
	public Schedule toEntity() {
		Schedule schedule = modelMapper.map(this,  Schedule.class);
	    
	    if (this.getCreatedUser() != null) {
	        Emp emp = new Emp();
	        emp.setEmpId(this.getCreatedUser());

	        if (this.getDeptId() != null) {
	            Dept dept = new Dept();
	            dept.setDeptId(this.getDeptId());
	            emp.setDept(dept);
	        }
	        schedule.setEmp(emp);
	    }
	    
        if ("NONE".equals(repeatType)) {
        	schedule.setRepeatType(null);
        	schedule.setRepeatInterval(null);
        	schedule.setRepeatWeekdays(0l);
        	schedule.setRepeatEndDate(null);
        } else {
        	schedule.setRepeatType(repeatType);
        	schedule.setRepeatInterval(repeatInterval != null ? repeatInterval : 1);
        	schedule.setRepeatWeekdays(
                repeatType.equals("WEEKLY") ? (repeatWeekdays != null ? repeatWeekdays : 0) : 0
            );
            if (repeatEndDate != null && !repeatEndDate.isBlank()) {
            	LocalDate date = LocalDate.parse(repeatEndDate);
            	schedule.setRepeatEndDate(date.atTime(23, 59, 59));
            }
        }
	    
		return schedule; 
	}
	
	public static ScheduleDTO fromEntity(Schedule schedule) {
		ScheduleDTO scheduleDTO = modelMapper.map(schedule, ScheduleDTO.class);
		scheduleDTO.setCreatedUser(schedule.getEmp().getEmpId());
		scheduleDTO.setEmpId(schedule.getEmp().getEmpId());
		scheduleDTO.setEmpName(schedule.getEmp().getEmpName());
		scheduleDTO.setDeptId(schedule.getEmp().getDept().getDeptId());
		scheduleDTO.setDeptName(schedule.getEmp().getDept().getDeptName());
		return scheduleDTO;
	}
}
