import { ReactNode } from "react";

interface DataTableProps {
  headers: string[];
  rows: ReactNode[][];
}

export function DataTable({ headers, rows }: DataTableProps) {
  return (
    <div className="overflow-x-auto rounded-2xl border border-slate-700 bg-slate-900/60">
      <table className="min-w-full text-sm">
        <thead className="sticky top-0 z-10 bg-slate-800 text-left text-slate-300">
          <tr>
            {headers.map((header) => (
              <th key={header} className="px-4 py-3 font-semibold">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr
              key={index}
              className={`border-t border-slate-800 transition-colors hover:bg-slate-800/60 ${
                index % 2 === 0 ? "bg-slate-900/40" : ""
              }`}
            >
              {row.map((cell, cellIndex) => (
                <td key={cellIndex} className="px-4 py-3 text-slate-100">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
