package com.uniai.cvbuilder.application.service;

import com.uniai.cvbuilder.application.dto.command.UpdatePersonalInfoCommand;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.application.port.in.PersonalInfoUseCase;
import com.uniai.cvbuilder.domain.builder.PersonalInfoBuilder;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.shared.exception.EmailNotFoundException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service handling retrieval and updates of user personal information leveraged
 * across generated CVs.
 */
@Service
@RequiredArgsConstructor
public class PersonalInfoApplicationService implements PersonalInfoUseCase {

    private final PersonalInfoRepository personalInfoRepository;
    private final UserRepository userRepository;

    @Override
    public PersonalInfoResponse getPersonalInfo(String email) {
        Long userId = getUserId(email);
        PersonalInfo info = personalInfoRepository.findByUserId(userId).orElse(null);
        return toResponse(info, userId);
    }

    @Override
    @Transactional
    public PersonalInfoResponse updatePersonalInfo(String email, UpdatePersonalInfoCommand command) {
        Long userId = getUserId(email);
        PersonalInfo info = personalInfoRepository.findByUserId(userId)
                .orElseGet(() -> PersonalInfoBuilder.forUser(userId).build());

        if (command.getPhone() != null) info.setPhone(command.getPhone());
        if (command.getAddress() != null) info.setAddress(command.getAddress());
        if (command.getLinkedin() != null) info.setLinkedin(command.getLinkedin());
        if (command.getGithub() != null) info.setGithub(command.getGithub());
        if (command.getPortfolio() != null) info.setPortfolio(command.getPortfolio());
        if (command.getSummary() != null) info.setSummary(command.getSummary());
        if (command.getJobTitle() != null) info.setJobTitle(command.getJobTitle());
        if (command.getCompany() != null) info.setCompany(command.getCompany());

        personalInfoRepository.save(info);
        return toResponse(info, userId);
    }

    private PersonalInfoResponse toResponse(PersonalInfo info, Long userId) {
        if (info == null) {
            return PersonalInfoResponse.builder().userId(userId).build();
        }
        return PersonalInfoResponse.builder()
                .id(info.getId())
                .userId(info.getUserId())
                .phone(info.getPhone())
                .address(info.getAddress())
                .linkedin(info.getLinkedin())
                .github(info.getGithub())
                .portfolio(info.getPortfolio())
                .summary(info.getSummary())
                .jobTitle(info.getJobTitle())
                .company(info.getCompany())
                .build();
    }

    private Long getUserId(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new);
        return user.getId();
    }
}
