import { useForm, type FieldErrors } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { intakeFormSchema, type IntakeFormData, type ReturnFormData, type ComplaintFormData } from '../lib/schemas';
import { useState } from 'react';
import { resizeImage, validateImage } from '../lib/imageUtils';

interface IntakeFormProps {
  onSubmit: (data: IntakeFormData, conversationId: string) => void;
  onError: (message: string) => void;
}

export function IntakeForm({ onSubmit, onError }: IntakeFormProps) {
  const [requestType, setRequestType] = useState<'RETURN' | 'COMPLAINT'>('RETURN');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    setValue,
    watch,
  } = useForm<IntakeFormData>({
    resolver: zodResolver(intakeFormSchema),
    defaultValues: {
      requestType: 'RETURN',
      unused: false,
    },
  });

  const watchedImages = watch('images');

  const handleRequestTypeChange = (type: 'RETURN' | 'COMPLAINT') => {
    setRequestType(type);
    setValue('requestType', type);
    if (type === 'RETURN') {
      reset({
        requestType: 'RETURN' as const,
        orderReceiptId: '',
        purchaseDate: '',
        unused: false,
        images: [],
      });
    } else {
      reset({
        requestType: 'COMPLAINT' as const,
        orderReceiptId: '',
        purchaseDate: '',
        defectDescription: '',
        images: [],
      });
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || []);
    
    // Validate files
    for (const file of files) {
      const validation = validateImage(file);
      if (!validation.valid) {
        onError(validation.error || 'Invalid image file');
        return;
      }
    }

    // Resize images
    try {
      const resizedFiles = await Promise.all(
        files.map((file) => resizeImage(file))
      );
      setValue('images', resizedFiles, { shouldValidate: true });
    } catch (error) {
      onError('Failed to process images. Please try again.');
    }
  };

  const onSubmitForm = async (data: IntakeFormData) => {
    setIsSubmitting(true);
    
    try {
      const formData = new FormData();
      formData.append('requestType', data.requestType);
      formData.append('orderReceiptId', data.orderReceiptId);
      formData.append('purchaseDate', data.purchaseDate);
      
      if (data.requestType === 'RETURN') {
        formData.append('unused', String(data.unused));
      } else {
        formData.append('defectDescription', data.defectDescription);
      }
      
      data.images.forEach((file) => {
        formData.append('images', file);
      });

      const response = await fetch('/api/returns/submit', {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();

      if (result.status === 'REJECTED') {
        onError(result.message);
        setIsSubmitting(false);
        return;
      }

      onSubmit(data, result.conversationId);
    } catch (error) {
      onError('Failed to submit request. Please try again.');
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmitForm)} className="space-y-6">
      {/* Request Type Selection */}
      <div>
        <label className="block text-sm font-medium mb-2">
          Request Type
        </label>
        <div className="flex gap-4">
          <button
            type="button"
            onClick={() => handleRequestTypeChange('RETURN')}
            className={`px-4 py-2 rounded ${
              requestType === 'RETURN'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            Return (30-day policy)
          </button>
          <button
            type="button"
            onClick={() => handleRequestTypeChange('COMPLAINT')}
            className={`px-4 py-2 rounded ${
              requestType === 'COMPLAINT'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700'
            }`}
          >
            Complaint (2-year warranty)
          </button>
        </div>
      </div>

      {/* Order/Receipt ID */}
      <div>
        <label className="block text-sm font-medium mb-2">
          Order/Receipt ID *
        </label>
        <input
          type="text"
          {...register('orderReceiptId')}
          className="w-full px-3 py-2 border rounded"
        />
        {errors.orderReceiptId && (
          <p className="text-red-500 text-sm mt-1">
            {errors.orderReceiptId.message}
          </p>
        )}
      </div>

      {/* Purchase Date */}
      <div>
        <label className="block text-sm font-medium mb-2">
          Purchase Date *
        </label>
        <input
          type="date"
          {...register('purchaseDate')}
          className="w-full px-3 py-2 border rounded"
          max={new Date().toISOString().split('T')[0]}
        />
        {errors.purchaseDate && (
          <p className="text-red-500 text-sm mt-1">
            {errors.purchaseDate.message}
          </p>
        )}
      </div>

      {/* Return-specific: Unused checkbox */}
      {requestType === 'RETURN' && (
        <div>
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              {...register('unused')}
              className="w-4 h-4"
            />
            <span>I confirm the item is unused *</span>
          </label>
          {(errors as FieldErrors<ReturnFormData>).unused && (
            <p className="text-red-500 text-sm mt-1">
              {(errors as FieldErrors<ReturnFormData>).unused?.message}
            </p>
          )}
        </div>
      )}

      {/* Complaint-specific: Defect Description */}
      {requestType === 'COMPLAINT' && (
        <div>
          <label className="block text-sm font-medium mb-2">
            Defect Description *
          </label>
          <textarea
            {...register('defectDescription')}
            rows={4}
            className="w-full px-3 py-2 border rounded"
            placeholder="Describe the defect in detail (minimum 10 characters)"
          />
          {(errors as FieldErrors<ComplaintFormData>).defectDescription && (
            <p className="text-red-500 text-sm mt-1">
              {(errors as FieldErrors<ComplaintFormData>).defectDescription?.message}
            </p>
          )}
        </div>
      )}

      {/* Image Upload */}
      <div>
        <label className="block text-sm font-medium mb-2">
          {requestType === 'RETURN' ? 'Receipt Image' : 'Defect Photos'} *
        </label>
        <input
          type="file"
          accept="image/jpeg,image/jpg,image/png,image/webp"
          multiple
          onChange={handleFileChange}
          className="w-full px-3 py-2 border rounded"
        />
        {watchedImages && watchedImages.length > 0 && (
          <p className="text-sm text-gray-600 mt-1">
            {watchedImages.length} image(s) selected
          </p>
        )}
        {errors.images && (
          <p className="text-red-500 text-sm mt-1">
            {errors.images.message}
          </p>
        )}
      </div>

      {/* Submit Button */}
      <button
        type="submit"
        disabled={isSubmitting}
        className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 disabled:opacity-50"
      >
        {isSubmitting ? 'Submitting...' : 'Submit Request'}
      </button>
    </form>
  );
}
