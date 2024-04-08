    package kr.spring.care.user.entity;

    import java.time.LocalDateTime;

    import org.springframework.security.crypto.password.PasswordEncoder;

    import jakarta.persistence.CascadeType;
    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.EnumType;
    import jakarta.persistence.Enumerated;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import jakarta.persistence.OneToOne;
    import kr.spring.care.user.constant.Role;
    import kr.spring.care.user.dto.UserFormDto;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import lombok.ToString;

    @Entity
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public class User {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(nullable = false, updatable = false)
        private Long userId;
        
        @Enumerated(EnumType.STRING)
        private Role role;
        
        private String name;
        private String email;
        private String address;
        private String password;

        private String phoneNumber;
        private String country;
        private String gender;
        private String image;
        
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // 관계 설정 (예: Caregiver, Senior, Guardian과의 연결)
        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
        private Caregiver caregiver;

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
        private Senior senior;

        @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
        private Guardian guardian;
        
        //User 생성
        public static User createUser(UserFormDto userFormDto, PasswordEncoder passwordEncoder) {
            User user = new User();
            user.setName(userFormDto.getName());
            user.setEmail(userFormDto.getEmail());
            user.setAddress(userFormDto.getAddress());
            user.setPhoneNumber(userFormDto.getPhoneNumber());
            user.setCountry(userFormDto.getCountry());
            user.setGender(userFormDto.getGender());
            user.setImage(userFormDto.getImage());
            user.setRole(Role.SENIOR); // 또는 다른 Role 설정
            user.setPassword(passwordEncoder.encode(userFormDto.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            if (userFormDto.getCertification() == null) {
                Senior senior = new Senior();
                senior.setSeniorName(user.getName()); // 유저 이름으로 시니어 이름 설정
                senior.setHasGuardian(userFormDto.getHasGuardian());
                senior.setRequirements(userFormDto.getRequirements());
                senior.setHealth(userFormDto.getHealth());
                senior.setUser(user);
                user.setSenior(senior);
        
                if (userFormDto.getHasGuardian() != null && !userFormDto.getGuardianName().isEmpty()) {
                    Guardian guardian = new Guardian();
                    guardian.setGuardianName(userFormDto.getGuardianName());
                    guardian.setGuardianPhoneNumber(userFormDto.getGuardianPhoneNumber());
                    guardian.setRelationship(userFormDto.getRelationship());
                    guardian.setSenior(senior);
                    guardian.setUser(user);
                    user.setGuardian(guardian);
                }
            }

            if (userFormDto.getCertification() != null && !userFormDto.getCertification().isEmpty()) {
                Caregiver caregiver = new Caregiver();
                caregiver.setCertification(userFormDto.getCertification());
                caregiver.setSpecialization(userFormDto.getSpecialization());
                caregiver.setExperience(userFormDto.getExperience());
                caregiver.setExperienceYears(userFormDto.getExperienceYears());
                caregiver.setAvailableHours(userFormDto.getAvailableHours());
                caregiver.setUser(user); 
                user.setCaregiver(caregiver);
            }
            return user;
        }
    }
