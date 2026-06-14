export interface SafeUrlResult {
  href: string | null;
  display: string;
}

const allowedProtocols = new Set(['http:', 'https:']);

export const getSafeExternalUrl = (value?: string | null): SafeUrlResult => {
  const raw = value?.trim() ?? '';
  if (!raw) {
    return { href: null, display: '' };
  }

  try {
    const url = new URL(raw);
    if (!allowedProtocols.has(url.protocol)) {
      return { href: null, display: raw };
    }

    return { href: url.toString(), display: raw };
  } catch {
    return { href: null, display: raw };
  }
};
