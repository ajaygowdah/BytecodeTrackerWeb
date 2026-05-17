"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api";
import { DataTable } from "@/components/ui/DataTable";
import { FilterBar } from "@/components/ui/FilterBar";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { RiskLevel } from "@/lib/types";

interface ScanRow {
  scanId: number;
  username: string;
  filename: string;
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

export default function CmsScansPage() {
  const [username, setUsername] = useState("");
  const [risk, setRisk] = useState("");
  const [selected, setSelected] = useState<number[]>([]);
  const [page, setPage] = useState(0);

  const { data, refetch } = useQuery({
    queryKey: ["cms-scans", username, risk, page],
    queryFn: () => apiRequest<ScansResponse>(`/api/admin/scans?username=${encodeURIComponent(username)}&riskLevel=${encodeURIComponent(risk)}&page=${page}&size=10`)
  });

  async function deleteScan(id: number) {
    await apiRequest<{ message: string }>(`/api/admin/scans/${id}`, { method: "DELETE" });
    refetch();
  }

  async function bulkDelete() {
    if (!selected.length) return;
    await apiRequest<{ message: string }>("/api/admin/scans", {
      method: "DELETE",
      body: JSON.stringify({ ids: selected })
    });
    setSelected([]);
    refetch();
  }

  const rows = (data?.content ?? []).map((s) => [
    <input key={`select-${s.scanId}`} type="checkbox" checked={selected.includes(s.scanId)} onChange={(e) => setSelected((prev) => e.target.checked ? [...prev, s.scanId] : prev.filter((x) => x !== s.scanId))} />,
    s.scanId,
    s.username,
    s.filename,
    <Badge key={`risk-${s.scanId}`} level={s.riskLevel} />,
    s.totalMethods,
    new Date(s.createdAt).toLocaleString(),
    <div key={`actions-${s.scanId}`} className="flex gap-2"><Button onClick={() => (window.location.href = `/scan/${s.scanId}`)}>View</Button><Button onClick={() => deleteScan(s.scanId)} className="bg-danger text-white">Delete</Button></div>
  ]);

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-heading">
          Scan Management
          <span className="ml-3 inline-block rounded-full bg-slate-800 px-3 py-1 text-sm font-medium text-slate-300">
            {data?.content?.length ?? 0}
          </span>
        </h1>
      </div>
      <FilterBar
        searchValue={username}
        onSearchChange={setUsername}
        searchPlaceholder="Filter by user"
        selectValue={risk}
        onSelectChange={setRisk}
        selectOptions={[
          { value: "", label: "All risk levels" },
          { value: "HIGH", label: "HIGH" },
          { value: "MEDIUM", label: "MEDIUM" },
          { value: "LOW", label: "LOW" }
        ]}
      >
        <Button onClick={bulkDelete} className="bg-danger text-white" disabled={!selected.length}>Bulk Delete</Button>
      </FilterBar>
      <DataTable headers={["", "ID", "User", "Filename", "Risk", "Methods", "Date", "Actions"]} rows={rows} />
      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between border-t border-slate-700 pt-4">
          <div className="text-sm text-slate-400">
            Page {(data.page ?? 0) + 1} of {data.totalPages} • {data.totalElements} total
          </div>
          <div className="flex gap-2">
            <Button
              onClick={() => setPage(Math.max(0, (page ?? 0) - 1))}
              disabled={page === 0}
              className="disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Previous
            </Button>
            <Button
              onClick={() => setPage((page ?? 0) + 1)}
              disabled={(page ?? 0) >= (data.totalPages ?? 1) - 1}
              className="disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </section>
  );
}
