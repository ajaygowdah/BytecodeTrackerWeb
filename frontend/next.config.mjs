/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // REMOVED: the rewrites() proxy that was here before.
  //
  // Root cause of HTTP 403 on file upload:
  //   Next.js processes rewrites() server-side (even for "use client" components).
  //   When it proxies /api/* → http://localhost:8080/api/*, it strips the
  //   Authorization header. Spring Boot receives an unauthenticated multipart
  //   request and returns 403.
  //
  // Fix: NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 in .env.local makes
  //   the browser send fetch() directly to Spring Boot — no proxy, no header loss.
  //   CORS is handled by Spring Boot's CorsConfig (allows localhost:*).
};

export default nextConfig;
