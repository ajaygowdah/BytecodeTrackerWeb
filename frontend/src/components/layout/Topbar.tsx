"use client";

import { useEffect, useState } from "react";
import { clearAuth, getRole } from "@/lib/auth";

export function Topbar() {
  const [role, setRole] = useState<string | null>(null);

  useEffect(() => {
    setRole(getRole());
  }, []);

  return (
    <header className="flex items-center justify-between border-b border-slate-800 px-6 py-4">
      <p className="text-sm text-slate-300">Role: {role ?? "Guest"}</p>
      <button
        className="rounded-lg border border-slate-600 px-3 py-1 text-sm text-slate-200"
        onClick={() => {
          clearAuth();
          window.location.href = "/login";
        }}
      >
        Logout
      </button>
    </header>
  );
}
