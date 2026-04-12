package com.uniai.cvbuilder.infrastructure.web;

import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.shared.exception.EmailNotFoundException;
import com.uniai.shared.exception.PersonalInfoGoneException;
import com.uniai.shared.infrastructure.jwt.JwtFacade;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ProfileCompletionInterceptor implements HandlerInterceptor {

    private final JwtFacade jwtFacade;
    private final UserRepository userRepository;
    private final PersonalInfoRepository personalInfoRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String email = jwtFacade.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);
        PersonalInfo info = personalInfoRepository.findByUserId(user.getId()).orElse(null);
        
        if (info == null || !Boolean.TRUE.equals(info.getIsFilled())) {
            throw new PersonalInfoGoneException();
        }
        
        return true;
    }
}
