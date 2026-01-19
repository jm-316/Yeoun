package com.yeoun.auth.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.yeoun.emp.entity.EmpRole;
import com.yeoun.emp.repository.EmpRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginDTO implements UserDetails {
	
	private String empId;
	
	@ToString.Exclude
	private String empPwd;
	
	private List<EmpRole> empRoles;
	
	// --------------------------------
	// 필요 정보 추가
	private String empName; // 직원이름
	private String deptId; // 직원 부서ID
	private String deptName;// 직원 부서명
    // 직급 정보 추가
    private String posCode;
    private String posName;
	// ------------------------------
	
	public boolean hasRole(String role) {
	    if (empRoles == null) return false;

	    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;

	    return getAuthorities().stream()
	            .anyMatch(auth -> auth.getAuthority().equals(roleName));
	}

	
	// ---------------------------------------------------
	// 필수 오버라이딩 메서드
	// 1) 사용자명(= 아이디 역할)을 리턴하는 메서드
	@Override
	public String getUsername() {
		return empId;
	}
	
	// 2) 사용자 패스워드를 리턴하는 메서드
	@Override
	public String getPassword() {
		return empPwd;
	}
	
	// 3) 사용자의 권한 목록을 리턴하는 메서드
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return empRoles.stream()
				.map(er -> new SimpleGrantedAuthority
						(er.getRole().getRoleCode()
				))
				.collect(Collectors.toList());
	}

	// --------------------------------------------
	// 선택적 오버라이딩 메서드
	// 4) 계정 만료 여부 리턴
	@Override
	public boolean isAccountNonExpired() {
		// 실제 계정 만료 여부 확인하는 서비스 로직 추가 필요
		// ex) memberRepository.isAccountNonExpired() 등의 메서드로 조회
		return true;
	}

	// 5) 계정 잠금 여부 리턴
	@Override
	public boolean isAccountNonLocked() {
		// 실제 계정 잠금 여부 확인하는 서비스 로직 추가 필요
		return true;
	}

	// 6) 패스워드 기간 만료 여부 리턴
	@Override
	public boolean isCredentialsNonExpired() {
		// 실제 패스워드 기간 만료 여부 확인하는 서비스 로직 추가 필요
		return true;
	}

	// 7) 계정 사용 가능(활성화) 여부 리턴
	@Override
	public boolean isEnabled() {
		// 실제 계정 활성화 여부 확인하는 서비스 로직 추가 필요
		return true;
	}
	

}
