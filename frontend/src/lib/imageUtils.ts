/**
 * Resize image to max 1024px on longest side before upload
 */
export async function resizeImage(file: File, maxSize: number = 1024): Promise<File> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    
    reader.onload = (e) => {
      const img = new Image();
      
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        
        // Calculate new dimensions
        if (width > height) {
          if (width > maxSize) {
            height = (height * maxSize) / width;
            width = maxSize;
          }
        } else {
          if (height > maxSize) {
            width = (width * maxSize) / height;
            height = maxSize;
          }
        }
        
        canvas.width = width;
        canvas.height = height;
        
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Could not get canvas context'));
          return;
        }
        
        ctx.drawImage(img, 0, 0, width, height);
        
        canvas.toBlob(
          (blob) => {
            if (!blob) {
              reject(new Error('Failed to create blob'));
              return;
            }
            const resizedFile = new File([blob], file.name, {
              type: file.type,
              lastModified: Date.now(),
            });
            resolve(resizedFile);
          },
          file.type,
          0.9
        );
      };
      
      img.onerror = () => reject(new Error('Failed to load image'));
      img.src = e.target?.result as string;
    };
    
    reader.onerror = () => reject(new Error('Failed to read file'));
    reader.readAsDataURL(file);
  });
}

/**
 * Validate image file type and size
 */
export function validateImage(file: File): { valid: boolean; error?: string } {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
  const maxSize = 5 * 1024 * 1024; // 5MB
  
  if (!validTypes.includes(file.type)) {
    return {
      valid: false,
      error: 'Invalid file type. Please upload JPG, PNG, or WebP images.',
    };
  }
  
  if (file.size > maxSize) {
    return {
      valid: false,
      error: 'File size exceeds 5MB. Please upload a smaller image.',
    };
  }
  
  return { valid: true };
}
