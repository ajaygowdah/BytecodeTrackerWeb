"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { isAdmin } from "../../lib/auth";

const navSections = [
  {
    section: "Workspace",
    items: [
      ["/dashboard", "Dashboard"],
      ["/scan/upload", "Upload"],
      ["/scan/history", "Scan History"]
    ]
  },
  {
    section: "Admin",
    items: [
      ["/cms/users", "Users"],
      ["/cms/reports", "Reports"]
    ]
  }
];

export function Sidebar() {
  const pathname = usePathname();

  const isActive = (href: string) => {
    if (href === "/dashboard") return pathname === "/dashboard";
    if (href === "/scan/upload") return pathname === "/scan/upload";
    if (href === "/scan/history") return pathname === "/scan/history";
    return pathname.startsWith(href) && href !== "/cms";
  };

  return (
    <aside className="h-screen w-64 border-r border-slate-800 bg-slate-950 overflow-y-auto p-4 flex flex-col">
      {/* Logo and Title */}
      <div className="mb-8 flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/20 text-accent">
          <svg
            className="h-6 w-6"
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
        <h2 className="text-lg font-heading font-bold text-accent">BytecodeTracker</h2>
      </div>

      {/* Navigation Sections */}
      <nav className="flex-1 space-y-6">
        {navSections.map((section) => {
          // Hide Admin section entirely for non-admin users
          if (section.section === "Admin" && !isAdmin()) return null;
          return (
            <div key={section.section}>
              <p className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
                {section.section}
              </p>
              <div className="space-y-1">
                {section.items.map(([href, label]) => (
                  <Link
                    key={href}
                    href={href}
                    className={`flex items-center rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                      isActive(href)
                        ? "border-l-2 border-accent bg-slate-800 text-accent"
                        : "border-l-2 border-transparent text-slate-300 hover:bg-slate-800/60 hover:text-slate-100"
                    }`}
                  >
                    {label}
                  </Link>
                ))}
              </div>
            </div>
          );
        })}
      </nav>
    </aside>
  );
}
