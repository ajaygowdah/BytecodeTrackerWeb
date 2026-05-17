"use client";

import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";

function CustomTooltip({ active, payload }: any) {
  if (active && payload && payload.length) {
    const { day, scans } = payload[0].payload;
    return (
      <div className="rounded border border-slate-600 bg-slate-950 p-2 shadow-lg">
        <p className="text-sm text-slate-300">{day}</p>
        <p className="text-sm font-semibold text-cyan-400">scans: {scans}</p>
      </div>
    );
  }
  return null;
}

export function ScanTimelineChart({ data }: { data: { day: string; scans: number }[] }) {
  return (
    <div className="card h-72 p-4">
      <ResponsiveContainer>
        <LineChart data={data}>
          <XAxis dataKey="day" hide />
          <YAxis />
          <Tooltip content={<CustomTooltip />} />
          <Line dataKey="scans" stroke="#1ecbe1" strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
