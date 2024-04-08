package kr.spring.care.senior_page.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.care.admin.DTO.CaregiverDTO;
import kr.spring.care.caregiver_page.repository.CaregiverPageRepository;
import kr.spring.care.matching.entity.Matching;
import kr.spring.care.matching.repository.MatchingRepository;
import kr.spring.care.senior_page.dto.GuardianDTO;
import kr.spring.care.senior_page.dto.MatchingDTO;
import kr.spring.care.senior_page.dto.SeniorDTO;
import kr.spring.care.senior_page.repository.GuardianPageRepository;
import kr.spring.care.senior_page.repository.SeniorPageRepository;
import kr.spring.care.user.entity.Caregiver;
import kr.spring.care.user.entity.Guardian;
import kr.spring.care.user.entity.Senior;
import kr.spring.care.user.entity.User;
import kr.spring.care.user_page.dto.UserDTO;
import kr.spring.care.user_page.repository.UserPageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeniorPageServiceImpl implements SeniorPageService {

	private final UserPageRepository userPageRepository;
	private final SeniorPageRepository seniorPageRepository;
	private final CaregiverPageRepository caregiverPageRepository;
	private final GuardianPageRepository guardianPageRepository; 
	private final MatchingRepository matchingRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDTO myInfo(long userId) {
		Optional<User> userOptional = userPageRepository.findById(userId);
		return userOptional.map(UserDTO::new).orElse(null);
	}

	@Override
	public SeniorDTO seniorInfo(long userId) {
		return seniorPageRepository.findByUserUserId(userId).map(SeniorDTO::new) // Optional<Senior>를 Optional<SeniorDTO>로 변환
				.orElse(null); // 값이 없는 경우 null 반환
	}
	
	@Override
	public GuardianDTO guardianInfo(long userId) {
		Optional<Guardian> guardianOptional = guardianPageRepository.findByUser_UserId(userId);
		return guardianOptional.map(GuardianDTO::new).orElse(null);
	}

	@Override
	@Transactional
	public void editUser(UserDTO userDTO) {
		User bfUser = userPageRepository.findById(userDTO.getUserId())
				.orElseThrow(() -> new NoSuchElementException("해당 user 를 찾을 수 없습니다."));
		// 사용자 정보 업데이트 로직...
		bfUser.setAddress(userDTO.getAddress());
		bfUser.setGender(userDTO.getGender());
		bfUser.setName(userDTO.getName());
		bfUser.setPhoneNumber(userDTO.getPhoneNumber());
		
		Optional<Senior> optionalSenior = seniorPageRepository.findByUserUserId(userDTO.getUserId());
		if(optionalSenior.isPresent()) {
			Senior bfSenior = optionalSenior.get();
			bfSenior.setHasGuardian(userDTO.getHasGuardian());
			bfSenior.setHealth(userDTO.getHealth());
			bfSenior.setRequirements(userDTO.getRequirements());
			bfUser.setSenior(bfSenior);
		}else {
			Senior senior = new Senior();
			senior.setHasGuardian(userDTO.getHasGuardian());
			senior.setHealth(userDTO.getHealth());
			senior.setRequirements(userDTO.getRequirements());
			senior.setUser(bfUser);
			
			seniorPageRepository.save(senior);
		}
	
		// 보호자 정보가 없는 경우를 처리하기 위한 수정
		// 보호자가 null 일때 insert
	    Optional<Guardian> optionalGuardian = guardianPageRepository.findByUser_UserId(userDTO.getUserId());
	    if (optionalGuardian.isPresent()) {
	        Guardian bfGuard = optionalGuardian.get();
	        
	        bfGuard.setGuardianName(userDTO.getGuardianName());
	        bfGuard.setGuardianPhoneNumber(userDTO.getGuardianPhoneNumber());
	        bfGuard.setRelationship(userDTO.getRelationship());
	        bfUser.setGuardian(bfGuard);
	    }else {
	    	Guardian guard = new Guardian();
	    	guard.setGuardianName(userDTO.getGuardianName());
	    	guard.setGuardianPhoneNumber(userDTO.getGuardianPhoneNumber());
	    	guard.setRelationship(userDTO.getRelationship());
	    	guard.setUser(bfUser);
	    	guard.setSenior(optionalSenior.get());
	    	
	    	guardianPageRepository.save(guard);
	    }
	    // 없을 경우에는 보호자 정보를 업데이트하지 않음
	    userPageRepository.save(bfUser);
		
	}

	@Override
	@Transactional
	public void editPw(User user) {
		User bfUser = userPageRepository.findById(user.getUserId()).get();
		String afPw = passwordEncoder.encode(user.getPassword());
		bfUser.setPassword(afPw);
		userPageRepository.save(bfUser);
	}

	@Override
	public List<MatchingDTO> matchingInfo(long userId) {
		Senior senior = seniorPageRepository.findByUserUserId(userId)
				.orElseThrow(() -> new NoSuchElementException("Senior not found"));

		List<Matching> matchings = matchingRepository.findBySeniorId(senior.getSeniorId());
		return matchings.stream().map(matching -> {
			// caregiverId가 null인 경우 바로 "N/A" 반환
//	        if (matching.getCaregiverId() == null) {
//	            return new MatchingDTO(matching, "N/A");
//	        }
	        // caregiverId가 존재하는 경우 해당 Caregiver 정보 조회
			
			Optional<Caregiver> caregiverOpt = caregiverPageRepository.findByCaregiverId(matching.getCaregiverId());
			String caregiverName = caregiverOpt.isPresent() ? caregiverOpt.get().getUser().getName() : "N/A";
			return new MatchingDTO(matching, caregiverName); // MatchingDTO 생성자에 caregiverName을 전달
		}).collect(Collectors.toList());
	}


}
