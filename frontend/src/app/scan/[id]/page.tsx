"use client";

import { useQuery } from "@tanstack/react-query";
import { useParams } from "next/navigation";
import { apiRequest } from "@/lib/api";
import { ScanResultDTO } from "@/lib/types";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";

export default function ScanDetailPage() {
  // Use useParams() hook instead of the deprecated sync params prop
  // to be compatible with Next.js 14 and future-proof for Next.js 15.
  const params = useParams<{ id?: string | string[] }>();
  const idParam = params?.id;
  const id = Array.isArray(idParam) ? idParam[0] : (idParam ?? "");

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["scan", id],
    queryFn: () => apiRequest<ScanResultDTO>(`/api/scans/${id}`),
    enabled: !!id,
  });

  if (isLoading) {
    return <p className="text-slate-300">Loading scan report...</p>;
  }

  if (isError || !data) {
    const message = error instanceof Error ? error.message : "Scan not found or you do not have permission to view it.";
    return (
      <div className="card p-6 text-center">
        <p className="text-danger">{message}</p>
        <a className="mt-4 inline-block text-sm text-accent" href="/scan/upload">
          ← Back to upload
        </a>
      </div>
    );
  }

  const methods = data.classes.flatMap((c) => c.methods);

  return (
    <section className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-heading">Scan Report #{data.scanId}</h1>
        <Badge level={data.riskLevel} />
      </div>
      <div className="grid gap-4 md:grid-cols-3">
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wider text-slate-400">File</p>
          <p className="mt-1 text-slate-100 truncate">{data.fileName}</p>
        </div>
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wider text-slate-400">Total Methods</p>
          <p className="mt-1 text-2xl text-white">{data.totalMethods}</p>
        </div>
        <div className="card p-4">
          <p className="text-xs uppercase tracking-wider text-slate-400">Dangerous Methods</p>
          <p className="mt-1 text-2xl text-danger">{data.dangerousCount}</p>
        </div>
      </div>
      {methods.length > 0 ? (
        <DataTable
          headers={["Method", "Status", "Risk", "Reason"]}
          rows={methods.map((m) => [
            <span key={`${m.methodName}-name`} className="font-mono text-xs">{m.methodName}</span>,
            m.status,
            <Badge key={`${m.methodName}-badge`} level={m.riskLevel} />,
            <span key={`${m.methodName}-reason`} className="text-slate-400">{m.riskReason}</span>,
          ])}
        />
      ) : (
        <p className="text-slate-400">No method data available for this scan.</p>
      )}
      {data.violations.length > 0 && (
        <div className="card p-4">
          <h2 className="mb-3 text-lg font-heading">Violations ({data.violations.length})</h2>
          <ul className="space-y-2">
            {data.violations.map((v, i) => (
              <li key={i} className="flex items-start gap-3 text-sm">
                <Badge level={v.riskLevel} />
                <span className="font-mono text-slate-200">{v.methodName}</span>
                <span className="text-slate-400">{v.reason}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
