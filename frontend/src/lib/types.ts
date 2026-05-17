export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export interface LoginResponse {
  token: string;
  role: "USER" | "ADMIN";
}

export interface MethodAnalysisDTO {
  methodName: string;
  status: string;
  riskLevel: RiskLevel;
  riskReason: string;
}

export interface ClassAnalysisDTO {
  className: string;
  methods: MethodAnalysisDTO[];
}

export interface ViolationDTO {
  methodName: string;
  riskLevel: RiskLevel;
  reason: string;
}

export interface ScanResultDTO {
  scanId: number;
  username: string;
  fileName: string;
  fileSize: number;
  totalMethods: number;
  dangerousCount: number;
  safeCount: number;
  riskLevel: RiskLevel;
  createdAt: string;
  classes: ClassAnalysisDTO[];
  violations: ViolationDTO[];
}

export interface RiskSliceDTO {
  label: string;
  value: number;
}

export interface TimelinePointDTO {
  day: string;
  scans: number;
}

export interface DashboardStatsDTO {
  totalUsers: number;
  totalScans: number;
  highRiskScans: number;
  mostRecentScan: string;
  riskBreakdown: RiskSliceDTO[];
  timeline: TimelinePointDTO[];
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
