"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api";
import { DataTable } from "@/components/ui/DataTable";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { RiskLevel } from "@/lib/types";

interface ScanRow {
  scanId: number;
  fileName: string;
  riskLevel: RiskLevel;
  totalMethods: number;
  dangerousCount: number;
  safeCount: number;
  createdAt: string;
}

interface ScansResponse {
  content: ScanRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export default function ScanHistoryPage() {
  const [page, setPage] = useState(0);

  const { data, refetch } = useQuery({
    queryKey: ["user-scans", page],
    queryFn: () => apiRequest<ScansResponse>(`/api/scans?page=${page}&size=10`)
  });

  async function deleteScan(id: number) {
    if (!window.confirm("Delete this scan?")) return;
    await apiRequest<{ message: string }>(`/api/scans/${id}`, { method: "DELETE" });
    refetch();
  }

  const rows = (data?.content ?? []).map((s) => [
    s.scanId,
    s.fileName,
    <Badge key={`risk-${s.scanId}`} level={s.riskLevel} />,
    s.totalMethods,
    s.dangerousCount,
    s.safeCount,
    new Date(s.createdAt).toLocaleString(),
    <div key={`actions-${s.scanId}`} className="flex gap-2">
      <Button onClick={() => (window.location.href = `/scan/${s.scanId}`)}>View</Button>
      <Button onClick={() => deleteScan(s.scanId)} className="bg-danger text-white">Delete</Button>
    </div>
  ]);

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-heading">
          Scan History
          <span className="ml-3 inline-block rounded-full bg-slate-800 px-3 py-1 text-sm font-medium text-slate-300">
            {data?.totalElements ?? 0}
          </span>
        </h1>
      </div>

      <DataTable headers={["ID", "Filename", "Risk", "Methods", "Dangerous", "Safe", "Date", "Actions"]} rows={rows} />

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-slate-700 pt-4">
          <div className="text-sm text-slate-400">
            Page {(data.page ?? 0) + 1} of {data.totalPages} • {data.totalElements} total
          </div>
          <div className="flex gap-2">
            <Button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0}
              className="disabled:opacity-50"
            >
              Previous
            </Button>
            <Button
              onClick={() => setPage((p) => p + 1)}
              disabled={page >= (data.totalPages ?? 1) - 1}
              className="disabled:opacity-50"
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </section>
  );
}
