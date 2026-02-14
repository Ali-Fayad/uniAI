/**
 * Centralized static text constants for the application
 * 
 * This file contains all UI text strings used throughout the application.
 * Structure is designed to support future internationalization (i18n).
 * 
 * Usage:
 * import { TEXT } from '@/constants/static';
 * <h1>{TEXT.common.appName}</h1>
 */

export const TEXT = {
  common: {
    appName: "uniAI",
    loading: "Loading...",
    error: "An unexpected error occurred.",
    cancel: "Cancel",
    save: "Save",
    submit: "Submit",
    delete: "Delete",
    edit: "Edit",
    back: "Back",
    next: "Next",
    confirm: "Confirm",
    close: "Close",
    logout: "Logout",
  },

  header: {
    appName: "uniAI",
  },

  main: {
    hero: {
      welcome: "Welcome to",
      appName: "uniAI",
      subtitle: "Your intelligent companion for academic excellence. Experience the future of learning with our advanced AI-powered platform.",
      getStarted: "Get Started",
      maps: "Discover All Campuses",
    },
    tryNow: "Try Now",
    cards: {
      powerfulIntegration: {
        title: "Powerful Integration",
        description: "Seamlessly connect with your favorite tools and platforms. UniAI works with your existing ecosystem to enhance productivity without disruption.",
      },
      creativeAssistance: {
        title: "Creative Assistance",
        description: "Break through creative blocks with AI-powered suggestions, content generation, and idea exploration. Elevate your creative projects to new heights.",
      },
      dataInsights: {
        title: "Data-driven Insights",
        description: "Transform complex data into clear, actionable insights. Make smarter decisions with our advanced analytics and visualization capabilities.",
      },
    },
    feedback: {
      heading: "We Value Your Input",
      subheading: "Your experience matters to us. Whether you have a suggestion, a question, or just want to say hello, we're here to listen. Help us shape the future of UniAI.",
      emailLabel: "Email",
      emailPlaceholder: "you@example.com",
      ratingLabel: "Rate Your Experience",
      feedbackLabel: "Your Feedback",
      feedbackPlaceholder: "Let us know how we can improve...",
      submitButton: "Submit Feedback",
    },
  },

  auth: {
    signIn: {
      title: "Welcome Back",
      subtitle: "Sign in to continue to your dashboard.",
      emailLabel: "Email Address",
      emailPlaceholder: "Enter your email",
      passwordLabel: "Password",
      passwordPlaceholder: "Enter your password",
      forgotPassword: "Forgot Password?",
      submitButton: "Sign In",
      submitButtonLoading: "Signing In...",
      noAccount: "Don't have an account?",
      signUpLink: "Create one",
      errors: {
        invalidCredentials: "Failed to sign in. Please check your credentials.",
      },
    },
    signUp: {
      title: "Create Account",
      subtitle: "Join and save your chats securely.",
      usernamePlaceholder: "Username",
      firstNamePlaceholder: "First Name",
      lastNamePlaceholder: "Last Name",
      emailPlaceholder: "Email Address",
      passwordPlaceholder: "Password",
      confirmPasswordPlaceholder: "Confirm Password",
      submitButton: "Sign Up",
      submitButtonLoading: "Creating Account...",
      haveAccount: "Already have an account?",
      signInLink: "Sign In",
      errors: {
        passwordMismatch: "Passwords do not match",
        signUpFailed: "Failed to sign up. Please try again.",
      },
    },
    forgotPassword: {
      title: "Reset Password",
      subtitle: "Enter your email to receive a reset code.",
      emailLabel: "Email Address",
      emailPlaceholder: "Enter your email",
      submitButton: "Send Code",
      submitButtonLoading: "Sending...",
      success: "Reset code sent successfully! Redirecting...",
      error: "Failed to send reset code. Please try again.",
    },
    forgotPasswordConfirm: {
      title: "Enter Reset Code",
      subtitle: "Check your email for the verification code.",
      codePlaceholder: "Enter verification code",
      newPasswordPlaceholder: "New Password",
      confirmPasswordPlaceholder: "Confirm New Password",
      submitButton: "Reset Password",
      submitButtonLoading: "Resetting...",
      errors: {
        passwordMismatch: "Passwords do not match",
        resetFailed: "Failed to reset password. Please try again.",
      },
    },
    verify: {
      title: "Verify Your Email",
      subtitle: "Enter the verification code sent to your email.",
      codePlaceholder: "Enter verification code",
      submitButton: "Verify",
      submitButtonLoading: "Verifying...",
      resendButton: "Resend Code",
      error: "Failed to verify code. Please try again.",
    },
    verify2FA: {
      title: "Two-Factor Verification",
      subtitle: "Enter the 6-digit code from your authenticator app.",
      codePlaceholder: "Enter 6-digit code",
      submitButton: "Verify",
      submitButtonLoading: "Verifying...",
      error: "Invalid verification code. Please try again.",
    },
  },

  chat: {
    newConversation: "Start a new conversation",
    loading: "Loading messages...",
    inputPlaceholder: "Type your message...",
    sendButton: "Send",
    emptyState: {
      icon: "chat_bubble_outline",
      message: "Start a new conversation",
    },
    sidebar: {
      newChat: "New Chat",
      conversations: "Conversations",
      deleteConfirm: "Are you sure you want to delete this chat?",
    },
  },

  settings: {
    title: "Settings",
    profile: {
      title: "Update Profile",
      firstName: "First name",
      lastName: "Last name",
      username: "Username",
      usernamePlaceholder: "janesmith",
      email: "Email address",
      twoFactor: {
        title: "Two-Factor Authentication (2FA)",
        description: "Add an extra layer of security to your account.",
      },
      saveButton: "Save Changes",
      saveButtonLoading: "Saving...",
      cancelButton: "Cancel",
    },
    theme: {
      title: "Theme Preference",
      light: {
        title: "Light Mode",
        description: "Default light appearance",
      },
      dark: {
        title: "Dark Mode",
        description: "Easy on the eyes",
      },
    },
    feedback: {
      title: "Feedback",
      description: "How was your experience using uniAI? We value your input.",
      ratingLabel: "Rate your experience",
      feedbackLabel: "Your Feedback",
      feedbackPlaceholder: "What features should we build next?",
      submitButton: "Submit Feedback",
    },
    dangerZone: {
      title: "Danger Zone",
      description: "Irreversible and sensitive actions.",
      changePassword: "Change Password",
      deleteAccount: "Delete Account",
    },
  },

  map: {
    title: "Lebanese Universities Map",
    subtitle: "Explore all major universities across Lebanon. Click on markers to learn more about each institution.",
    visitWebsite: "Visit Website",
    loading: "Loading map...",
    error: "Failed to load map. Please try again later.",
  },

  errors: {
    default: "An unexpected error occurred.",
    network: "Network error. Please check your connection.",
    unauthorized: "You are not authorized to perform this action.",
    notFound: "The requested resource was not found.",
    serverError: "Server error. Please try again later.",
  },

  validation: {
    required: "This field is required.",
    email: "Please enter a valid email address.",
    password: {
      min: "Password must be at least 8 characters.",
      match: "Passwords must match.",
    },
  },
} as const;

// Type helper for accessing nested text values with autocompletion
export type TextKey = typeof TEXT;
