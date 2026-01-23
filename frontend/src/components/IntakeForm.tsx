import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import type { SessionContext } from '../App';
import { useState } from 'react';

const schema = z.object({
  orderId: z.string().min(5, "Order ID must be at least 5 chars"),
  intent: z.enum(["RETURN", "COMPLAINT"]),
  description: z.string().min(10, "Please describe the issue"),
});

type FormData = z.infer<typeof schema>;

export function IntakeForm({ onSubmit }: { onSubmit: (data: SessionContext) => void }) {
  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { intent: "RETURN" }
  });

  const [image, setImage] = useState<string | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const onFormSubmit = (data: FormData) => {
    if (!image) {
      alert("Please upload a photo.");
      return;
    }
    onSubmit({ ...data, image });
  };

  return (
    <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-6">
      <div className="text-center mb-8">
        <h2 className="text-xl font-semibold">Verification Request</h2>
        <p className="text-gray-500 text-sm">Please provide details to start the AI analysis.</p>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Order Number</label>
        <input {...register("orderId")} className="mt-1 block w-full rounded-md border-gray-300 shadow-sm border p-2 focus:ring-black focus:border-black" placeholder="e.g. PL-123456" />
        {errors.orderId && <p className="text-red-500 text-xs mt-1">{errors.orderId.message}</p>}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Request Type</label>
        <div className="mt-2 space-y-2">
          <label className="flex items-center p-3 border rounded cursor-pointer hover:bg-gray-50">
            <input type="radio" {...register("intent")} value="RETURN" className="form-radio text-black" />
            <div className="ml-3">
              <span className="block text-sm font-medium">Standard Return</span>
              <span className="block text-xs text-gray-500">Within 30 days. Unworn items only.</span>
            </div>
          </label>
          <label className="flex items-center p-3 border rounded cursor-pointer hover:bg-gray-50">
            <input type="radio" {...register("intent")} value="COMPLAINT" className="form-radio text-black" />
            <div className="ml-3">
              <span className="block text-sm font-medium">Complaint (Reklamacja)</span>
              <span className="block text-xs text-gray-500">2 years warranty for defects.</span>
            </div>
          </label>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Photo Evidence</label>
        <div className="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-md hover:bg-gray-50 transition">
          <div className="space-y-1 text-center">
            {image ? (
              <div className="relative">
                <img src={image} alt="Preview" className="mx-auto h-32 object-cover rounded" />
                <button type="button" onClick={() => setImage(null)} className="absolute top-0 right-0 bg-red-500 text-white rounded-full p-1 text-xs">X</button>
              </div>
            ) : (
              <>
                <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                  <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                <div className="flex text-sm text-gray-600 justify-center">
                  <label htmlFor="file-upload" className="relative cursor-pointer bg-white rounded-md font-medium text-black hover:text-gray-700 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-black">
                    <span>Upload a file</span>
                    <input id="file-upload" name="file-upload" type="file" className="sr-only" accept="image/*" onChange={handleFileChange} />
                  </label>
                </div>
                <p className="text-xs text-gray-500">PNG, JPG up to 5MB</p>
              </>
            )}
          </div>
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Description</label>
        <textarea {...register("description")} className="mt-1 block w-full rounded-md border-gray-300 shadow-sm border p-2 focus:ring-black focus:border-black" rows={4} placeholder="Describe the issue or reason for return..." />
        {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description.message}</p>}
      </div>

      <button type="submit" className="w-full bg-black text-white py-3 px-4 rounded font-medium hover:bg-gray-800 transition shadow-md">
        Proceed to Verification
      </button>
    </form>
  );
}