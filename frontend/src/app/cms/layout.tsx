import { PageShell } from "@/components/layout/PageShell";
import { AuthGate } from "@/components/layout/AuthGate";

export default function CmsLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthGate adminOnly>
      <PageShell>{children}</PageShell>
    </AuthGate>
  );
}
