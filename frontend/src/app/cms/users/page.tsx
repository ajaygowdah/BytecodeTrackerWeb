"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api";
import { DataTable } from "@/components/ui/DataTable";
import { FilterBar } from "@/components/ui/FilterBar";
import { Button } from "@/components/ui/Button";

interface UserRow {
  id: number;
  username: string;
  role: string;
  createdAt: string;
  scanCount: number;
}

interface UsersResponse {
  content: UserRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export default function CmsUsersPage() {
  const [search, setSearch] = useState("");
  const [role, setRole] = useState("");
  const [page, setPage] = useState(0);

  const { data, refetch } = useQuery({
    queryKey: ["cms-users", search, role, page],
    queryFn: () => apiRequest<UsersResponse>(`/api/admin/users?search=${encodeURIComponent(search)}&role=${encodeURIComponent(role)}&page=${page}&size=10`)
  });

  async function deleteUser(id: number) {
    if (!window.confirm("Delete this user and all scans?")) return;
    await apiRequest<{ message: string }>(`/api/admin/users/${id}`, { method: "DELETE" });
    refetch();
  }

  const rows = (data?.content ?? []).map((u) => [
    u.id,
    u.username,
    u.createdAt,
    u.scanCount,
    <Button key={`delete-${u.id}`} onClick={() => deleteUser(u.id)}>Delete</Button>
  ]);

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-heading">
          User Management
          <span className="ml-3 inline-block rounded-full bg-slate-800 px-3 py-1 text-sm font-medium text-slate-300">
            {data?.content?.length ?? 0}
          </span>
        </h1>
      </div>
      <FilterBar
        searchValue={search}
        onSearchChange={setSearch}
        searchPlaceholder="Search username"
        selectValue={role}
        onSelectChange={setRole}
        selectOptions={[
          { value: "", label: "All roles" },
          { value: "USER", label: "USER" },
          { value: "ADMIN", label: "ADMIN" }
        ]}
      />
      <DataTable headers={["ID", "Username", "Created", "Scans", "Actions"]} rows={rows} />
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
