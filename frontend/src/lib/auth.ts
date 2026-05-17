const TOKEN_KEY = "bt_token";
const ROLE_KEY = "bt_role";

interface JwtPayload {
  exp?: number;
  role?: string;
}

export function saveAuth(token: string, role: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(ROLE_KEY, role);
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ROLE_KEY);
}

export function getToken(): string | null {
  return typeof window === "undefined" ? null : localStorage.getItem(TOKEN_KEY);
}

function decodePayload(token: string): JwtPayload | null {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;

    const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(normalized);
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

export function getValidToken(): string | null {
  const token = getToken();
  if (!token) return null;

  const payload = decodePayload(token);
  if (!payload) {
    clearAuth();
    return null;
  }

  if (typeof payload.exp === "number") {
    const nowSeconds = Math.floor(Date.now() / 1000);
    if (payload.exp <= nowSeconds) {
      clearAuth();
      return null;
    }
  }

  return token;
}

export function getRole(): string | null {
  if (typeof window === "undefined") return null;

  const token = getToken();
  if (token) {
    const payload = decodePayload(token);
    if (payload?.role) {
      return payload.role;
    }
  }

  return localStorage.getItem(ROLE_KEY);
}

export function isAdmin(): boolean {
  const role = getRole();
  if (!role) return false;
  return role === "ADMIN" || role === "ROLE_ADMIN";
}
