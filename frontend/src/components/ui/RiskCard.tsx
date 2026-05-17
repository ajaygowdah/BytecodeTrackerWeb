import { ReactNode } from "react";

interface RiskCardProps {
  title: string;
  value: string | number;
  trend?: string;
  icon?: ReactNode;
  isDanger?: boolean;
}

export function RiskCard({ title, value, trend, icon, isDanger }: RiskCardProps) {
  return (
    <article className="card relative p-5">
      {icon && (
        <div className="absolute right-4 top-4 text-accent/60">
          {icon}
        </div>
      )}
      <p className="text-xs uppercase tracking-wider text-slate-400">{title}</p>
      <p className={`mt-3 text-3xl font-heading font-semibold ${isDanger ? 'text-danger' : 'text-white'}`}>
        {value}
      </p>
      {trend && (
        <p className="mt-2 text-xs text-slate-400">{trend}</p>
      )}
    </article>
  );
}
