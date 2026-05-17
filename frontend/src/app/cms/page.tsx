"use client";

import { useEffect } from "react";
import { isAdmin } from "@/lib/auth";

export default function CmsHomePage() {
  useEffect(() => {
    if (!isAdmin()) {
      window.location.href = "/dashboard";
    }
  }, []);

  return (
    <section className="space-y-4">
      <h1 className="text-3xl font-heading">CMS Control Panel</h1>
      <p className="text-slate-300">Manage users, scans, and reporting from this admin workspace.</p>
      <div className="grid gap-4 md:grid-cols-3">
        <a className="card p-4 hover:border-accent" href="/cms/users">Users</a>
        <a className="card p-4 hover:border-accent" href="/cms/scans">Scans</a>
        <a className="card p-4 hover:border-accent" href="/cms/reports">Reports</a>
      </div>
    </section>
  );
}
