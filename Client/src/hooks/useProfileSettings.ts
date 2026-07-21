/**
 * useProfileSettings hook
 *
 * Owns profile form state and the updateMe API call.
 * Extracted from useSettings so callers that need only profile management
 * are not forced to depend on theme or feedback state (ISP).
 */

import { useState, useEffect, useMemo, useRef } from 'react';
import { Storage } from '../utils/Storage';
import { userService } from '../services/user';
import type { UpdateUserDto } from '../types/dto';
import { useAuth } from './useAuth';
import { useNotification } from './useNotification';
import { isValidUsername } from '../lib/validation';
import { isAxiosError } from 'axios';

export interface ProfileState {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  twoFactorEnabled: boolean;
}

export interface UseProfileSettingsReturn {
  profile: ProfileState;
  isLoading: boolean;
  handleProfileChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleProfileSubmit: (e: React.FormEvent) => Promise<void>;
  usernameAvailability: 'idle' | 'checking' | 'available' | 'unavailable' | 'error' | 'invalid';
  usernameAvailabilityMessage: string;
}

const readTwoFactorEnabled = (
  userData: Pick<
    NonNullable<ReturnType<typeof Storage.getUser>>,
    'twoFactorEnabled' | 'isTwoFacAuth'
  > & {
    twoFacAuth?: boolean;
  },
  fallback = false,
): boolean =>
  userData.twoFactorEnabled ?? userData.isTwoFacAuth ?? userData.twoFacAuth ?? fallback;

export const useProfileSettings = (): UseProfileSettingsReturn => {
  const { user, updateUser } = useAuth();
  const { showNotification } = useNotification();
  const [profile, setProfile] = useState<ProfileState>(() => {
    const stored = Storage.getUser();
    return {
      firstName: stored?.firstName ?? '',
      lastName: stored?.lastName ?? '',
      username: stored?.username ?? '',
      email: stored?.email ?? '',
      twoFactorEnabled: stored?.twoFactorEnabled ?? false,
    };
  });

  const [isLoading, setIsLoading] = useState(false);
  const [usernameAvailability, setUsernameAvailability] = useState<UseProfileSettingsReturn['usernameAvailability']>('idle');
  const [usernameAvailabilityMessage, setUsernameAvailabilityMessage] = useState('');
  const usernameRequestSequence = useRef(0);
  const currentUsername = useMemo(() => (user?.username ?? Storage.getUser()?.username ?? '').trim().toLowerCase(), [user?.username]);

  /** Refresh from the API so settings always reflect persisted server state. */
  useEffect(() => {
    const stored = Storage.getUser();
    const preservedRole = stored?.role ?? user?.role;

    (async () => {
      try {
        const userData = await userService.getMe();
        const twoFactorEnabled = readTwoFactorEnabled(userData, stored?.twoFactorEnabled ?? false);
        const mapped: ProfileState = {
          firstName: userData.firstName ?? '',
          lastName: userData.lastName ?? '',
          username: userData.username ?? '',
          email: userData.email ?? '',
          twoFactorEnabled,
        };
        setProfile(mapped);

        const nextUser = {
          id: stored?.id ?? user?.id,
          firstName: mapped.firstName,
          lastName: mapped.lastName,
          username: mapped.username,
          email: mapped.email,
          role: userData.role ?? preservedRole,
          isVerified: userData.isVerified,
          isTwoFacAuth: twoFactorEnabled,
          twoFactorEnabled,
        };
        Storage.setUser(nextUser);
        updateUser(nextUser);
      } catch (error) {
        console.error('Failed to fetch user data', error);
      }
    })();
  }, []);

  useEffect(() => {
    const normalized = profile.username.trim().toLowerCase();
    const requestSequence = ++usernameRequestSequence.current;
    if (!normalized || normalized === currentUsername) {
      setUsernameAvailability(normalized === currentUsername && normalized ? 'available' : 'idle');
      setUsernameAvailabilityMessage(normalized === currentUsername && normalized ? 'Username is available.' : '');
      return;
    }
    if (!isValidUsername(normalized)) {
      setUsernameAvailability('invalid');
      setUsernameAvailabilityMessage('Use 2–50 letters, numbers, or underscores.');
      return;
    }
    setUsernameAvailability('checking');
    setUsernameAvailabilityMessage('Checking availability...');
    const timeout = window.setTimeout(async () => {
      try {
        const response = await userService.checkUsernameAvailability(normalized);
        if (requestSequence !== usernameRequestSequence.current) return;
        setUsernameAvailability(response.available ? 'available' : 'unavailable');
        setUsernameAvailabilityMessage(response.available ? 'Username is available.' : 'This username is already in use.');
      } catch {
        if (requestSequence !== usernameRequestSequence.current) return;
        setUsernameAvailability('error');
        setUsernameAvailabilityMessage('Unable to check username availability.');
      }
    }, 400);
    return () => window.clearTimeout(timeout);
  }, [currentUsername, profile.username]);

  const handleProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setProfile((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      const stored = Storage.getUser();
      const normalizedUsername = profile.username.trim().toLowerCase();
      if (normalizedUsername !== currentUsername && usernameAvailability !== 'available') {
        setUsernameAvailabilityMessage(
          usernameAvailability === 'checking' ? 'Wait for username availability to finish.' :
            usernameAvailability === 'unavailable' ? 'This username is already in use.' :
              'Enter a valid available username.',
        );
        setIsLoading(false);
        return;
      }
      const updateDto: UpdateUserDto = {};
      let hasChanges = false;

      if (profile.firstName !== (stored?.firstName ?? '')) {
        updateDto.firstName = profile.firstName;
        hasChanges = true;
      }
      if (profile.lastName !== (stored?.lastName ?? '')) {
        updateDto.lastName = profile.lastName;
        hasChanges = true;
      }
      if (profile.username !== (stored?.username ?? '')) {
        updateDto.username = normalizedUsername;
        hasChanges = true;
      }
      if (profile.twoFactorEnabled !== (stored?.twoFactorEnabled ?? false)) {
        updateDto.enableTwoFactor = profile.twoFactorEnabled;
        hasChanges = true;
      }

      if (!hasChanges) {
        console.log('No changes to save');
        return;
      }

      const updated = await userService.updateMe(updateDto);
      const twoFactorEnabled = readTwoFactorEnabled(
        updated,
        profile.twoFactorEnabled,
      );

      setProfile((prev) => ({
        ...prev,
        firstName: updated.firstName ?? '',
        lastName: updated.lastName ?? '',
        username: updated.username ?? '',
        twoFactorEnabled,
      }));

      const currentStorage = Storage.getUser();
      if (currentStorage) {
        const nextUser = {
          ...currentStorage,
          firstName: updated.firstName ?? profile.firstName,
          lastName: updated.lastName ?? profile.lastName,
          username: updated.username ?? profile.username,
          role: updated.role ?? currentStorage.role ?? user?.role,
          isVerified: updated.isVerified,
          isTwoFacAuth: twoFactorEnabled,
          twoFactorEnabled,
        };
        Storage.setUser(nextUser);
        updateUser(nextUser);
      }

      showNotification({
        type: 'success',
        message: 'Profile settings updated successfully.',
      });
      console.log('Profile updated successfully');
    } catch (error: unknown) {
      const responseMessage = isAxiosError(error) ? String(error.response?.data ?? '') : '';
      if (isAxiosError(error) && error.response?.status === 409 && responseMessage.toLowerCase().includes('username')) {
        setUsernameAvailability('unavailable');
        setUsernameAvailabilityMessage('This username is already in use.');
      }
      showNotification({
        type: 'error',
        message: 'Failed to update profile settings. Please try again.',
        duration: 5000,
        showCloseButton: true,
      });
      console.error('Failed to update profile', error);
    } finally {
      setIsLoading(false);
    }
  };

  return { profile, isLoading, handleProfileChange, handleProfileSubmit, usernameAvailability, usernameAvailabilityMessage };
};
