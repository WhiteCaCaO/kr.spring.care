package kr.spring.care.senior_page.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import kr.spring.care.matching.constant.MatchingStatus;
import kr.spring.care.senior_page.dto.GuardianDTO;
import kr.spring.care.senior_page.dto.MatchingDTO;
import kr.spring.care.senior_page.dto.MatchingResponse;
import kr.spring.care.senior_page.dto.SeniorDTO;
import kr.spring.care.senior_page.service.SeniorPageService;
import kr.spring.care.user.entity.User;
import kr.spring.care.user_page.dto.UserDTO;
import kr.spring.care.user_page.service.UserPageService;
import lombok.RequiredArgsConstructor;

@RequestMapping("/m/seniorPage/*")
@RestController
@RequiredArgsConstructor
public class SeniorPageRestController {
	
	private final UserPageService userPageService;
	private final SeniorPageService seniorPageService; 
	
	// 기본사항(User) + 특정사항(Senior+guardian) 정보
	@GetMapping("seniorInfo/{id}")
	public ResponseEntity<UserDTO> myinfo(@PathVariable("id") long userId, Model model) {
		UserDTO userInfo = seniorPageService.myInfo(userId);
		SeniorDTO seniorInfo = seniorPageService.seniorInfo(userId);
		GuardianDTO guardianInfo = seniorPageService.guardianInfo(userId);
		
		userInfo.setRoleStr(userInfo.getRole().toString());
		userInfo.setSeniorId(seniorInfo.getSeniorId());
		userInfo.setHealth(seniorInfo.getHealth());
		userInfo.setRequirements(seniorInfo.getRequirements());
		userInfo.setHasGuardian(seniorInfo.getHasGuardian());
		
		if (guardianInfo == null) {
			guardianInfo = new GuardianDTO(); // GuardInfo는 당신의 객체 타입
		    // 필요하다면 기본값 설정
			guardianInfo.setGuardianName("");
			guardianInfo.setGuardianPhoneNumber("");
			guardianInfo.setRelationship("");
		} else {
			userInfo.setGuardianId(guardianInfo.getGuardianId());
			userInfo.setGuardianName(guardianInfo.getGuardianName());
			userInfo.setGuardianPhoneNumber(guardianInfo.getGuardianPhoneNumber());
			userInfo.setRelationship(guardianInfo.getRelationship());
		}
		return new ResponseEntity<UserDTO>(userInfo, HttpStatus.OK);
	}
	
	// 회원정보 수정
	@PutMapping("updateInfo")
	public ResponseEntity<String> edit(@RequestBody UserDTO userDTO) {
		seniorPageService.editUser(userDTO);
		return new ResponseEntity<>(userDTO.getEmail(), HttpStatus.OK);
	}
	
	// 비밀번호 변경
	@PutMapping("editPw")
	@ResponseBody
	public String editPw(@RequestBody User user) {
		userPageService.editPw(user);
		return "success";
	}
	
	// 매칭 정보
	@GetMapping("matchingInfo/{id}")
	public ResponseEntity<MatchingResponse> matchingInfo(@PathVariable long id) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	    
	    List<MatchingDTO> allMatchings = seniorPageService.matchingInfo(id);
	    
	    // 변환 작업을 여기에서 수행
	    List<MatchingDTO> formattedMatchings = allMatchings.stream().map(matching -> {
	        // LocalDate 형식의 날짜 필드를 문자열로 변환, null 체크
	        if (matching.getStartDate() != null) {
	            matching.setStartDateStr(matching.getStartDate().format(dateFormatter));
	        }
	        if (matching.getEndDate() != null) {
	            matching.setEndDateStr(matching.getEndDate().format(dateFormatter));
	        }
	        
	        // LocalTime 형식의 시간 필드를 문자열로 변환, null 체크
	        if (matching.getStartTime() != null) {
	            matching.setStartTimeStr(matching.getStartTime().format(timeFormatter));
	        }
	        if (matching.getEndTime() != null) {
	            matching.setEndTimeStr(matching.getEndTime().format(timeFormatter));
	        }
	        return matching; // 한 번만 반환
	    }).collect(Collectors.toList());
	    
	    List<MatchingDTO> filteredPastMatchings = formattedMatchings.stream()
	            .filter(m -> m.getStatus() == MatchingStatus.COMPLETED || m.getStatus() == MatchingStatus.CANCELLED)
	            .collect(Collectors.toList());

	    List<MatchingDTO> filteredProgressMatchings = formattedMatchings.stream()
	            .filter(m -> m.getStatus() == MatchingStatus.POSTED || m.getStatus() == MatchingStatus.REQUESTED || m.getStatus() == MatchingStatus.IN_PROGRESS)
	            .collect(Collectors.toList());
	    
	    MatchingResponse response = new MatchingResponse(filteredPastMatchings, filteredProgressMatchings);

	    return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	

	
	
	/*
	 * @GetMapping("matchingInfo/{id}") public String matchingInfo(@PathVariable
	 * long id ,Model model) { List<MatchingDTO> matchingInfo =
	 * seniorPageService.matchingInfo(id);
	 * 
	 * model.addAttribute("matchings", matchingInfo); model.addAttribute("isEmpty",
	 * matchingInfo.isEmpty()); matchingInfo.forEach(matching ->
	 * System.out.println(matching)); // List<CaregiverDTO> careInfo =
	 * seniorPageService.careInfo(); return "seniorPage/matchingInfo"; }
	 */
	
	
	
	
}
