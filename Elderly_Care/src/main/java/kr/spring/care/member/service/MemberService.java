package kr.spring.care.member.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.spring.care.member.entity.Member;
import kr.spring.care.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
	
	private final MemberRepository memberRepository;
	
	public Member saveMember(Member member){
		validateDuplicate(member);
		return memberRepository.save(member);
	}
	
	private void validateDuplicate(Member member) {
		Member findMember = memberRepository.findByEmail(member.getEmail());
		if(findMember != null) {
			throw new IllegalStateException("이미 등록된 사용자 입니다.");
		}
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Member member = memberRepository.findByEmail(email);
		
		if(member == null) {
			throw new UsernameNotFoundException("해당 사용자가 없습니다" + email);
		}
		
		log.info("-------> loadUserByUsername" + member);
		
		return User.builder()
				.username(member.getEmail())
				.password(member.getPassword())
                .roles(member.getRole().toString())
				.build();
	}

	public boolean login(Member member, PasswordEncoder passwordEncoder) {
	    Member findMember = memberRepository.findByEmail(member.getEmail());
	    if(findMember == null || !passwordEncoder.matches(member.getPassword(), findMember.getPassword())){
	        return false;
	    }
	    return true;
	}
}
