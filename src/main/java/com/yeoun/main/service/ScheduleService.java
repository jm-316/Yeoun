package com.yeoun.main.service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.yeoun.auth.dto.LoginDTO;
import com.yeoun.common.dto.AlarmDTO;
import com.yeoun.common.service.AlarmService;
import com.yeoun.emp.dto.DeptDTO;
import com.yeoun.emp.entity.Dept;
import com.yeoun.emp.entity.Emp;
import com.yeoun.emp.repository.DeptRepository;
import com.yeoun.emp.repository.EmpRepository;
import com.yeoun.leave.dto.LeaveDTO;
import com.yeoun.leave.dto.LeaveHistoryDTO;
import com.yeoun.leave.entity.AnnualLeaveHistory;
import com.yeoun.leave.repository.LeaveHistoryRepository;
import com.yeoun.main.dto.ScheduleDTO;
import com.yeoun.main.dto.ScheduleSharerDTO;
import com.yeoun.main.entity.Schedule;
import com.yeoun.main.entity.ScheduleSharer;
import com.yeoun.main.mapper.ScheduleMapper;
import com.yeoun.main.repository.ScheduleRepository;
import com.yeoun.main.repository.ScheduleSharerRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final DeptRepository deptRepository;
	private final EmpRepository empRepository;
	private final LeaveHistoryRepository leaveHistoryRepository;
	private final ScheduleMapper scheduleMapper;
	private final ScheduleSharerRepository scheduleSharerRepository;
	private final AlarmService alarmService;
	// --------------------------------------------------
	
	//일정 등록모달 부서리스트 가져오기
	public List<DeptDTO> getDeptList() {
		List<Dept> deptList = deptRepository.findAll();
		
		return deptList.stream() //부서리스트를 엔터티 -> dto리스트로 변경후 리턴
				.map(dept -> DeptDTO.fromEntity(dept))
				.collect(Collectors.toList());
	}
	
	// 일정 등록로직
	@Transactional
	public void createSchedule(@Valid ScheduleDTO scheduleDTO, List<ScheduleSharerDTO> list) {
		Emp emp = empRepository.findById(scheduleDTO.getCreatedUser()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다.111"));

		Schedule schedule = scheduleDTO.toEntity();
		schedule.setEmp(emp);
		String scheduleTitle =schedule.getScheduleTitle();
		// 여기서 Schedule테이블 정보 저장
		scheduleRepository.save(schedule);
		
		// Schedule테이블에 저장된 정보를 토대로 ScheduleSharer테이블에 정보저장
		for(ScheduleSharerDTO DTO : list) {
			// save할 객체 생성
			ScheduleSharer scheduleSharer = new ScheduleSharer();
			// sharer에 공유된 empId로 emp객체 찾기
			Emp sharerEmp = empRepository.findById(DTO.getEmpId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다.111"));;
			
			// scheduleSharer엔티티에 schedule객체, emp 객체 추가 
			scheduleSharer.setSchedule(schedule);
			scheduleSharer.setSharedEmp(sharerEmp);
			// 엔티티 값 저장
			scheduleSharerRepository.save(scheduleSharer);
			
			// 공유일정 공유자들에게 알림 설정
			String alarmMessage = """
			        새로운 공유 일정이 등록되었습니다.['%s']
					""".formatted(scheduleTitle);
			
			AlarmDTO alarmDTO = AlarmDTO.builder()
					.empId(sharerEmp.getEmpId())
					.alarmMessage(alarmMessage)
					.alarmStatus("N")
					.alarmLink("/main")
					.build();
			alarmService.sendPersonalMessage(alarmDTO);
		}
		
		
	}
	
	// 일정 목록 조회로직
	public List<ScheduleDTO> getScheduleList(LocalDateTime startDate, LocalDateTime endDate, Authentication authentication) {
		String empId = authentication.getName();
		// empId로 로그인한 emp엔티티 정보 조회
		Emp emp = empRepository.findById(empId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다.111"));
		String myDeptId = emp.getDept().getDeptId();
		String myDeptName = emp.getDept().getDeptName();
		
	    LocalDateTime startOfDay = startDate.with(java.time.LocalTime.MIN);//해당일자의 00시00분00초  
	    LocalDateTime endOfDay   = endDate.with(java.time.LocalTime.MAX);//해당일자의 23시59분59초
	    
		// 일정목록 조회(반복일정이 아닌일정만 조회)
	    List<Schedule> scheduleList = scheduleRepository.getIndividualSchedule(empId, startOfDay, endOfDay);
	    
	    // 반복일정 조회후 일정에 맞게 변환로직 추가
	    List<Schedule> repeatScheduleList = scheduleRepository.getRepeatScheduleList(empId, startOfDay);
	    
	    // 조회한 목록을 리턴할 result객체 생성
	    List<ScheduleDTO> result = new ArrayList<>();
	    
	    // result에 단발일정 추가
	    for (Schedule s : scheduleList) {
	    	result.add(ScheduleDTO.fromEntity(s));
	    }
	    
	    // 반복 일정은 viewStart~viewEnd 안에서 전개
	    LocalDate viewStart = startOfDay.toLocalDate();
	    LocalDate viewEnd   = endOfDay.toLocalDate();
	    
	    for (Schedule s : repeatScheduleList) {
	        result.addAll(expandRepeatSchedule(s, viewStart, viewEnd));
	    }
//		System.out.println(result + "ㅇㅇㅇㅇㅇㅇㅇㅇㅇ");
		return result;
	}
	
	/**
	 * 반복 일정을 조회 기간(viewStart~viewEnd)에 맞춰
	 * 개별 일정 리스트로 전개한다.
	 *
	 * - DAILY  : 매 N일마다
	 * - WEEKLY : 매 N주마다 + 요일 비트 플래그(repeatWeekdays)
	 * - MONTHLY: 매 N개월마다, 기준 시작일의 '일(dayOfMonth)' 기준
	 * - YEARLY : 매 N년마다, 기준 시작일의 '월/일' 기준
	 *
	 * @param s         반복 규칙이 설정된 원본 일정 엔티티
	 * @param viewStart 캘린더에서 보고 있는 시작일 (YYYY-MM-DD)
	 * @param viewEnd   캘린더에서 보고 있는 종료일 (YYYY-MM-DD)
	 * @return 조회 기간 안에 실제로 표시될 반복 일정 인스턴스들의 DTO 리스트
	 */
	private List<ScheduleDTO> expandRepeatSchedule(Schedule s, LocalDate viewStart, LocalDate viewEnd) {		// TODO Auto-generated method stub
		List<ScheduleDTO> list = new ArrayList<>();
		
	    // 반복 종료일(없으면 매우 뒤 날짜로 대체해서 사실상 무한 반복처럼 처리)
	    LocalDate repeatEnd = s.getRepeatEndDate() != null
	            ? s.getRepeatEndDate().toLocalDate()
	            : LocalDate.of(2100, 12, 31);
	    
	    // 기준 시작일(원본 일정의 날짜 부분)
	    LocalDate baseStart = s.getScheduleStart().toLocalDate();
	    
	    // 실제 반복 계산 시작/끝 범위
	    LocalDate start = baseStart.isAfter(viewStart) ? baseStart : viewStart;
	    LocalDate end   = repeatEnd.isBefore(viewEnd) ? repeatEnd : viewEnd;
	    
	    
	    // 원본 일정의 시간 부분(시/분/초)은 그대로 유지
	    LocalTime originStartTime = s.getScheduleStart().toLocalTime();
	    LocalTime originEndTime   = s.getScheduleFinish().toLocalTime();
	    
	    // 반복타입, 인터벌 변수 저장
	    String type = s.getRepeatType();
	    int interval = s.getRepeatInterval() != null ? s.getRepeatInterval().intValue() : 1;
	    
	    // DAILY 매 N일마다
	    if ("DAILY".equalsIgnoreCase(type)) {
	    	// 시작일에서 매일 하루 추가 반복 마감일이 지나지 않을때 까지
	        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
	            long daysBetween = ChronoUnit.DAYS.between(baseStart, d);
	            // 기준 시작 이전 날짜는 스킵
	            if (daysBetween < 0) continue;
	            // interval 에 맞는 날만 생성
	            if (daysBetween % interval != 0) continue;

	            LocalDateTime startDateTime = d.atTime(originStartTime);
	            LocalDateTime endDateTime   = d.atTime(originEndTime);
	            
	            // 원본 정보 복사
	            ScheduleDTO dto = ScheduleDTO.fromEntity(s);
	            dto.setScheduleStart(startDateTime);
	            dto.setScheduleFinish(endDateTime);
	            // list추가
	            list.add(dto);
	        }
	    } else if ("WEEKLY".equalsIgnoreCase(type)) {
	    	// 설정된 요일 정보 가져오기
	        long mask = s.getRepeatWeekdays() != null ? s.getRepeatWeekdays() : 0L;
	        // 시작일 ~ 마감일 반복
	        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
	        	// 반복되는 날의 요일index : 일 0 월 1 ... 토 6 
	            int dayIndex = d.getDayOfWeek().getValue() % 7; 
	            long flag = 1L << dayIndex;
	            
	            // 비트 플래그에 포함된 요일이 아니면 스킵
	            if ((mask & flag) == 0) continue;
	            
	            // 기준 시작일로부터 몇 주 지났는지 계산해서 interval 체크
	            long weeksBetween = ChronoUnit.WEEKS.between(baseStart, d);
	            if (weeksBetween < 0) continue;
	            if (weeksBetween % interval != 0) continue;

	            LocalDateTime startDateTime = d.atTime(originStartTime);
	            LocalDateTime endDateTime   = d.atTime(originEndTime);

	            ScheduleDTO dto = ScheduleDTO.fromEntity(s);
	            dto.setScheduleStart(startDateTime);
	            dto.setScheduleFinish(endDateTime);
	            list.add(dto);
	        }
        //MONTHLY (매 N개월마다, 같은 일자 기준)
        // 예: 10월 31일이면 매 N개월마다 31일에 반복 (해당 달에 31일이 없으면 스킵)
	    } else if ("MONTHLY".equalsIgnoreCase(type)) {

	        // 기준: baseStart 의 '일(dayOfMonth)' 기준으로 반복
	        int baseDayOfMonth = baseStart.getDayOfMonth();

	        // 반복을 계산할 기준 날짜: baseStart 와 viewStart 중 큰 쪽에서 시작
	        LocalDate cursor = baseStart;
	        
	        //  viewStart 보다 이전이면, viewStart 기준으로 interval 에 맞게 cursor 이동
	        if (cursor.isBefore(start)) {
	            // viewStart 기준으로 몇 달 차이인지 구해서 interval 에 맞게 올림
	            long monthsDiff = ChronoUnit.MONTHS.between(baseStart.withDayOfMonth(1), start.withDayOfMonth(1));
	            long offset = (monthsDiff / interval) * interval;
	            cursor = baseStart.plusMonths(offset);
	            while (cursor.isBefore(start)) {
	                cursor = cursor.plusMonths(interval);
	            }
	        }
	        
	        // cursor 를 interval 만큼 증가시키면서 viewEnd / repeatEnd 범위 내에서 반복 생성
	        while (!cursor.isAfter(end) && !cursor.isAfter(repeatEnd)) {
	            // 해당 달에 baseDayOfMonth 가 존재하는지 확인 (2월 30일 같은 케이스 방지)
	            int lengthOfMonth = cursor.lengthOfMonth();
	            if (baseDayOfMonth <= lengthOfMonth) {
	                LocalDate d = cursor.withDayOfMonth(baseDayOfMonth);
	                if (!d.isBefore(start) && !d.isAfter(end)) {
	                    LocalDateTime startDateTime = d.atTime(originStartTime);
	                    LocalDateTime endDateTime   = d.atTime(originEndTime);

	                    ScheduleDTO dto = ScheduleDTO.fromEntity(s);
	                    dto.setScheduleStart(startDateTime);
	                    dto.setScheduleFinish(endDateTime);
	                    list.add(dto);
	                }
	            }
	            cursor = cursor.plusMonths(interval);
	        }

	    // YEARLY : 매 N년마다, 기준 시작일의 '월/일'로 반복
	    } else if ("YEARLY".equalsIgnoreCase(type)) {

	        int baseMonth = baseStart.getMonthValue();
	        int baseDay   = baseStart.getDayOfMonth();

	        LocalDate cursor = baseStart;
	        
	        // viewStart 이전이면, viewStart 기준으로 interval 에 맞게 cursor 이동
	        if (cursor.isBefore(start)) {
	            long yearsDiff = ChronoUnit.YEARS.between(baseStart.withDayOfYear(1), start.withDayOfYear(1));
	            long offset = (yearsDiff / interval) * interval;
	            cursor = baseStart.plusYears(offset);
	            while (cursor.isBefore(start)) {
	                cursor = cursor.plusYears(interval);
	            }
	        }
	        
	        // cursor 를 interval 년씩 증가시키면서 해당 연도의 같은 월/일에 인스턴스 생성
	        while (!cursor.isAfter(end) && !cursor.isAfter(repeatEnd)) {
	            LocalDate d;
	            try {
	                d = LocalDate.of(cursor.getYear(), baseMonth, baseDay); // 윤년/월 일자 검증
	            } catch (DateTimeException e) {
	                // 2/29 같은 경우, YEARLY 에서 해당 연도에 존재하지 않으면 스킵
	                cursor = cursor.plusYears(interval);
	                continue;
	            }

	            if (!d.isBefore(start) && !d.isAfter(end)) {
	                LocalDateTime startDateTime = d.atTime(originStartTime);
	                LocalDateTime endDateTime   = d.atTime(originEndTime);

	                ScheduleDTO dto = ScheduleDTO.fromEntity(s);
	                dto.setScheduleStart(startDateTime);
	                dto.setScheduleFinish(endDateTime);
	                list.add(dto);
	            }

	            cursor = cursor.plusYears(interval);
	        }
	    }
	    
	    return list;
	}

// ----------------------------------------------------------------------------------------------------------------------
	
	
	//단일 일정 조회
	public ScheduleDTO getSchedule(Long scheduleId) {
		Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 일정입니다."));
		
		return ScheduleDTO.fromEntity(schedule);
	}

	//일정 정보 수정
	@Transactional
	public void modifySchedule(@Valid ScheduleDTO scheduleDTO, List<ScheduleSharerDTO> list) {
		// 입력된 스케줄id로 기존 스케줄로우 정보 받아오기
		Schedule schedule = scheduleRepository.findById(scheduleDTO.getScheduleId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 일정입니다."));
		
		String scheduleTitle = schedule.getScheduleTitle();
		
		// 반복일정이 없을경우 interval 0으로설정
		if("NONE".equals(scheduleDTO.getRepeatType())) {
			scheduleDTO.setRepeatInterval(0l);
		}
		// changeSchedule 메서드 사용해서 수정된 정보 저장
		schedule.changeSchedule(scheduleDTO);
		
		// scheduleDTO의 scheduleType이 "share"일 경우 기존의 공유자리스트 제거후 다시 저장
		if("share".equals(scheduleDTO.getScheduleType())) {
			// 기존의 공유자목록 조회
			List<ScheduleSharer> sharers = scheduleSharerRepository.findBySchedule_ScheduleId(scheduleDTO.getScheduleId());
			// 이전 정보 삭제
			scheduleSharerRepository.deleteAll(sharers);
			// Schedule테이블에 저장된 정보를 토대로 ScheduleSharer테이블에 정보저장
			for(ScheduleSharerDTO DTO : list) {
				// save할 객체 생성
				ScheduleSharer scheduleSharer = new ScheduleSharer();
				// sharer에 공유된 empId로 emp객체 찾기
				Emp sharerEmp = empRepository.findById(DTO.getEmpId()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 직원입니다."));;
				
				// scheduleSharer엔티티에 schedule객체, emp 객체 추가 
				scheduleSharer.setSchedule(schedule);
				scheduleSharer.setSharedEmp(sharerEmp);
				// 엔티티 값 저장
				scheduleSharerRepository.save(scheduleSharer);
				
				// 공유일정 공유자들에게 알림 설정
				String alarmMessage = """
				        새로운 공유 일정이 등록되었습니다.['%s']
						""".formatted(scheduleTitle);
				
				AlarmDTO alarmDTO = AlarmDTO.builder()
						.empId(sharerEmp.getEmpId())
						.alarmMessage(alarmMessage)
						.alarmStatus("N")
						.alarmLink("/main")
						.build();
				alarmService.sendPersonalMessage(alarmDTO);
			}
		}
	}
	
	//일정 정보 삭제
	@Transactional
	public void deleteSchedule(@Valid ScheduleDTO scheduleDTO, Authentication authentication) {
		Schedule schedule = scheduleRepository.findById(scheduleDTO.getScheduleId()).orElseThrow(() -> new EntityNotFoundException("존재하지않는 일정입니다!!"));
		
		scheduleRepository.delete(schedule);
	}
	
	//startDate, endDate의 연차정보 가져오기
	public List<LeaveHistoryDTO> getLeaveHistoryList(LocalDateTime startDateTime, LocalDateTime endDateTime,
			LoginDTO loginDTO) {
		
		String empId = loginDTO.getEmpId();
		String deptId = loginDTO.getDeptId();
		
		LocalDate startDate = startDateTime.toLocalDate();
		LocalDate endDate = endDateTime.toLocalDate();
		List<AnnualLeaveHistory> leaveHistoryList = leaveHistoryRepository.findLeaveHistorySchedule(startDate, endDate, empId, deptId);
		return leaveHistoryList.stream().map(LeaveHistoryDTO::fromEntity).collect(Collectors.toList());
	}

	public List<Map<String, Object>> getOrganizationList() {
		
		List<Map<String, Object>> organizationList = scheduleMapper.getOrganizationList();
		
		return organizationList;
	}


}

































