import { describe, it, expect } from 'vitest';
import { returnFormSchema } from '../../app/lib/schemas';

describe('returnFormSchema', () => {
  const validData = {
    productName: 'Sukienka letnia',
    type: 'return' as const,
    description: 'Produkt nie pasuje rozmiarem, jest za duży',
    photo: 'data:image/jpeg;base64,/9j/test',
    photoMimeType: 'image/jpeg',
  };

  it('accepts valid return data', () => {
    const result = returnFormSchema.safeParse(validData);
    expect(result.success).toBe(true);
  });

  it('accepts valid complaint data', () => {
    const result = returnFormSchema.safeParse({ ...validData, type: 'complaint' });
    expect(result.success).toBe(true);
  });

  it('rejects empty productName', () => {
    const result = returnFormSchema.safeParse({ ...validData, productName: '' });
    expect(result.success).toBe(false);
  });

  it('rejects description shorter than 20 chars', () => {
    const result = returnFormSchema.safeParse({ ...validData, description: 'za krótki' });
    expect(result.success).toBe(false);
  });

  it('rejects empty photo', () => {
    const result = returnFormSchema.safeParse({ ...validData, photo: '' });
    expect(result.success).toBe(false);
  });

  it('rejects invalid type', () => {
    const result = returnFormSchema.safeParse({ ...validData, type: 'invalid' as 'return' });
    expect(result.success).toBe(false);
  });

  it('returns Polish error for empty productName', () => {
    const result = returnFormSchema.safeParse({ ...validData, productName: '' });
    if (!result.success) {
      expect(result.error.issues[0].message).toContain('wymagana');
    }
  });

  it('returns Polish error for short description', () => {
    const result = returnFormSchema.safeParse({ ...validData, description: 'za krótki' });
    if (!result.success) {
      expect(result.error.issues[0].message).toContain('20');
    }
  });
});
