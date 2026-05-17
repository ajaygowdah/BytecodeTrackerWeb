"use client";

import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from "recharts";

const COLORS: Record<string, string> = {
  HIGH: "#ff5d5d",
  MEDIUM: "#f1c74a",
  LOW: "#4ecb71"
};

function CustomTooltip({ active, payload }: any) {
  if (active && payload && payload.length) {
    const { label, value } = payload[0].payload;
    return (
      <div className="rounded border border-slate-600 bg-slate-950 p-2 shadow-lg">
        <p className="text-sm font-semibold text-slate-200">{label}</p>
        <p className="text-sm text-cyan-400">{value}</p>
      </div>
    );
  }
  return null;
}

export function RiskPieChart({ data }: { data: { label: string; value: number }[] }) {
  return (
    <div className="card h-72 p-4">
      <ResponsiveContainer>
        <PieChart>
          <Pie data={data} dataKey="value" nameKey="label" outerRadius={90}>
            {data.map((entry) => (
              <Cell key={entry.label} fill={COLORS[entry.label] ?? "#1ecbe1"} />
            ))}
          </Pie>
          <Tooltip content={<CustomTooltip />} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
