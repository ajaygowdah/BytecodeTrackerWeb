"use client";

import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api";
import { DashboardStatsDTO } from "@/lib/types";
import { RiskCard } from "@/components/ui/RiskCard";
import { RiskPieChart } from "@/components/charts/RiskPieChart";
import { ScanTimelineChart } from "@/components/charts/ScanTimelineChart";

function LoadingSkeleton() {
  return (
    <>
      <div className="grid gap-4 md:grid-cols-4">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="card h-32 animate-pulse bg-slate-900/60 p-5" />
        ))}
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <div className="card h-72 animate-pulse bg-slate-900/60" />
        <div className="card h-72 animate-pulse bg-slate-900/60" />
      </div>
    </>
  );
}

export default function DashboardPage() {
  const { data } = useQuery({
    queryKey: ["dashboard-stats"],
    queryFn: () => apiRequest<DashboardStatsDTO>("/api/dashboard/stats")
  });

  if (!data) return <section className="space-y-6"><h1 className="text-3xl font-heading">Security Dashboard</h1><LoadingSkeleton /></section>;

  return (
    <section className="space-y-6">
      <h1 className="text-3xl font-heading">Security Dashboard</h1>
      <div className="grid gap-4 md:grid-cols-4">
        <RiskCard title="Total Users" value={data.totalUsers} />
        <RiskCard title="Total Scans" value={data.totalScans} />
        <RiskCard
          title="High Risk Scans"
          value={data.highRiskScans}
          isDanger={data.highRiskScans > 0}
        />
        <RiskCard
          title="Most Recent Scan"
          value={data.mostRecentScan ? new Date(data.mostRecentScan).toLocaleString() : "None"}
        />
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        <div>
          <h2 className="mb-3 text-lg font-heading font-semibold text-slate-100">
            Risk Distribution
          </h2>
          <RiskPieChart data={data.riskBreakdown} />
        </div>
        <div>
          <h2 className="mb-3 text-lg font-heading font-semibold text-slate-100">
            Scan Activity (30 days)
          </h2>
          <ScanTimelineChart data={data.timeline} />
        </div>
      </div>
    </section>
  );
}
