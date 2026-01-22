import { z } from 'zod';

export const requestTypeSchema = z.enum(['RETURN', 'COMPLAINT']);

export const returnFormSchema = z.object({
  requestType: z.literal('RETURN'),
  orderReceiptId: z.string().min(1, 'Order/Receipt ID is required'),
  purchaseDate: z
    .string()
    .min(1, 'Purchase date is required')
    .refine(
      (date) => {
        const parsed = new Date(date);
        return !isNaN(parsed.getTime()) && parsed <= new Date();
      },
      { message: 'Purchase date must be a valid date and not in the future' }
    ),
  unused: z.boolean().refine((val) => val === true, {
    message: 'You must confirm the item is unused',
  }),
  images: z
    .array(z.instanceof(File))
    .min(1, 'At least one receipt image is required')
    .max(5, 'Maximum 5 images allowed'),
});

export const complaintFormSchema = z.object({
  requestType: z.literal('COMPLAINT'),
  orderReceiptId: z.string().min(1, 'Order/Receipt ID is required'),
  purchaseDate: z
    .string()
    .min(1, 'Purchase date is required')
    .refine(
      (date) => {
        const parsed = new Date(date);
        return !isNaN(parsed.getTime()) && parsed <= new Date();
      },
      { message: 'Purchase date must be a valid date and not in the future' }
    ),
  defectDescription: z
    .string()
    .min(10, 'Defect description must be at least 10 characters'),
  images: z
    .array(z.instanceof(File))
    .min(1, 'At least one defect photo is required')
    .max(5, 'Maximum 5 images allowed'),
});

export const intakeFormSchema = z.discriminatedUnion('requestType', [
  returnFormSchema,
  complaintFormSchema,
]);

export type ReturnFormData = z.infer<typeof returnFormSchema>;
export type ComplaintFormData = z.infer<typeof complaintFormSchema>;
export type IntakeFormData = z.infer<typeof intakeFormSchema>;
