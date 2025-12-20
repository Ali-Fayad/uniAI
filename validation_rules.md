# UniAI Input Validation Rules

This document defines the input validation rules for user registration and login in the UniAI platform. The rules must be enforced **on both client (frontend) and server (backend) sides** to ensure consistency, security, and data integrity.

---

## 1. First Name and Last Name

- Must contain only alphabetic characters (A-Z, a-z).
- Must be at least 2 characters long.
- Leading and trailing spaces should be trimmed.
- Must be **stored in the database in Capitalized form** (first letter uppercase, all others lowercase).
- Example:
  - Input: ` alEx ` → Saved: `Alex`
  - Input: `MARy` → Saved: `Mary`

---

## 2. Username

- Can contain only: alphabetic characters, numbers, and underscore `_`.
- Must be at least 2 characters long.
- Must be stored in **lowercase** in the database.
- Example:
  - Input: `User_123` → Saved: `user_123`

---

## 3. Email

- Must be a valid email format.
- Must be stored in **lowercase** in the database.
- Example:
  - Input: `John.Doe@Example.COM` → Saved: `john.doe@example.com`

---

## 4. Password

- Must contain at least:
  - One uppercase letter (A-Z)
  - One number (0-9)
- Must be at least 8 characters long.
- **Frontend submission:** password must be hashed using **SHA-256** before sending to the backend.
- **Backend storage:** must hash the received SHA-256 value again before storing in the database (hash of the hashed password).

---

## 5. Login Credentials

- Email and password must be submitted in the formats described above.
- Email is stored in lowercase for case-insensitive search.
- Password is submitted hashed from the frontend. Backend hashes again before verification.

---

## 6. Additional Notes

- All trimming, capitalization, and lowercase transformations must happen **before sending data from the frontend**.
- The backend must **re-validate** all inputs, even if frontend validation exists, to prevent bypass or tampering.
- Any invalid input must return a clear error message to the client.

---

**Example Flow:**

1. User enters:
