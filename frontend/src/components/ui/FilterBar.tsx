"use client";

import { ReactNode } from "react";

interface FilterBarProps {
  searchValue: string;
  onSearchChange: (value: string) => void;
  searchPlaceholder?: string;
  selectValue: string;
  onSelectChange: (value: string) => void;
  selectOptions: { value: string; label: string }[];
  selectLabel?: string;
  children?: ReactNode;
}

export function FilterBar({
  searchValue,
  onSearchChange,
  searchPlaceholder = "Search...",
  selectValue,
  onSelectChange,
  selectOptions,
  selectLabel,
  children
}: FilterBarProps) {
  return (
    <div className="flex flex-wrap gap-2">
      <div className="relative flex-1 min-w-56">
        <svg
          className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <input
          type="text"
          className="w-full rounded-lg bg-slate-900 py-2 pl-10 pr-3 text-sm text-slate-100 placeholder-slate-400 transition focus:outline-none focus:ring-2 focus:ring-accent"
          placeholder={searchPlaceholder}
          value={searchValue}
          onChange={(e) => onSearchChange(e.target.value)}
        />
      </div>
      <select
        className="rounded-lg bg-slate-900 px-3 py-2 text-sm text-slate-100 transition focus:outline-none focus:ring-2 focus:ring-accent"
        value={selectValue}
        onChange={(e) => onSelectChange(e.target.value)}
      >
        {selectOptions.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {children}
    </div>
  );
}
