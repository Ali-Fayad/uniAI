package com.uniai.admin.application.service;

import com.uniai.admin.application.dto.response.AdminOverviewResponse;
import com.uniai.chat.domain.model.Chat;
import com.uniai.chat.domain.model.Message;
import com.uniai.chat.domain.repository.ChatRepository;
import com.uniai.chat.domain.repository.MessageRepository;
import com.uniai.feedback.domain.model.Feedback;
import com.uniai.feedback.domain.repository.FeedbackRepository;
import com.uniai.user.domain.model.User;
import com.uniai.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminApplicationServiceTest {

    @Test
    void getOverviewShouldReturnZerosForEmptyDatabase() {
        AdminApplicationService service = new AdminApplicationService(
                new CountingUserRepository(0),
                new CountingChatRepository(0),
                new CountingMessageRepository(0),
                new CountingFeedbackRepository(0)
        );

        AdminOverviewResponse response = service.getOverview();

        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getTotalChats());
        assertEquals(0L, response.getTotalMessages());
        assertEquals(0L, response.getTotalFeedback());
        assertEquals(0.0, response.getAverageChatsPerUser());
        assertEquals(0.0, response.getAverageMessagesPerChat());
        assertEquals(0.0, response.getAverageMessagesPerUser());
    }

    @Test
    void getOverviewShouldCalculateAggregatesFromCounts() {
        AdminApplicationService service = new AdminApplicationService(
                new CountingUserRepository(12),
                new CountingChatRepository(34),
                new CountingMessageRepository(198),
                new CountingFeedbackRepository(5)
        );

        AdminOverviewResponse response = service.getOverview();

        assertEquals(12L, response.getTotalUsers());
        assertEquals(34L, response.getTotalChats());
        assertEquals(198L, response.getTotalMessages());
        assertEquals(5L, response.getTotalFeedback());
        assertEquals(34d / 12d, response.getAverageChatsPerUser());
        assertEquals(198d / 34d, response.getAverageMessagesPerChat());
        assertEquals(198d / 12d, response.getAverageMessagesPerUser());
    }

    private static final class CountingUserRepository implements UserRepository {
        private final long count;

        private CountingUserRepository(long count) {
            this.count = count;
        }

        @Override public Optional<User> findByEmail(String email) { throw unsupported(); }
        @Override public Optional<User> findByUsername(String username) { throw unsupported(); }
        @Override public boolean existsByEmail(String email) { throw unsupported(); }
        @Override public boolean existsByUsername(String username) { throw unsupported(); }
        @Override public User save(User user) { throw unsupported(); }
        @Override public void delete(User user) { throw unsupported(); }
        @Override public boolean deleteByEmail(String email) { throw unsupported(); }
        @Override public boolean deleteByUsername(String username) { throw unsupported(); }
        @Override public List<User> findAll() { throw unsupported(); }
        @Override public long count() { return count; }
    }

    private static final class CountingChatRepository implements ChatRepository {
        private final long count;

        private CountingChatRepository(long count) {
            this.count = count;
        }

        @Override public Optional<Chat> findById(Long id) { throw unsupported(); }
        @Override public List<Chat> findByUserUsernameOrderByUpdatedAtDesc(String username) { throw unsupported(); }
        @Override public String findTitleById(Long chatId) { throw unsupported(); }
        @Override public Chat save(Chat chat) { throw unsupported(); }
        @Override public void delete(Chat chat) { throw unsupported(); }
        @Override public void deleteAll(List<Chat> chats) { throw unsupported(); }
        @Override public long count() { return count; }
    }

    private static final class CountingMessageRepository implements MessageRepository {
        private final long count;

        private CountingMessageRepository(long count) {
            this.count = count;
        }

        @Override public List<Message> findByChatIdOrderByTimestampAsc(Long chatId) { throw unsupported(); }
        @Override public List<Message> findTop10ByChatIdOrderByTimestampDesc(Long chatId) { throw unsupported(); }
        @Override public void deleteByChatId(Long chatId) { throw unsupported(); }
        @Override public void deleteByChatIdIn(List<Long> chatIds) { throw unsupported(); }
        @Override public long countByChatId(Long chatId) { throw unsupported(); }
        @Override public long count() { return count; }
        @Override public boolean existsByChatId(Long chatId) { throw unsupported(); }
        @Override public Message save(Message message) { throw unsupported(); }
    }

    private static final class CountingFeedbackRepository implements FeedbackRepository {
        private final long count;

        private CountingFeedbackRepository(long count) {
            this.count = count;
        }

        @Override public Feedback save(Feedback feedback) { throw unsupported(); }
        @Override public long count() { return count; }
    }

    private static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not used in this test");
    }
}
