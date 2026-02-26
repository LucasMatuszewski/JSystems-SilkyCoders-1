'use client';

import { useState, useCallback } from 'react';
import { returnFormSchema } from '../lib/schemas';
import { resizeImageToBase64 } from '../lib/imageResize';

interface ReturnFormProps {
  args: { type?: string };
  status: 'inProgress' | 'complete' | 'executing';
  respond: (response: string) => void;
}

interface FormErrors {
  productName?: string;
  type?: string;
  description?: string;
  photo?: string;
}

export function ReturnForm({ args, status, respond }: ReturnFormProps) {
  const isReadOnly = status === 'complete';

  const [productName, setProductName] = useState('');
  const [type, setType] = useState<'return' | 'complaint'>(
    args.type === 'complaint' ? 'complaint' : 'return'
  );
  const [description, setDescription] = useState('');
  const [photo, setPhoto] = useState('');
  const [photoMimeType, setPhotoMimeType] = useState('');
  const [photoFileName, setPhotoFileName] = useState('');
  const [errors, setErrors] = useState<FormErrors>({});
  const [isProcessingPhoto, setIsProcessingPhoto] = useState(false);

  const isComplete = !!productName && !!description && !!photo;

  const handlePhotoChange = useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsProcessingPhoto(true);
    setErrors(prev => ({ ...prev, photo: undefined }));
    try {
      const resized = await resizeImageToBase64(file);
      setPhoto(resized.base64);
      setPhotoMimeType(resized.mimeType);
      setPhotoFileName(file.name);
    } catch {
      setErrors(prev => ({ ...prev, photo: 'Nieobsługiwany format pliku. Użyj JPEG, PNG lub WebP.' }));
    } finally {
      setIsProcessingPhoto(false);
    }
  }, []);

  const handleSubmit = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    const data = { productName, type, description, photo, photoMimeType };
    const result = returnFormSchema.safeParse(data);

    if (!result.success) {
      const newErrors: FormErrors = {};
      result.error.issues.forEach(issue => {
        const field = issue.path[0] as keyof FormErrors;
        if (!newErrors[field]) {
          newErrors[field] = issue.message;
        }
      });
      setErrors(newErrors);
      return;
    }

    respond(JSON.stringify(result.data));
  }, [productName, type, description, photo, photoMimeType, respond]);

  return (
    <form
      onSubmit={handleSubmit}
      aria-label="Formularz zwrotu lub reklamacji"
      className="border border-[#C7C8C9] rounded-lg p-4 shadow-sm bg-white w-full max-w-md"
    >
      <h3 className="text-xs font-semibold text-[#16181D] mb-4 uppercase tracking-wide">
        Formularz Zwrotu / Reklamacji
      </h3>

      {/* Product Name */}
      <div className="mb-3">
        <label htmlFor="productName" className="block text-xs font-medium text-[#494A4D] mb-1">
          Nazwa produktu *
        </label>
        <input
          id="productName"
          type="text"
          value={productName}
          onChange={e => setProductName(e.target.value)}
          disabled={isReadOnly}
          placeholder="np. Sukienka letnia"
          className="w-full border border-[#C7C8C9] rounded px-3 py-2 text-sm text-[#16181D] focus:outline-none focus:border-[#E09243] focus:ring-1 focus:ring-[#E09243] disabled:bg-[#F1F2F4] disabled:text-[#7B7D80] transition-colors"
        />
        {errors.productName && (
          <p role="alert" className="text-xs text-[#FF0023] mt-1">{errors.productName}</p>
        )}
      </div>

      {/* Type Select */}
      <div className="mb-3">
        <label htmlFor="type" className="block text-xs font-medium text-[#494A4D] mb-1">
          Typ zgłoszenia *
        </label>
        <select
          id="type"
          value={type}
          onChange={e => setType(e.target.value as 'return' | 'complaint')}
          disabled={isReadOnly}
          className="w-full border border-[#C7C8C9] rounded px-3 py-2 text-sm text-[#16181D] focus:outline-none focus:border-[#E09243] focus:ring-1 focus:ring-[#E09243] disabled:bg-[#F1F2F4] transition-colors"
        >
          <option value="return">Zwrot</option>
          <option value="complaint">Reklamacja</option>
        </select>
      </div>

      {/* Description */}
      <div className="mb-3">
        <label htmlFor="description" className="block text-xs font-medium text-[#494A4D] mb-1">
          Opis problemu *
        </label>
        <textarea
          id="description"
          value={description}
          onChange={e => setDescription(e.target.value)}
          disabled={isReadOnly}
          rows={3}
          placeholder="Opisz szczegółowo powód zwrotu lub reklamacji (min. 20 znaków)"
          className="w-full border border-[#C7C8C9] rounded px-3 py-2 text-sm text-[#16181D] focus:outline-none focus:border-[#E09243] focus:ring-1 focus:ring-[#E09243] disabled:bg-[#F1F2F4] disabled:text-[#7B7D80] resize-none transition-colors"
        />
        {errors.description && (
          <p role="alert" className="text-xs text-[#FF0023] mt-1">{errors.description}</p>
        )}
      </div>

      {/* Photo */}
      <div className="mb-4">
        <label htmlFor="photo" className="block text-xs font-medium text-[#494A4D] mb-1">
          Zdjęcie produktu *
        </label>
        {!isReadOnly && (
          <input
            id="photo"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            onChange={handlePhotoChange}
            disabled={isReadOnly || isProcessingPhoto}
            className="w-full text-sm text-[#7B7D80] file:mr-3 file:py-1 file:px-3 file:border file:border-[#C7C8C9] file:rounded file:text-xs file:font-medium file:bg-[#F1F2F4] hover:file:bg-[#E3E4E5] file:cursor-pointer file:transition-colors"
          />
        )}
        {photoFileName && (
          <p className="text-xs text-[#0DB209] mt-1">{photoFileName} ✓</p>
        )}
        {isProcessingPhoto && (
          <p className="text-xs text-[#7B7D80] mt-1">Przetwarzanie zdjęcia...</p>
        )}
        {errors.photo && (
          <p role="alert" className="text-xs text-[#FF0023] mt-1">{errors.photo}</p>
        )}
      </div>

      {/* Submit */}
      {!isReadOnly && (
        <button
          type="submit"
          disabled={!isComplete || isProcessingPhoto}
          className="w-full bg-[#16181D] text-white py-2 px-4 rounded text-sm font-medium hover:bg-[#303133] disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
        >
          Wyślij do asystenta
        </button>
      )}

      {isReadOnly && (
        <p className="text-xs text-[#7B7D80] text-center mt-2">Formularz wysłany ✓</p>
      )}
    </form>
  );
}
