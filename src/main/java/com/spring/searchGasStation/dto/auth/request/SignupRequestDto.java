package com.spring.searchGasStation.dto.auth.request;

import com.spring.searchGasStation.domain.entity.Member;
import com.spring.searchGasStation.domain.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    // (선택) 비밀번호 복잡도 검사: 8자 이상, 영문/숫자/특수문자 포함
    // @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$", message = "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String password;

    // ★ 서버에서도 비교하기 위해 이 필드를 추가합니다.
    private String checkPassword;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .role(Role.USER)
                .build();
    }
}