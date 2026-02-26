import { z } from 'zod';

export const returnFormSchema = z.object({
  productName: z.string().min(1, 'Nazwa produktu jest wymagana'),
  type: z.enum(['return', 'complaint']),
  description: z.string().min(20, 'Opis musi mieć co najmniej 20 znaków'),
  photo: z.string().min(1, 'Zdjęcie jest wymagane'),
  photoMimeType: z.string(),
});

export type ReturnFormData = z.infer<typeof returnFormSchema>;
