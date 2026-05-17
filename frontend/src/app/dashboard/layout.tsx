import { PageShell } from "@/components/layout/PageShell";
import { AuthGate } from "@/components/layout/AuthGate";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGate>
      <PageShell>{children}</PageShell>
    </AuthGate>
  );
}
