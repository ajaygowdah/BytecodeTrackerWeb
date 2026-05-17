"use client";

import { ReactNode, useEffect, useState } from "react";
import { getValidToken, isAdmin } from "@/lib/auth";

export function AuthGate({ children, adminOnly = false }: { children: ReactNode; adminOnly?: boolean }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const token = getValidToken();
    if (!token) {
      window.location.href = "/login";
      return;
    }
    if (adminOnly && !isAdmin()) {
      window.location.href = "/dashboard";
      return;
    }
    setReady(true);
  }, [adminOnly]);

  if (!ready) {
    return <div className="p-6 text-slate-300">Authorizing...</div>;
  }

  return <>{children}</>;
}
