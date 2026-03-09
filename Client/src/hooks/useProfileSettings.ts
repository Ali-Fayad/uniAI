/**
 * useProfileSettings hook
 *
 * Owns profile form state and the updateMe API call.
 * Extracted from useSettings so callers that need only profile management
 * are not forced to depend on theme or feedback state (ISP).
 */

import { useState, useEffect } from 'react';
import { Storage } from '../utils/Storage';
import { userService } from '../services/user';
import type { UpdateUserDto } from '../types/dto';

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
}

export const useProfileSettings = (): UseProfileSettingsReturn => {
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

  /** Fetch from API if storage has no user data. */
  useEffect(() => {
    if (!Storage.getUser()) {
      (async () => {
        try {
          const userData = await userService.getMe();
          const mapped: ProfileState = {
            firstName: userData.firstName ?? '',
            lastName: userData.lastName ?? '',
            username: userData.username ?? '',
            email: userData.email ?? '',
            twoFactorEnabled: userData.isTwoFacAuth,
          };
          setProfile(mapped);
          Storage.setUser({
            id: 0,
            firstName: userData.firstName,
            lastName: userData.lastName,
            username: userData.username,
            email: userData.email,
            twoFactorEnabled: userData.isTwoFacAuth,
          });
        } catch (error) {
          console.error('Failed to fetch user data', error);
        }
      })();
    }
  }, []);

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
        updateDto.username = profile.username;
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

      setProfile((prev) => ({
        ...prev,
        firstName: updated.firstName ?? '',
        lastName: updated.lastName ?? '',
        username: updated.username ?? '',
      }));

      const currentStorage = Storage.getUser();
      if (currentStorage) {
        Storage.setUser({
          ...currentStorage,
          firstName: updated.firstName,
          lastName: updated.lastName,
          username: updated.username,
          twoFactorEnabled: updated.isTwoFacAuth,
        });
      }

      console.log('Profile updated successfully');
    } catch (error) {
      console.error('Failed to update profile', error);
    } finally {
      setIsLoading(false);
    }
  };

  return { profile, isLoading, handleProfileChange, handleProfileSubmit };
};
