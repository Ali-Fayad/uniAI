package com.uniai.chat.domain.builder;

import com.uniai.chat.domain.model.Chat;
import com.uniai.user.domain.model.User;

/**
 * Domain builder for {@link Chat}.
 *
 * <p>Acts as the single construction point for Chat objects.
 * Future additions (default title, tags, settings) can be added here
 * without touching the application layer.
 */
public final class ChatBuilder {

    private User   user;
    private String title;

    private ChatBuilder() {}

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static ChatBuilder forUser(User user) {
        ChatBuilder b = new ChatBuilder();
        b.user = user;
        return b;
    }

    // -------------------------------------------------------------------------
    // Optional overrides
    // -------------------------------------------------------------------------

    public ChatBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    // -------------------------------------------------------------------------
    // Terminal
    // -------------------------------------------------------------------------

    public Chat build() {
        return Chat.builder()
                .user(user)
                .title(title)
                .build();
    }
}
