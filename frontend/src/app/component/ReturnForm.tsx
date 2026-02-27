'use client';

import { useState, useCallback, useRef } from 'react';
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

/** Chip-style radio buttons for Zwrot / Reklamacja */
function TypeChip({
  value,
  label,
  selected,
  disabled,
  onChange,
}: {
  value: 'return' | 'complaint';
  label: string;
  selected: boolean;
  disabled: boolean;
  onChange: (v: 'return' | 'complaint') => void;
}) {
  return (
    <button
      type="button"
      role="radio"
      aria-checked={selected}
      disabled={disabled}
      onClick={() => !disabled && onChange(value)}
      className={[
        'px-4 py-2 rounded-full text-sm font-medium border transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E09243] focus-visible:ring-offset-1',
        selected
          ? 'bg-[#16181D] text-white border-[#16181D]'
          : 'bg-white text-[#16181D] border-[#C7C8C9] hover:border-[#16181D]',
        disabled ? 'opacity-60 cursor-not-allowed' : 'cursor-pointer',
      ].join(' ')}
    >
      {label}
    </button>
  );
}

export function ReturnForm({ args, status, respond }: ReturnFormProps) {
  const isReadOnly = status === 'complete';
  const fileInputRef = useRef<HTMLInputElement>(null);

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
  const descriptionLength = description.length;

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
      data-testid="return-form"
      onSubmit={handleSubmit}
      aria-label="Formularz zwrotu lub reklamacji"
      className="bg-white border border-[#E3E4E5] rounded-2xl shadow-sm w-full overflow-hidden"
      style={{ boxShadow: '0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.06)' }}
    >
      {/* Card header */}
      <div className="bg-[#F1F2F4] px-5 py-3 border-b border-[#E3E4E5] flex items-center gap-2">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="#16181D"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          aria-hidden="true"
        >
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" y1="13" x2="8" y2="13" />
          <line x1="16" y1="17" x2="8" y2="17" />
          <polyline points="10 9 9 9 8 9" />
        </svg>
        <h3 className="text-sm font-semibold text-[#16181D] tracking-wide">
          Formularz Zwrotu / Reklamacji
        </h3>
      </div>

      <div className="px-5 py-4 flex flex-col gap-4">

        {/* Type selector — chip buttons */}
        <div>
          {/*
            Accessibility note:
            - <label htmlFor="type"> associates with the hidden <select> for getByLabelText tests
            - The radiogroup has a distinct aria-label so it does NOT conflict with getByLabelText(/typ zgłoszenia/)
          */}
          <label htmlFor="type" className="block text-xs font-medium text-[#7B7D80] mb-2 uppercase tracking-wider">
            Typ zgłoszenia *
          </label>
          {/* Hidden select for form semantics / test compatibility */}
          <select
            data-testid="form-type"
            id="type"
            value={type}
            onChange={e => setType(e.target.value as 'return' | 'complaint')}
            disabled={isReadOnly}
            className="sr-only"
          >
            <option value="return">Zwrot</option>
            <option value="complaint">Reklamacja</option>
          </select>
          <div
            role="radiogroup"
            aria-label="Zwrot lub reklamacja"
            className="flex gap-2"
          >
            <TypeChip
              value="return"
              label="Zwrot"
              selected={type === 'return'}
              disabled={isReadOnly}
              onChange={setType}
            />
            <TypeChip
              value="complaint"
              label="Reklamacja"
              selected={type === 'complaint'}
              disabled={isReadOnly}
              onChange={setType}
            />
          </div>
          {errors.type && (
            <p role="alert" className="text-xs text-[#FF0023] mt-1">{errors.type}</p>
          )}
        </div>

        {/* Product Name */}
        <div>
          <label htmlFor="productName" className="block text-xs font-medium text-[#7B7D80] mb-1.5 uppercase tracking-wider">
            Nazwa produktu *
          </label>
          <input
            data-testid="form-product-name"
            id="productName"
            type="text"
            value={productName}
            onChange={e => setProductName(e.target.value)}
            disabled={isReadOnly}
            placeholder="np. Sukienka letnia, T-shirt basic"
            className={[
              'w-full border rounded-md px-3 py-2.5 text-sm text-[#16181D] transition-colors duration-150',
              'placeholder:text-[#AFB0B2]',
              'focus:outline-none focus:border-[#E09243] focus:ring-2 focus:ring-[#E09243]/20',
              'disabled:bg-[#F1F2F4] disabled:text-[#7B7D80] disabled:cursor-not-allowed',
              errors.productName ? 'border-[#FF0023] ring-2 ring-[#FF0023]/20' : 'border-[#C7C8C9]',
            ].join(' ')}
          />
          {errors.productName && (
            <p role="alert" className="text-xs text-[#FF0023] mt-1 flex items-center gap-1">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {errors.productName}
            </p>
          )}
        </div>

        {/* Description */}
        <div>
          <label htmlFor="description" className="block text-xs font-medium text-[#7B7D80] mb-1.5 uppercase tracking-wider">
            Opis problemu *
          </label>
          <textarea
            data-testid="form-description"
            id="description"
            value={description}
            onChange={e => setDescription(e.target.value)}
            disabled={isReadOnly}
            rows={3}
            placeholder="Opisz szczegółowo powód zwrotu lub reklamacji…"
            className={[
              'w-full border rounded-md px-3 py-2.5 text-sm text-[#16181D] transition-colors duration-150 resize-none',
              'placeholder:text-[#AFB0B2]',
              'focus:outline-none focus:border-[#E09243] focus:ring-2 focus:ring-[#E09243]/20',
              'disabled:bg-[#F1F2F4] disabled:text-[#7B7D80] disabled:cursor-not-allowed',
              errors.description ? 'border-[#FF0023] ring-2 ring-[#FF0023]/20' : 'border-[#C7C8C9]',
            ].join(' ')}
          />
          <div className="flex items-center justify-between mt-1">
            {errors.description ? (
              <p role="alert" className="text-xs text-[#FF0023] flex items-center gap-1">
                <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                {errors.description}
              </p>
            ) : (
              <span />
            )}
            {!isReadOnly && (
              <span className={`text-xs tabular-nums ${descriptionLength < 20 ? 'text-[#AFB0B2]' : 'text-[#0DB209]'}`}>
                {descriptionLength}/20 min.
              </span>
            )}
          </div>
        </div>

        {/* Photo upload */}
        <div>
          <span className="block text-xs font-medium text-[#7B7D80] mb-1.5 uppercase tracking-wider">
            Zdjęcie produktu *
          </span>

          {!isReadOnly && (
            <>
              {/* Hidden real file input — accessible via label */}
              <input
                data-testid="form-photo-upload"
                id="photo"
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={handlePhotoChange}
                disabled={isReadOnly || isProcessingPhoto}
                aria-label="Zdjęcie produktu"
                className="sr-only"
              />
              {/* Visible styled trigger button */}
              <label
                htmlFor="photo"
                className={[
                  'flex items-center justify-center gap-2 w-full border-2 border-dashed rounded-xl px-4 py-4',
                  'text-sm font-medium transition-colors duration-150 cursor-pointer',
                  photoFileName
                    ? 'border-[#0DB209]/40 bg-[#0DB209]/5 text-[#0A8C07]'
                    : 'border-[#C7C8C9] bg-[#F1F2F4] text-[#7B7D80] hover:border-[#16181D] hover:bg-[#E3E4E5]/50',
                  errors.photo ? 'border-[#FF0023] bg-[#FF0023]/5 text-[#FF0023]' : '',
                  isProcessingPhoto ? 'opacity-60 cursor-wait' : '',
                ].join(' ')}
              >
                {isProcessingPhoto ? (
                  <>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="animate-spin" aria-hidden="true"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg>
                    Przetwarzanie…
                  </>
                ) : photoFileName ? (
                  <>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
                    <span className="truncate max-w-[200px]">{photoFileName}</span>
                    <span className="text-xs text-[#0A8C07]/70 ml-auto shrink-0">Zmień</span>
                  </>
                ) : (
                  <>
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                    Dodaj zdjęcie produktu
                  </>
                )}
              </label>
            </>
          )}

          {/* Read-only: show photo name */}
          {isReadOnly && photoFileName && (
            <div className="flex items-center gap-2 text-sm text-[#7B7D80] bg-[#F1F2F4] rounded-lg px-3 py-2">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
              <span className="truncate">{photoFileName}</span>
            </div>
          )}

          {errors.photo && (
            <p role="alert" className="text-xs text-[#FF0023] mt-1.5 flex items-center gap-1">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
              {errors.photo}
            </p>
          )}
        </div>

        {/* Submit button */}
        {!isReadOnly && (
          <button
            data-testid="form-submit-btn"
            type="submit"
            disabled={!isComplete || isProcessingPhoto}
            className={[
              'w-full rounded-full py-3 px-6 text-sm font-medium transition-all duration-150',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#16181D] focus-visible:ring-offset-2',
              isComplete && !isProcessingPhoto
                ? 'bg-[#16181D] text-white hover:bg-[#303133] active:scale-[0.98]'
                : 'bg-[#C8C9CC] text-white cursor-not-allowed',
            ].join(' ')}
          >
            Wyślij do asystenta
          </button>
        )}

        {/* Frozen / read-only state indicator */}
        {isReadOnly && (
          <div className="flex items-center justify-center gap-2 py-2">
            <div className="flex items-center justify-center w-5 h-5 rounded-full bg-[#0DB209]">
              <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
            <p className="text-sm font-medium text-[#0A8C07]">Formularz wysłany — analizuję zgłoszenie</p>
          </div>
        )}
      </div>
    </form>
  );
}
