TASK CODE: EMAIL_SMTP_AUTH_DEBUG_001

Recommended Model:
GPT-5.6 Terra

Objective

Add temporary, safe diagnostic logging around the email configuration and SMTP send flow to determine why Gmail returns:

MailAuthenticationException: Authentication failed
535-5.7.8 Username and Password not accepted

This is a DEBUGGING task.

Do not redesign the email system.
Do not change authentication behavior yet.
Do not change signup transaction behavior.
Do not replace Gmail.
Do not log passwords, JWT secrets, OAuth secrets, API keys, verification codes, or full email content.

────────────────────────────────────────
Confirmed Runtime Behavior
────────────────────────────────────────

Signup reaches:

AuthController.signUp
→ AuthApplicationService.signUp
→ AuthApplicationService.sendVerificationCode
→ EmailNotificationAdapter.sendVerificationEmail
→ EmailNotificationAdapter.sendEmail
→ JavaMailSender.send

The failure happens during SMTP authentication:

jakarta.mail.AuthenticationFailedException:
535-5.7.8 Username and Password not accepted

This means:

- email message configuration is now loaded
- the Thymeleaf template is reached
- JavaMail attempts an SMTP connection
- Gmail rejects the credentials or their resolved runtime values

The goal is to identify exactly what configuration Spring and Docker are using without exposing secrets.

────────────────────────────────────────
Files To Inspect
────────────────────────────────────────

Inspect at minimum:

Server/src/main/java/com/uniai/user/infrastructure/notification/EmailNotificationAdapter.java

Server/src/main/java/com/uniai/shared/infrastructure/email/EmailProperties.java

Server/src/main/resources/application.properties

Server/src/main/resources/email.properties

docker-compose.yml

.env

Any global exception handler that converts this failure into HTTP 403.

Search for:

spring.mail
MAIL_USERNAME
MAIL_PASSWORD
MAIL_FROM
JavaMailSender
MailAuthenticationException
AuthenticationFailedException
AccessDenied
FORBIDDEN
HttpStatus.FORBIDDEN
handleResponseError

────────────────────────────────────────
Required Safe Diagnostic Logging
────────────────────────────────────────

Add temporary logs that answer the following questions.

## 1. Email configuration at startup

At application startup, log a sanitized email configuration summary.

Log:

- SMTP host
- SMTP port
- SMTP auth enabled
- STARTTLS enabled
- STARTTLS required
- configured username, masked
- username length
- password present: true/false
- password length only
- password contains whitespace: true/false
- password begins or ends with whitespace: true/false
- configured `app.email.from`, masked
- whether `from` equals the authenticated username
- email-message keys loaded
- whether footer configuration exists
- active Spring profiles

Do not log:

- password value
- password prefix
- password suffix
- full username if avoidable
- OAuth secrets
- JWT secret
- verification code

Safe examples:

username=a***@gmail.com
usernameLength=23
passwordPresent=true
passwordLength=16
passwordContainsWhitespace=false
from=n***@uniai.com
fromMatchesUsername=false
messageKeys=[verify, two-factor, change-password]

## 2. Before sending an email

Inside `EmailNotificationAdapter.sendVerificationEmail(...)` or immediately before `mailSender.send(...)`, log:

- verification type
- resolved message key
- recipient domain only, not full address
- masked sender
- masked SMTP username
- SMTP host and port
- password present
- password length
- subject configured: true/false
- template name
- email type

Do not log:

- recipient full email
- verification code
- rendered HTML
- password
- message body

Example:

[EMAIL] Send started type=REGISTRATION key=verify recipientDomain=gmail.com smtpHost=smtp.gmail.com smtpPort=587 username=a***@gmail.com from=n***@uniai.com passwordPresent=true passwordLength=16

## 3. On mail authentication failure

Catch or inspect `MailAuthenticationException` narrowly enough to log:

- exception class
- nested exception class
- SMTP response code/message if available
- masked username
- host
- port
- from address mismatch flag
- password presence and length
- whether password contains whitespace
- durationMs

Do not swallow the exception.

Re-throw it using existing behavior after logging.

Do not log the password.

Example:

[EMAIL] SMTP authentication failed host=smtp.gmail.com port=587 username=a***@gmail.com passwordPresent=true passwordLength=16 passwordContainsWhitespace=false fromMatchesUsername=false durationMs=814 cause=AuthenticationFailedException smtpMessage="535-5.7.8 Username and Password not accepted"

## 4. Distinguish configuration resolution from Gmail rejection

Add enough logging to determine whether:

- the container passed an empty password
- Spring resolved the expected property
- the password contains accidental quotes
- the password contains spaces or line breaks
- Docker Compose used a different `.env` file
- `MAIL_USERNAME` differs from `spring.mail.username`
- `MAIL_FROM` differs from the authenticated Gmail account
- Gmail received credentials but rejected them

Do not assume the cause before observing logs.

────────────────────────────────────────
Recommended Implementation Shape
────────────────────────────────────────

Prefer a small dedicated diagnostics component rather than spreading startup logs across unrelated classes.

Possible class:

Server/src/main/java/com/uniai/shared/infrastructure/email/EmailConfigurationDiagnostics.java

Responsibilities:

- inject Spring mail properties or environment
- inject `EmailProperties`
- log one sanitized configuration summary at startup
- expose reusable masking/safety helpers if needed

Possible startup hook:

- `@PostConstruct`
- `ApplicationRunner`
- `ApplicationReadyEvent`

Choose the smallest project-consistent option.

Do not create a generic secrets framework.

For request-time logging, keep the logs inside `EmailNotificationAdapter`.

────────────────────────────────────────
Useful Configuration Sources
────────────────────────────────────────

Inspect the runtime values that Spring uses, not only source files.

Possible dependencies:

- `JavaMailSenderImpl`
- Spring `Environment`
- `MailProperties`
- `EmailProperties`

If `JavaMailSender` is a `JavaMailSenderImpl`, sanitized diagnostics may inspect:

- host
- port
- username
- protocol
- JavaMail properties

Do not cast unsafely without checking the type.

Example concept:

```java
if (mailSender instanceof JavaMailSenderImpl sender) {
    String username = sender.getUsername();
    String password = sender.getPassword();

    logger.info(
        "[EMAIL_CONFIG] host={} port={} username={} usernameLength={} passwordPresent={} passwordLength={}",
        sender.getHost(),
        sender.getPort(),
        maskEmail(username),
        length(username),
        hasText(password),
        length(password)
    );
}
```

Never log `sender.getPassword()` directly.

────────────────────────────────────────
Whitespace and Quoting Diagnostics
────────────────────────────────────────

The password may be rejected because Docker or `.env` resolution includes unexpected characters.

Log booleans only:

- containsWhitespace
- startsWithWhitespace
- endsWithWhitespace
- containsQuoteCharacter
- containsNewline
- expectedAppPasswordLength

Do not log character positions or the characters themselves.

Example:

passwordLength=18
containsWhitespace=true
containsQuoteCharacter=true

This may indicate values such as:

"abcd efgh ijkl mnop"

or accidental quotation marks.

Do not automatically strip or mutate the password in this debugging task.

────────────────────────────────────────
Sender Identity Diagnostics
────────────────────────────────────────

Current likely configuration:

SMTP username:
ali.nz.fayad@gmail.com

From:
noreply@uniai.com

Log whether these differ.

Do not automatically change `MAIL_FROM` during this task.

The logs should make the mismatch visible:

fromMatchesUsername=false

This mismatch may affect sending, but do not incorrectly claim it causes SMTP authentication itself unless verified.

Authentication happens before sender authorization in many SMTP flows.

────────────────────────────────────────
HTTP 403 Investigation
────────────────────────────────────────

The frontend reports:

403 Forbidden

But the backend root cause is:

MailAuthenticationException

Investigate the global exception mapping.

Determine why a mail infrastructure failure becomes HTTP 403.

Inspect:

- global exception handlers
- security exception handlers
- generic `IllegalStateException` handlers
- Spring Security access-denied handlers
- frontend `errorHandlers.ts`

For this task:

- diagnose and report the incorrect mapping
- add temporary logging around the mapping if useful
- do not redesign API errors unless the smallest safe correction is obvious and explicitly documented

A mail authentication failure should normally be a server/infrastructure response, not an authorization response.

Expected future status would likely be:

- 500 Internal Server Error
or
- 503 Service Unavailable

Do not implement the status correction unless it is clearly inside the requested scope and covered by tests.

────────────────────────────────────────
Security Constraints
────────────────────────────────────────

Absolute rules:

- Never log `MAIL_PASSWORD`.
- Never log the verification code.
- Never log rendered email HTML.
- Never log JWT_SECRET.
- Never log GOOGLE_CLIENT_SECRET.
- Never log GEMINI_API_KEY.
- Never log GROQ_API_KEY.
- Never log the recipient’s full email.
- Never include secrets in exceptions.
- Never commit real secrets to source control.

The user previously exposed credential values while debugging.

Recommend rotating any exposed credentials after diagnosis, including:

- Gmail App Password
- Google OAuth client secret
- Gemini API key
- Groq API key
- JWT secret if it is not only a disposable local value

Do not rotate them automatically.

────────────────────────────────────────
Tests Required
────────────────────────────────────────

Add focused tests for diagnostic helpers where practical.

Verify:

- email masking works
- null username is safe
- empty password reports `present=false`
- non-empty password reports correct length
- whitespace detection works
- quotes are detected
- no diagnostic string contains the real password
- no diagnostic string contains the verification code
- recipient logs contain only the domain
- exception logging does not swallow the original exception

Do not add SMTP integration tests against Gmail.

Do not call live Gmail in automated tests.

Use mocks or a fake mail sender.

────────────────────────────────────────
Manual Validation
────────────────────────────────────────

After implementation:

1. Rebuild and recreate the app container.

```bash
docker compose up -d --build --force-recreate app
```

2. Inspect sanitized startup logs.

```bash
docker compose logs app --tail=200 | grep -E "EMAIL_CONFIG|EMAIL"
```

3. Confirm logs show:

- correct host
- port 587
- masked username
- non-zero password length
- no whitespace or quote problems
- loaded message keys
- sender/username match status

4. Retry signup.

5. Inspect the send and failure logs.

6. Compare the runtime results against `.env` and Compose configuration.

7. Do not paste real secrets into reports.

────────────────────────────────────────
Potential Outcomes
────────────────────────────────────────

Classify the final observed result as one of:

A. PASSWORD_EMPTY_AT_RUNTIME

B. PASSWORD_HAS_WHITESPACE_OR_QUOTES

C. USERNAME_MISMATCH

D. FROM_ADDRESS_MISMATCH

E. INVALID_OR_REVOKED_GMAIL_APP_PASSWORD

F. GMAIL_ACCOUNT_DOES_NOT_ALLOW_APP_PASSWORDS

G. WRONG_DOCKER_ENV_FILE

H. STALE_CONTAINER_ENVIRONMENT

I. SPRING_PROPERTY_OVERRIDE

J. UNKNOWN_GMAIL_REJECTION

Do not guess. Use runtime logs.

────────────────────────────────────────
Files Likely To Change
────────────────────────────────────────

Likely:

Server/src/main/java/com/uniai/user/infrastructure/notification/EmailNotificationAdapter.java

Possibly:

Server/src/main/java/com/uniai/shared/infrastructure/email/EmailConfigurationDiagnostics.java

Server/src/test/java/com/uniai/shared/infrastructure/email/EmailConfigurationDiagnosticsTest.java

Server/src/test/java/com/uniai/user/infrastructure/notification/EmailNotificationAdapterTest.java

Possibly global exception-handler tests if the 403 mapping is investigated.

Do not change:

- email template styling
- auth flow
- verification-code generation
- database schema
- frontend signup behavior
- SMTP provider
- Docker image architecture

────────────────────────────────────────
Validation Commands
────────────────────────────────────────

Run focused tests using actual class names:

```bash
cd Server

./mvnw -q \
  -Dtest=EmailConfigurationDiagnosticsTest,EmailNotificationAdapterTest \
  test
```

Run compile:

```bash
./mvnw -q -DskipTests compile
```

Run diff check:

```bash
git diff --check
```

Then rebuild the app container:

```bash
cd ..
docker compose up -d --build --force-recreate app
```

Do not claim Gmail authentication succeeded unless signup actually sends the email.

────────────────────────────────────────
Required Deliverables
────────────────────────────────────────

Return exactly:

## 1. Files Changed

## 2. Existing SMTP Configuration Flow

## 3. Diagnostic Logging Added

## 4. Secret-Safety Review

## 5. Runtime Configuration Findings

## 6. SMTP Authentication Findings

## 7. HTTP 403 Mapping Findings

## 8. Tests Added or Updated

## 9. Validation Results

## 10. Confirmed Root Cause

Choose one of the defined outcome codes.

## 11. Recommended Correction

Do not implement the correction unless it was explicitly included and safely scoped.

## 12. Temporary Logging Cleanup Plan

State which temporary logs should be removed or reduced after diagnosis.

## 13. Remaining Risks

## 14. Commit Message

Use:

```text
chore(email): add safe SMTP authentication diagnostics
```
