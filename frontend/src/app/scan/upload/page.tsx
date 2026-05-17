"use client";

import { useState } from "react";
import { FileDropzone } from "@/components/ui/FileDropzone";
import { apiRequest } from "@/lib/api";
import { ScanResultDTO } from "@/lib/types";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";

export default function UploadScanPage() {
  const [result, setResult] = useState<ScanResultDTO | null>(null);
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);

  async function isValidClassBytecode(file: File): Promise<boolean> {
    if (file.size < 4) return false;
    const header = new Uint8Array(await file.slice(0, 4).arrayBuffer());
    return header[0] === 0xca && header[1] === 0xfe && header[2] === 0xba && header[3] === 0xbe;
  }

  async function upload(file: File) {
    setError("");
    setResult(null);

    const isBytecode = await isValidClassBytecode(file);
    if (!isBytecode) {
      setError("Upload blocked: selected file is not valid Java bytecode (.class / CAFEBABE header missing).");
      return;
    }

    setUploading(true);
    const formData = new FormData();
    formData.append("file", file);

    try {
      const response = await apiRequest<ScanResultDTO>("/api/scans/upload", {
        method: "POST",
        body: formData
      });
      setResult(response);
    } catch (err) {
      const message = (err as Error).message || "Upload failed";
      if (/corrupt|invalid bytecode|bad request/i.test(message)) {
        setError("Upload failed: this file is not a valid compiled Java .class bytecode file.");
      } else if (/only \.class files are allowed/i.test(message)) {
        setError("Upload failed: only .class files are accepted.");
      } else {
        setError(message);
      }
    } finally {
      setUploading(false);
    }
  }

  return (
    <section className="space-y-6">
      <h1 className="text-3xl font-heading">Upload Class File</h1>

      {uploading && (
        <div className="flex flex-col items-center justify-center py-8">
          <div className="relative h-20 w-20">
            <div className="absolute inset-0 rounded-full border-4 border-slate-700/30" />
            <div className="absolute inset-0 animate-spin rounded-full border-4 border-accent border-t-accent" />
          </div>
          <p className="mt-4 text-sm text-slate-300">Analyzing...</p>
        </div>
      )}

      {!uploading && <FileDropzone onFile={upload} />}

      {error ? <p className="text-danger">{error}</p> : null}
      {result ? (
        <article className="card p-5">
          <div className="flex items-center justify-between">
            <h2 className="text-xl">{result.fileName}</h2>
            <Badge level={result.riskLevel} />
          </div>
          <p className="mt-3 text-slate-300">Methods: {result.totalMethods} | Dangerous: {result.dangerousCount}</p>
          <Button className="mt-4" onClick={() => (window.location.href = `/scan/${result.scanId}`)}>
            View full report
          </Button>
        </article>
      ) : null}
    </section>
  );
}
