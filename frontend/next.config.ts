import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Hide the Next.js dev overlay button (the "N" circle in the corner).
  // It lives inside a shadow DOM that global CSS cannot reach, and it
  // conflicts visually with the Sinsay brand design.
  devIndicators: false,
};

export default nextConfig;
