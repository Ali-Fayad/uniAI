import { useState, useCallback } from 'react';
import { userService } from '../services/user';
import { Storage } from '../utils/Storage';
import type { UpdateUserDto, AuthenticationResponseDto } from '../types/dto';

/**
 * DIP: SettingsPage depends on this hook's interface rather than the concrete
 * userService or Storage implementations.
 *
 * SRP: this hook manages user-profile loading and updating only.
 * Theme and feedback state remain in their own hooks/components.
 */
export interface ProfileState {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  twoFactorEnabled: boolean;
}

/** Accepts either a new value or a functional updater — mirrors the React setState signature. */
type ProfileUpdater = (value: ProfileState | ((prev: ProfileState) => ProfileState)) => void;

export interface FeedbackPayload {
  rating: number;
  comment: string;
}

export interface UseUserProfileReturn {
  profile: ProfileState;
  isLoading: boolean;
  setProfile: ProfileUpdater;
  saveProfile: (incoming: ProfileState) => Promise<void>;
  /** DIP: callers don't need to import userService directly. */
  submitFeedback: (payload: FeedbackPayload) => Promise<void>;
}

function fromStorageOrEmpty(): ProfileState {
  const stored = Storage.getUser();
  return {
    firstName: stored?.firstName ?? '',
    lastName: stored?.lastName ?? '',
    username: stored?.username ?? '',
    email: stored?.email ?? '',
    twoFactorEnabled: stored?.twoFactorEnabled ?? false,
  };
}

function mapApiResponse(dto: AuthenticationResponseDto): Partial<ProfileState> {
  return {
    firstName: dto.firstName ?? '',
    lastName: dto.lastName ?? '',
    username: dto.username ?? '',
    email: dto.email ?? '',
    twoFactorEnabled: dto.isTwoFacAuth,
  };
}

export function useUserProfile(): UseUserProfileReturn {
  const [profile, setProfile] = useState<ProfileState>(fromStorageOrEmpty);
  const [isLoading, setIsLoading] = useState(false);

  const saveProfile = useCallback(async (incoming: ProfileState) => {
    setIsLoading(true);
    try {
      const stored = Storage.getUser();

      // Only send fields that actually changed (minimise payload)
      const updateDto: UpdateUserDto = {};
      if (incoming.firstName !== (stored?.firstName ?? '')) updateDto.firstName = incoming.firstName;
      if (incoming.lastName !== (stored?.lastName ?? ''))   updateDto.lastName  = incoming.lastName;
      if (incoming.username !== (stored?.username ?? ''))   updateDto.username  = incoming.username;
      if (incoming.twoFactorEnabled !== (stored?.twoFactorEnabled ?? false)) {
        updateDto.enableTwoFactor = incoming.twoFactorEnabled;
      }

      if (Object.keys(updateDto).length === 0) return; // nothing changed

      const updatedUser = await userService.updateMe(updateDto);

      const mapped = mapApiResponse(updatedUser);
      setProfile((prev: ProfileState) => ({ ...prev, ...mapped }));

      // Sync storage
      if (stored) {
        Storage.setUser({
          ...stored,
          ...mapped,
        });
      }
    } catch (error) {
      console.error('Failed to update profile:', error);
      throw error; // re-throw so the component can show an error state if needed
    } finally {
      setIsLoading(false);
    }
  }, []);

  const submitFeedback = useCallback(
    async ({ rating, comment }: FeedbackPayload) => {
      const commentWithRating = `Rating: ${rating}/5. ${comment}`;
      await userService.sendFeedback({
        email: profile.email,
        comment: commentWithRating,
      });
    },
    [profile.email],
  );

  return { profile, isLoading, setProfile, saveProfile, submitFeedback };
}
