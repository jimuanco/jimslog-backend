package jimuanco.jimslog.config.filter;

import jakarta.servlet.ServletException;
import jimuanco.jimslog.domain.user.Role;
import jimuanco.jimslog.domain.user.User;
import jimuanco.jimslog.utils.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    String testSecretKey = "81b87ceccbd1c0d324168169b2292ab4b53bcd66b54f5e562d0fa4005ebd942f";

    @DisplayName("유효한 JWT로 인증이 필요한 API 요청시 인증이 성공하면 SecurityContextHolder에 인증 정보가 담긴다.") //todo 도메인 스럽게 수정
    @Test
    void doFilterInternal() throws ServletException, IOException {
        // given
        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils);

        User user = User.builder()
                .email("jim@gmail.com")
                .role(Role.USER)
                .build();
        String accessToken = jwtUtils.generateAccessToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        request.addHeader("Authorization", "Bearer " + accessToken);

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication)
                .extracting("principal", "authorities")
                .contains("jim@gmail.com", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @DisplayName("유효하지 않은 JWT로 인증이 필요한 API 요청시 인증이 실패해 SecurityContextHolder에 인증 정보가 담기지 않는다.") //todo 도메인 스럽게 수정
    @Test
    void doFilterInternalWithInvalidJwt() throws ServletException, IOException {
        // given
        JwtUtils jwtUtils = new JwtUtils(testSecretKey);
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtils);

        User user = User.builder()
                .email("jim@gmail.com")
                .role(Role.USER)
                .build();
        String accessToken = jwtUtils.generateAccessToken(user);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        request.addHeader("Authorization", "Bearer " + accessToken + "a");

        // when
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication);
        assertThat(authentication).isNull();
    }

}