package com.uniai.cvbuilder.domain.builder;

import com.uniai.cvbuilder.domain.model.Certificate;

import java.time.LocalDate;

public final class CertificateBuilder {

    private final Certificate.CertificateBuilder builder;

    private CertificateBuilder(Long cvId, String name) {
        this.builder = Certificate.builder()
                .cvId(cvId)
                .name(name);
    }

    public static CertificateBuilder newCertificate(Long cvId, String name) {
        return new CertificateBuilder(cvId, name);
    }

    public CertificateBuilder issuer(String issuer) {
        builder.issuer(issuer);
        return this;
    }

    public CertificateBuilder date(LocalDate date) {
        builder.date(date);
        return this;
    }

    public CertificateBuilder credentialUrl(String credentialUrl) {
        builder.credentialUrl(credentialUrl);
        return this;
    }

    public Certificate build() {
        return builder.build();
    }
}
