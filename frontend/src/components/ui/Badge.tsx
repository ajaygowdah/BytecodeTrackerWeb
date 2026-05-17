import { RiskLevel } from "@/lib/types";

const classes: Record<RiskLevel, string> = {
  HIGH: "bg-danger/20 text-danger border-danger/50",
  MEDIUM: "bg-warn/20 text-warn border-warn/50",
  LOW: "bg-ok/20 text-ok border-ok/50"
};

export function Badge({ level }: { level: RiskLevel }) {
  return <span className={`rounded-full border px-2 py-1 text-xs font-bold ${classes[level]}`}>{level}</span>;
}
