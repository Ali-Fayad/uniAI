import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const isValidPhoneNumber = (phone: string): boolean => {
  // Must start with +, followed by digits (country code), space, then remaining number
  const phoneRegex = /^\+\d+ \d+$/;
  return phoneRegex.test(phone);
};
