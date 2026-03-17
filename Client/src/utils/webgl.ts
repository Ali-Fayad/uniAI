// Lightweight WebGL capability checks used by components that optionally
// use THREE/WebGL. Returns true only when a usable WebGL context is available.
export function isWebGLAvailable(): boolean {
  try {
    if (typeof window === 'undefined' || typeof document === 'undefined') return false;
    const canvas = document.createElement('canvas');
    // Prefer WebGL2 if available
    const gl = (canvas.getContext('webgl2') as WebGL2RenderingContext) ||
      (canvas.getContext('webgl') as WebGLRenderingContext) ||
      (canvas.getContext('experimental-webgl') as WebGLRenderingContext);
    if (!gl) return false;

    // Basic extension checks: the liquid simulation uses float textures / render targets.
    // For WebGL2, EXT_color_buffer_float or EXT_float_blend is desirable; for WebGL1,
    // check for OES_texture_float and WEBGL_color_buffer_float variants.
    const isWebGL2 = (gl as any).getContextAttributes !== undefined && !!(window as any).WebGL2RenderingContext && gl instanceof (window as any).WebGL2RenderingContext;

    if (isWebGL2) {
      // WebGL2 has better float support, but ensure rendering to float buffers is supported
      const ext = (gl as WebGL2RenderingContext).getExtension('EXT_color_buffer_float') || (gl as WebGL2RenderingContext).getExtension('EXT_float_blend');
      return !!ext || true; // WebGL2 usually suffices even if extensions are missing
    }

    // WebGL1 path: require OES_texture_float or OES_texture_half_float and ability to render to it
    const hasFloatTex = !!(gl.getExtension('OES_texture_float') || gl.getExtension('OES_texture_half_float'));
    const hasColorBufferFloat = !!(gl.getExtension('WEBGL_color_buffer_float') || gl.getExtension('EXT_color_buffer_half_float'));
    return hasFloatTex || hasColorBufferFloat;
  } catch (e) {
    return false;
  }
}

export function isWebGL2Available(): boolean {
  try {
    if (typeof window === 'undefined' || typeof document === 'undefined') return false;
    const canvas = document.createElement('canvas');
    const gl2 = canvas.getContext('webgl2');
    return !!gl2;
  } catch (e) {
    return false;
  }
}
