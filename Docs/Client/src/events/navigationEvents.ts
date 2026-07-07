export const NAVIGATION_REQUEST_EVENT = 'uniai:navigation-request';

export type NavigationReason = 'auth-required' | 'profile-incomplete';

export interface NavigationRequest {
  path: string;
  reason: NavigationReason;
  clearAuth?: boolean;
}

export const requestNavigation = (detail: NavigationRequest) => {
  window.dispatchEvent(new CustomEvent<NavigationRequest>(NAVIGATION_REQUEST_EVENT, { detail }));
};
