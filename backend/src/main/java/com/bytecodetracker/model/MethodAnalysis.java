package com.bytecodetracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "methods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassAnalysis classAnalysis;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    @Builder.Default
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "risk_reason", columnDefinition = "TEXT")
    private String riskReason;
}
