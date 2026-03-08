/**
 * SRP: this module's sole responsibility is detecting WebGL support.
 * Components (e.g. LiquidEther) read the result and decide what to render.
 */

/**
 * Returns true when the browser's canvas element can provide a WebGL or
 * WebGL2 rendering context.  Falls back gracefully on SSR or headless VMs.
 */
export function isWebGLAvailable(): boolean {
  if (typeof window === 'undefined' || typeof document === 'undefined') {
    return false;
  }
  try {
    const canvas = document.createElement('canvas');
    return !!(
      canvas.getContext('webgl2') ||
      canvas.getContext('webgl') ||
      // legacy prefix used by some browsers/drivers
      (canvas as HTMLCanvasElement).getContext('experimental-webgl')
    );
  } catch {
    return false;
  }
}
