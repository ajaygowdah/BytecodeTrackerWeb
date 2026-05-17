"use client";

import { FormEvent, useState } from "react";
import { apiRequest } from "@/lib/api";
import { saveAuth } from "@/lib/auth";
import { LoginResponse } from "@/lib/types";
import { Button } from "@/components/ui/Button";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function onSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      const data = await apiRequest<LoginResponse>("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ username, password })
      });
      saveAuth(data.token, data.role);
      window.location.href = "/dashboard";
    } catch (err) {
      setError((err as Error).message);
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* Animated gradient border wrapper */}
        <div className="relative rounded-2xl p-1 animate-border-spin" style={{
          background: "linear-gradient(90deg, #1ecbe1, #ff5d5d, #f1c74a, #4ecb71, #1ecbe1)",
          backgroundSize: "200% 200%"
        }}>
          <div className="card p-8 !bg-slate-950/95 !border-0">
            {/* Logo */}
            <div className="mb-6 flex justify-center">
              <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-accent/20 text-accent">
                <svg
                  className="h-7 w-7"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  />
                </svg>
              </div>
            </div>

            <h1 className="text-center text-3xl font-heading text-white">Login</h1>
            <form className="mt-6 space-y-4" onSubmit={onSubmit}>
              <input
                className="w-full rounded-lg bg-slate-900 p-3 text-slate-100 placeholder-slate-400 transition focus:outline-none focus:ring-2 focus:ring-accent"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <input
                className="w-full rounded-lg bg-slate-900 p-3 text-slate-100 placeholder-slate-400 transition focus:outline-none focus:ring-2 focus:ring-accent"
                placeholder="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              {error ? <p className="text-sm text-danger">{error}</p> : null}
              <Button type="submit" className="w-full">Sign in</Button>
            </form>
            <a className="mt-4 block text-center text-sm text-accent hover:text-accent/80 transition" href="/register">Create account</a>
          </div>
        </div>
      </div>
    </main>
  );
}
