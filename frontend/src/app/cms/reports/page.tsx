"use client";

import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api";
import { DashboardStatsDTO } from "@/lib/types";
import jsPDF from "jspdf";
import { Button } from "@/components/ui/Button";

export default function CmsReportsPage() {
  const { data } = useQuery({
    queryKey: ["dashboard-stats-reports"],
    queryFn: () => apiRequest<DashboardStatsDTO>("/api/dashboard/stats")
  });

  async function exportCsv() {
    const csv = await apiRequest<string>("/api/admin/reports/csv");
    const blob = new Blob([csv], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "scans-report.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  function exportPdf() {
    const doc = new jsPDF();
    doc.setFontSize(14);
    doc.text("BytecodeTracker Scan Summary", 14, 20);
    doc.setFontSize(11);
    doc.text(`Total Scans: ${data?.totalScans ?? 0}`, 14, 34);
    doc.text(`High Risk Scans: ${data?.highRiskScans ?? 0}`, 14, 42);
    doc.text(`Total Users: ${data?.totalUsers ?? 0}`, 14, 50);
    doc.save("scan-summary.pdf");
  }

  return (
    <section className="space-y-4">
      <h1 className="text-3xl font-heading">Reports</h1>
      <div className="grid gap-4 md:grid-cols-3">
        <article className="card p-4"><p className="text-sm text-slate-400">Total scans</p><p className="mt-2 text-2xl">{data?.totalScans ?? 0}</p></article>
        <article className="card p-4"><p className="text-sm text-slate-400">Dangerous scans</p><p className="mt-2 text-2xl">{data?.highRiskScans ?? 0}</p></article>
        <article className="card p-4"><p className="text-sm text-slate-400">Top users indicator</p><p className="mt-2 text-2xl">{data?.totalUsers ?? 0} users</p></article>
      </div>
      <div className="flex gap-2">
        <Button onClick={exportCsv}>Export CSV</Button>
        <Button onClick={exportPdf}>Export PDF</Button>
      </div>
    </section>
  );
}
