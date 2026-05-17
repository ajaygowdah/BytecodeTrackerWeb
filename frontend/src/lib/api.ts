import { clearAuth, getValidToken } from "@/lib/auth";

// NEXT_PUBLIC_* are statically inlined at Next.js build/dev startup.
// With .env.local setting NEXT_PUBLIC_API_BASE_URL=http://localhost:8080,
// all requests go directly from the browser to Spring Boot —
// avoiding the Next.js rewrite proxy that strips Authorization headers
// on multipart POST requests.
const API_BASE = (process.env.NEXT_PUBLIC_API_BASE_URL ?? "").replace(/\/$/, "");

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  // getValidToken() drops malformed/expired JWTs before the request.
  const token = getValidToken();
  const headers = new Headers(init.headers);

  // Never set Content-Type for FormData — the browser must set it automatically
  // so it includes the multipart boundary. Setting it manually breaks uploads.
  if (!headers.has("Content-Type") && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers,
  });

  if (response.status === 401 && !path.startsWith("/api/auth/")) {
    clearAuth();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  }

  if (!response.ok) {
    const text = await response.text().catch(() => "");
    // Try to surface the Spring Boot error message (JSON body)
    try {
      const json = JSON.parse(text);
      // If validation errors are present, build a friendly message
      if (Array.isArray(json.errors) && json.errors.length > 0) {
        const parts = json.errors.map((e: any) => {
          const field = e.field ?? e.objectName ?? "error";
          const msg = e.defaultMessage ?? e.message ?? JSON.stringify(e);
          return `${field}: ${msg}`;
        });
        throw new Error(parts.join("; "));
      }
      const msg = json.message ?? json.error ?? `HTTP ${response.status}`;
      throw new Error(msg);
    } catch {
      throw new Error(text || `HTTP ${response.status}`);
    }
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return (await response.json()) as T;
  }
  return (await response.text()) as unknown as T;
}
