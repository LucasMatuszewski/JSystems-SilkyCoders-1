import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import type { SessionContext } from '../App';
import { useState } from 'react';

const schema = z.object({
  orderId: z.string().min(5, 'Numer zamówienia musi mieć co najmniej 5 znaków'),
  intent: z.enum(['RETURN', 'COMPLAINT']),
  description: z.string().min(10, 'Opis musi mieć co najmniej 10 znaków'),
});

type FormData = z.infer<typeof schema>;

export function IntakeForm({
  onSubmit,
}: {
  onSubmit: (data: SessionContext) => void;
}) {
  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { intent: 'RETURN' },
  });

  const watchIntent = watch('intent');

  const [image, setImage] = useState<string | null>(null);

  const resizeImage = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.src = URL.createObjectURL(file);
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        const maxDim = 1024;

        if (width > maxDim || height > maxDim) {
          if (width > height) {
            height = Math.round((height * maxDim) / width);
            width = maxDim;
          } else {
            width = Math.round((width * maxDim) / height);
            height = maxDim;
          }
        }

        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.drawImage(img, 0, 0, width, height);
          // Default to JPEG for better compression if original was JPEG, else PNG
          const type = file.type === 'image/jpeg' ? 'image/jpeg' : 'image/png';
          resolve(canvas.toDataURL(type, 0.8)); // 0.8 quality for JPEG
        } else {
          reject(new Error('Canvas context not available'));
        }
        URL.revokeObjectURL(img.src);
      };
      img.onerror = (err) => reject(err);
    });
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      try {
        const resized = await resizeImage(file);
        setImage(resized);
      } catch (err) {
        console.error('Failed to resize image', err);
        // Fallback to original if resize fails
        const reader = new FileReader();
        reader.onloadend = () => {
          setImage(reader.result as string);
        };
        reader.readAsDataURL(file);
      }
    }
  };

  const onFormSubmit = (data: FormData) => {
    if (!image) {
      alert('Please upload a photo.');
      return;
    }
    onSubmit({ ...data, image });
  };

  return (
    <form
      onSubmit={handleSubmit(onFormSubmit)}
      className="space-y-10 animate-slide-up"
    >
      <div className="space-y-4">
        <div>
          <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">
            Numer zamówienia
          </label>
          <input
            {...register('orderId')}
            className="sinsay-input"
            placeholder="np. PL-123456"
          />
          {errors.orderId && (
            <p className="text-red-500 text-[10px] mt-2 uppercase font-bold tracking-tight">
              {errors.orderId.message}
            </p>
          )}
        </div>

        <div>
          <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">
            Typ zgłoszenia
          </label>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label
              className={`flex flex-col p-4 border transition-all cursor-pointer hover:border-black ${watchIntent === 'RETURN' ? 'border-black bg-gray-50' : 'border-gray-200'}`}
            >
              <div className="flex items-center mb-1">
                <input
                  type="radio"
                  {...register('intent')}
                  value="RETURN"
                  className="accent-black"
                />
                <span className="ml-3 text-sm font-bold uppercase tracking-wide">
                  Zwrot standardowy
                </span>
              </div>
              <span className="ml-7 text-[10px] text-gray-500 leading-tight">
                Do 30 dni. Produkty nieużywane z metkami.
              </span>
            </label>
            <label
              className={`flex flex-col p-4 border transition-all cursor-pointer hover:border-black ${watchIntent === 'COMPLAINT' ? 'border-black bg-gray-50' : 'border-gray-200'}`}
            >
              <div className="flex items-center mb-1">
                <input
                  type="radio"
                  {...register('intent')}
                  value="COMPLAINT"
                  className="accent-black"
                />
                <span className="ml-3 text-sm font-bold uppercase tracking-wide">
                  Reklamacja
                </span>
              </div>
              <span className="ml-7 text-[10px] text-gray-500 leading-tight">
                Wada produktu. 2 lata rękojmi.
              </span>
            </label>
          </div>
        </div>

        <div>
          <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">
            Zdjęcie produktu / wady
          </label>
          <div className="relative group">
            {image ? (
              <div className="relative aspect-video rounded-none overflow-hidden border border-gray-200">
                <img
                  src={image}
                  alt="Preview"
                  className="w-full h-full object-cover"
                />
                <button
                  type="button"
                  onClick={() => setImage(null)}
                  className="absolute top-2 right-2 bg-black text-white w-8 h-8 flex items-center justify-center font-bold hover:bg-red-600 transition-colors"
                >
                  ✕
                </button>
              </div>
            ) : (
              <label className="flex flex-col items-center justify-center aspect-video border-2 border-dashed border-gray-200 hover:border-black transition-colors cursor-pointer bg-gray-50 group-hover:bg-white">
                <svg
                  className="w-12 h-12 text-gray-300 group-hover:text-black transition-colors"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="1"
                    d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
                  />
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="1"
                    d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                <span className="mt-4 text-[10px] font-bold uppercase tracking-widest text-gray-400 group-hover:text-black">
                  Dodaj zdjęcie
                </span>
                <input
                  type="file"
                  className="hidden"
                  accept="image/*"
                  onChange={handleFileChange}
                />
              </label>
            )}
          </div>
        </div>

        <div>
          <label className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-2">
            Opis zgłoszenia
          </label>
          <textarea
            {...register('description')}
            className="sinsay-input !rounded-2xl min-h-[120px] resize-none"
            placeholder="Opisz krótko przyczynę zwrotu lub wadę produktu..."
          />
          {errors.description && (
            <p className="text-red-500 text-[10px] mt-2 uppercase font-bold tracking-tight">
              {errors.description.message}
            </p>
          )}
        </div>
      </div>

      <button
        type="submit"
        className="sinsay-button w-full !py-4 text-base tracking-widest hover:scale-[1.01] active:scale-[0.99] transition-all"
      >
        Rozpocznij weryfikację
      </button>
    </form>
  );
}
