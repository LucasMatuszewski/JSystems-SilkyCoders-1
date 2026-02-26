import { describe, it, expect, vi, afterEach } from 'vitest';
import { resizeImageToBase64 } from '../../app/lib/imageResize';

describe('resizeImageToBase64', () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('rejects unsupported file types', async () => {
    const file = new File(['content'], 'test.pdf', { type: 'application/pdf' });
    await expect(resizeImageToBase64(file)).rejects.toThrow('Unsupported file type');
  });

  it('accepts JPEG files', async () => {
    const mockCtx = { drawImage: vi.fn() };
    const mockCanvas = {
      getContext: vi.fn().mockReturnValue(mockCtx),
      toDataURL: vi.fn().mockReturnValue('data:image/jpeg;base64,/9j/test'),
      width: 0,
      height: 0,
    };
    vi.spyOn(document, 'createElement').mockReturnValue(mockCanvas as unknown as HTMLCanvasElement);

    // Use a proper constructor function so `new Image()` works
    let capturedInstance: { onload?: () => void; onerror?: () => void; src?: string } = {};
    const OriginalImage = global.Image;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (global as any).Image = function MockImage() {
      capturedInstance = this as { onload?: () => void; onerror?: () => void; src?: string };
    };

    // Also mock URL.createObjectURL and revokeObjectURL
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:mock-url');
    vi.spyOn(URL, 'revokeObjectURL').mockReturnValue(undefined);

    const file = new File(['jpeg content'], 'photo.jpg', { type: 'image/jpeg' });
    const promise = resizeImageToBase64(file);

    // Trigger image load via the captured instance
    if (capturedInstance.onload) capturedInstance.onload();

    const result = await promise;
    expect(result.mimeType).toBe('image/jpeg');
    expect(result.base64).toContain('/9j/test');

    global.Image = OriginalImage;
  });

  it('accepts PNG files', async () => {
    const mockCtx = { drawImage: vi.fn() };
    const mockCanvas = {
      getContext: vi.fn().mockReturnValue(mockCtx),
      toDataURL: vi.fn().mockReturnValue('data:image/png;base64,iVBtest'),
      width: 0,
      height: 0,
    };
    vi.spyOn(document, 'createElement').mockReturnValue(mockCanvas as unknown as HTMLCanvasElement);

    let capturedInstance: { onload?: () => void; onerror?: () => void; src?: string } = {};
    const OriginalImage = global.Image;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (global as any).Image = function MockImage() {
      capturedInstance = this as { onload?: () => void; onerror?: () => void; src?: string };
    };

    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:mock-url');
    vi.spyOn(URL, 'revokeObjectURL').mockReturnValue(undefined);

    const file = new File(['png content'], 'photo.png', { type: 'image/png' });
    const promise = resizeImageToBase64(file);
    if (capturedInstance.onload) capturedInstance.onload();
    const result = await promise;
    expect(result.mimeType).toBe('image/png');

    global.Image = OriginalImage;
  });
});
