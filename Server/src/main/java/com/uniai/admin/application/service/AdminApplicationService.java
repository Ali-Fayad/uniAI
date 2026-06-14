package com.uniai.admin.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.admin.application.dto.response.AdminFeedbackResponse;
import com.uniai.admin.application.dto.response.AdminUserDetailsResponse;
import com.uniai.admin.application.dto.response.AdminUserFeedbackResponse;
import com.uniai.admin.application.dto.response.AdminUserSearchResponse;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.cvbuilder.application.dto.response.PersonalInfoResponse;
import com.uniai.cvbuilder.domain.model.PersonalInfo;
import com.uniai.cvbuilder.domain.repository.CVRepository;
import com.uniai.cvbuilder.domain.repository.PersonalInfoRepository;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.shared.exception.FeedbackNotFoundException;
import com.uniai.shared.exception.LastAdminProtectionException;
import com.uniai.shared.exception.SelfDeleteNotAllowedException;
import com.uniai.shared.exception.SelfDemotionNotAllowedException;
import com.uniai.shared.exception.UserNotFoundException;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import com.uniai.user.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Thin admin application service for basic admin access and overview aggregation.
 * Keeps the admin module orchestration in the application layer.
 */
@Service
@RequiredArgsConstructor
public class AdminApplicationService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FeedbackRepository feedbackRepository;
    private final CVRepository cvRepository;
    private final PersonalInfoRepository personalInfoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getHealthMessage() {
        return "Admin access granted";
    }

    public AdminOverviewResponse getOverview() {
        long totalUsers = userRepository.count();
        long totalChats = chatRepository.count();
        long totalMessages = messageRepository.count();
        long totalFeedback = feedbackRepository.count();

        double averageChatsPerUser = totalUsers == 0 ? 0.0 : totalChats / (double) totalUsers;
        double averageMessagesPerChat = totalChats == 0 ? 0.0 : totalMessages / (double) totalChats;
        double averageMessagesPerUser = totalUsers == 0 ? 0.0 : totalMessages / (double) totalUsers;

        return AdminOverviewResponse.builder()
                .totalUsers(totalUsers)
                .totalChats(totalChats)
                .totalMessages(totalMessages)
                .totalFeedback(totalFeedback)
                .averageChatsPerUser(averageChatsPerUser)
                .averageMessagesPerChat(averageMessagesPerChat)
                .averageMessagesPerUser(averageMessagesPerUser)
                .build();
    }

    public List<AdminUserSearchResponse> searchUsersByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return List.of();
        }

        return userRepository.searchByEmail(email.trim()).stream()
                .map(this::toSearchResponse)
                .toList();
    }

    public AdminUserDetailsResponse getUserDetails(Long userId) {
        User user = getRequiredUser(userId);
        long chatCount = chatRepository.countByUserId(userId);
        long messageCount = messageRepository.countByUserId(userId);
        long cvCount = cvRepository.countByUserId(userId);

        double averageMessagesPerChat = chatCount == 0 ? 0.0 : messageCount / (double) chatCount;

        return AdminUserDetailsResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole() == null ? null : user.getRole().name())
                .isVerified(user.isVerified())
                .isTwoFacAuth(user.isTwoFacAuth())
                .chatCount(chatCount)
                .messageCount(messageCount)
                .averageMessagesPerChat(averageMessagesPerChat)
                .cvCount(cvCount)
                .build();
    }

    public PersonalInfoResponse getUserPersonalInfo(Long userId) {
        getRequiredUser(userId);
        PersonalInfo info = personalInfoRepository.findByUserId(userId).orElse(null);
        return toPersonalInfoResponse(info, userId);
    }

    public List<AdminUserFeedbackResponse> getUserFeedback(Long userId) {
        getRequiredUser(userId);
        return feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toFeedbackResponse)
                .toList();
    }

    public List<AdminFeedbackResponse> getFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toAdminFeedbackResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(String actorEmail, Long userId) {
        User actor = userRepository.findByEmail(actorEmail).orElseThrow(UserNotFoundException::new);
        User target = getRequiredUser(userId);

        if (actor.getId() != null && actor.getId().equals(target.getId())) {
            throw new SelfDeleteNotAllowedException();
        }

        if (target.getRole() == UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new LastAdminProtectionException();
            }
        }

        feedbackRepository.deleteByUserId(target.getId());
        userRepository.delete(target);
    }

    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(FeedbackNotFoundException::new);
        feedbackRepository.deleteById(feedback.getId());
    }

    @Transactional
    public AdminUserDetailsResponse updateUserRole(String actorEmail, Long userId, UserRole requestedRole) {
        User actor = userRepository.findByEmail(actorEmail).orElseThrow(UserNotFoundException::new);
        User target = getRequiredUser(userId);

        if (actor.getId() != null && actor.getId().equals(target.getId()) && target.getRole() != requestedRole) {
            throw new SelfDemotionNotAllowedException();
        }

        if (target.getRole() == requestedRole) {
            return getUserDetails(userId);
        }

        if (target.getRole() == UserRole.ADMIN && requestedRole == UserRole.USER) {
            long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new LastAdminProtectionException();
            }
        }

        target.setRole(requestedRole);
        userRepository.save(target);
        return getUserDetails(userId);
    }

    private AdminUserSearchResponse toSearchResponse(User user) {
        return AdminUserSearchResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole() == null ? null : user.getRole().name())
                .build();
    }

    private AdminUserFeedbackResponse toFeedbackResponse(Feedback feedback) {
        return AdminUserFeedbackResponse.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private AdminFeedbackResponse toAdminFeedbackResponse(Feedback feedback) {
        return AdminFeedbackResponse.builder()
                .id(feedback.getId())
                .userId(feedback.getUserId())
                .rating(feedback.getRating())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private User getRequiredUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    private PersonalInfoResponse toPersonalInfoResponse(PersonalInfo info, Long userId) {
        if (info == null) {
            return PersonalInfoResponse.builder()
                    .userId(userId)
                    .hasPersonalInfo(false)
                    .education(List.of())
                    .skills(List.of())
                    .languages(List.of())
                    .experience(List.of())
                    .projects(List.of())
                    .certificates(List.of())
                    .build();
        }

        List<PersonalInfoResponse.EducationEntryResponse> education = fromJson(
                info.getEducationJson(),
                new TypeReference<>() {},
                List.of()
        );
        List<PersonalInfoResponse.SkillEntryResponse> skills = fromJson(
                info.getSkillsJson(),
                new TypeReference<>() {},
                List.of()
        );
        List<PersonalInfoResponse.LanguageEntryResponse> languages = fromJson(
                info.getLanguagesJson(),
                new TypeReference<>() {},
                List.of()
        );
        List<PersonalInfoResponse.ExperienceEntryResponse> experience = fromJson(
                info.getExperienceJson(),
                new TypeReference<>() {},
                List.of()
        );
        List<PersonalInfoResponse.ProjectEntryResponse> projects = fromJson(
                info.getProjectsJson(),
                new TypeReference<>() {},
                List.of()
        );
        List<PersonalInfoResponse.CertificateEntryResponse> certificates = fromJson(
                info.getCertificatesJson(),
                new TypeReference<>() {},
                List.of()
        );

        boolean hasPersonalInfo = hasText(info.getPhone())
                || hasText(info.getAddress())
                || hasText(info.getLinkedin())
                || hasText(info.getGithub())
                || hasText(info.getPortfolio())
                || hasText(info.getSummary())
                || hasText(info.getJobTitle())
                || hasText(info.getCompany())
                || !education.isEmpty()
                || !skills.isEmpty()
                || !languages.isEmpty()
                || !experience.isEmpty()
                || !projects.isEmpty()
                || !certificates.isEmpty();

        return PersonalInfoResponse.builder()
                .userId(info.getUserId())
                .hasPersonalInfo(hasPersonalInfo)
                .isFilled(Boolean.TRUE.equals(info.getIsFilled()))
                .phone(info.getPhone())
                .address(info.getAddress())
                .linkedin(info.getLinkedin())
                .github(info.getGithub())
                .portfolio(info.getPortfolio())
                .summary(info.getSummary())
                .jobTitle(info.getJobTitle())
                .company(info.getCompany())
                .education(education)
                .skills(skills)
                .languages(languages)
                .experience(experience)
                .projects(projects)
                .certificates(certificates)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private <T> T fromJson(String value, TypeReference<T> typeReference, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }
}
