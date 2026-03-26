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
import type { PersonalInfoCertificateEntryDto } from '../../../../types/dto';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import { moveItem } from '../personalInfoUtils';

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

  editingCertificateId: string | null;
  setEditingCertificateId: React.Dispatch<React.SetStateAction<string | null>>;
  editingCertificateName: string;
  setEditingCertificateName: React.Dispatch<React.SetStateAction<string>>;
  editingCertificateIssuer: string;
  setEditingCertificateIssuer: React.Dispatch<React.SetStateAction<string>>;
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
  editingCertificateId,
  setEditingCertificateId,
  editingCertificateName,
  setEditingCertificateName,
  editingCertificateIssuer,
  setEditingCertificateIssuer,
}) => {
  return (
    <PersonalInfoSectionCard
      title="Certificates"
      className="rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 sm:p-6 space-y-4"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <input
          value={certificateName}
          onChange={(e) => setCertificateName(e.target.value)}
          placeholder="Certificate name"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={certificateIssuer}
          onChange={(e) => setCertificateIssuer(e.target.value)}
          placeholder="Issuer"
          className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <input
          value={certificateCredentialUrl}
          onChange={(e) => setCertificateCredentialUrl(e.target.value)}
          placeholder="Credential URL"
          className="sm:col-span-1 w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
        />
        <button
          type="button"
          onClick={addCertificate}
          className="rounded-md bg-[var(--color-primary)] px-4 py-2 text-[var(--color-background)] font-medium"
        >
          Add Certificate
        </button>
      </div>

      <div className="space-y-2">
        {certificates.map((item, index) => (
          <div key={item.id} className="rounded-md border border-[var(--color-border)] p-3 space-y-2">
            {editingCertificateId === item.id ? (
              <>
                <input
                  value={editingCertificateName}
                  onChange={(e) => setEditingCertificateName(e.target.value)}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
                <input
                  value={editingCertificateIssuer}
                  onChange={(e) => setEditingCertificateIssuer(e.target.value)}
                  className="w-full rounded-md border border-[var(--color-border)] bg-[var(--color-background)] px-3 py-2 text-[var(--color-textPrimary)]"
                />
              </>
            ) : (
              <p className="text-[var(--color-textPrimary)]">
                {item.name}{item.issuer ? ` · ${item.issuer}` : ''}
              </p>
            )}
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => index > 0 && setCertificates((prev) => moveItem(prev, index, index - 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↑
              </button>
              <button
                type="button"
                onClick={() => index < certificates.length - 1 && setCertificates((prev) => moveItem(prev, index, index + 1))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                ↓
              </button>
              {editingCertificateId === item.id ? (
                <button
                  type="button"
                  onClick={() => {
                    const nextName = editingCertificateName.trim();
                    if (!nextName) {
                      return;
                    }
                    setCertificates((prev) =>
                      prev.map((entry) =>
                        entry.id === item.id
                          ? {
                              ...entry,
                              name: nextName,
                              issuer: editingCertificateIssuer.trim(),
                            }
                          : entry,
                      ),
                    );
                    setEditingCertificateId(null);
                    setEditingCertificateName('');
                    setEditingCertificateIssuer('');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Save
                </button>
              ) : (
                <button
                  type="button"
                  onClick={() => {
                    setEditingCertificateId(item.id);
                    setEditingCertificateName(item.name);
                    setEditingCertificateIssuer(item.issuer ?? '');
                  }}
                  className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
                >
                  Edit
                </button>
              )}
              <button
                type="button"
                onClick={() => setCertificates((prev) => prev.filter((entry) => entry.id !== item.id))}
                className="rounded-md border border-[var(--color-border)] px-2 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </PersonalInfoSectionCard>
  );
};

export default CertificatesSection;
