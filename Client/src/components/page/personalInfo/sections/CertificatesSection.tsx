/**
 * CertificatesSection
 *
 * Responsibility:
 * - Render certificate inputs and the certificate list editor.
 *
 * Does NOT:
 * - Persist personal info
 * - Own controller state (provided via props)
 */

import React from 'react';
import { FaCertificate } from 'react-icons/fa';
import type { PersonalInfoCertificateEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';
import FormButton from '../../../settings/FormButton';

export interface CertificatesSectionProps {
  certificateName: string;
  setCertificateName: React.Dispatch<React.SetStateAction<string>>;
  certificateIssuer: string;
  setCertificateIssuer: React.Dispatch<React.SetStateAction<string>>;
  certificateCredentialUrl: string;
  setCertificateCredentialUrl: React.Dispatch<React.SetStateAction<string>>;
  addCertificate: () => void;

  certificates: PersonalInfoCertificateEntryDto[];
  setCertificates: React.Dispatch<React.SetStateAction<PersonalInfoCertificateEntryDto[]>>;

}

const CertificatesSection: React.FC<CertificatesSectionProps> = ({
  certificateName,
  setCertificateName,
  certificateIssuer,
  setCertificateIssuer,
  certificateCredentialUrl,
  setCertificateCredentialUrl,
  addCertificate,
  certificates,
  setCertificates,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Certificates"
      icon={<FaCertificate className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-start">
        <AnimatedInput
          value={certificateName}
          onChange={(e) => setCertificateName(e.target.value)}
          label="Certificate name"
        />
        <AnimatedInput
          value={certificateIssuer}
          onChange={(e) => setCertificateIssuer(e.target.value)}
          label="Issuer"
        />
        <AnimatedInput
          value={certificateCredentialUrl}
          onChange={(e) => setCertificateCredentialUrl(e.target.value)}
          label="Credential URL"
          containerClassName="sm:col-span-1"
        />
        <FormButton
          variant="primary"
          type="button"
          onClick={addCertificate}
          className="h-14 px-4 shrink-0 sm:col-span-2 md:col-span-1"
        >
          Add Certificate
        </FormButton>
      </div>

      <div className="flex flex-wrap gap-2 mt-4">
        {certificates.map((item) => (
          <span
            key={item.id}
            className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
          >
            {item.name}{item.issuer ? ` · ${item.issuer}` : ''}
            <button
              type="button"
              onClick={() => setCertificates((prev) => prev.filter((entry) => entry.id !== item.id))}
              className="text-[var(--color-textSecondary)] hover:text-[var(--color-textPrimary)]"
            >
              ×
            </button>
          </span>
        ))}
      </div>
    </PersonalInfoSectionCard>
  );
};

export default CertificatesSection;
