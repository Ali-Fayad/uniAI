import type { AdminUserPersonalInfoResponse } from '../../../../types/dto';
import LoadingSpinner from '../../../common/LoadingSpinner';
import PersonalInfoSectionCard from '../../personalInfo/PersonalInfoSectionCard';
import { formatEducationLabel } from '../../personalInfo/personalInfoStateHelpers';
import { getSafeExternalUrl } from '../utils/safeAdminLinks';

interface SelectedUserPersonalInfoTabProps {
  personalInfo: AdminUserPersonalInfoResponse | null;
  isLoading: boolean;
  error: string | null;
}

const hasText = (value?: string | null) => !!value && value.trim().length > 0;

const fieldValue = (value?: string | null) => (hasText(value) ? value : '—');

const joinList = (values: Array<string | undefined | null>) =>
  values.filter((value): value is string => hasText(value)).join(' · ');

const renderExternalLink = (value?: string | null) => {
  const safeUrl = getSafeExternalUrl(value);

  if (!safeUrl.display) {
    return <p className="mt-1 text-sm text-[var(--color-textPrimary)]">—</p>;
  }

  if (safeUrl.href) {
    return (
      <a
        href={safeUrl.href}
        target="_blank"
        rel="noopener noreferrer"
        className="mt-1 break-all text-sm text-[var(--color-primary)] hover:underline"
      >
        {safeUrl.display}
      </a>
    );
  }

  return <p className="mt-1 break-all text-sm text-[var(--color-textPrimary)]">{safeUrl.display}</p>;
};

const SelectedUserPersonalInfoTab = ({
  personalInfo,
  isLoading,
  error,
}: SelectedUserPersonalInfoTabProps) => {
  if (isLoading) {
    return (
      <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-10">
        <LoadingSpinner text="Loading personal information..." />
      </div>
    );
  }

  if (error) {
    return (
      <div
        role="alert"
        className="rounded-2xl border border-[var(--color-error)]/40 bg-[var(--color-error)]/10 px-4 py-4 text-sm text-[var(--color-textPrimary)]"
      >
        {error}
      </div>
    );
  }

  if (
    !personalInfo ||
    (!personalInfo.hasPersonalInfo &&
      !hasText(personalInfo.phone) &&
      !hasText(personalInfo.address) &&
      !hasText(personalInfo.linkedin) &&
      !hasText(personalInfo.github) &&
      !hasText(personalInfo.portfolio) &&
      !hasText(personalInfo.summary) &&
      !hasText(personalInfo.jobTitle) &&
      !hasText(personalInfo.company) &&
      (personalInfo.education ?? []).length === 0 &&
      (personalInfo.skills ?? []).length === 0 &&
      (personalInfo.languages ?? []).length === 0 &&
      (personalInfo.experience ?? []).length === 0 &&
      (personalInfo.projects ?? []).length === 0 &&
      (personalInfo.certificates ?? []).length === 0)
  ) {
    return (
      <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-8 text-sm text-[var(--color-textSecondary)]">
        No personal information available.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <PersonalInfoSectionCard
        title="Basic Information"
        className="space-y-4 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
      >
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Phone</p>
            <p className="mt-1 text-sm text-[var(--color-textPrimary)]">{fieldValue(personalInfo.phone)}</p>
          </div>
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Address</p>
            <p className="mt-1 text-sm text-[var(--color-textPrimary)]">{fieldValue(personalInfo.address)}</p>
          </div>
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Job Title</p>
            <p className="mt-1 text-sm text-[var(--color-textPrimary)]">{fieldValue(personalInfo.jobTitle)}</p>
          </div>
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Company</p>
            <p className="mt-1 text-sm text-[var(--color-textPrimary)]">{fieldValue(personalInfo.company)}</p>
          </div>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">LinkedIn</p>
            {renderExternalLink(personalInfo.linkedin)}
          </div>
          <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">GitHub</p>
            {renderExternalLink(personalInfo.github)}
          </div>
        </div>

        <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Portfolio</p>
          {renderExternalLink(personalInfo.portfolio)}
        </div>

        <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">Summary</p>
          <p className="mt-2 text-sm leading-6 text-[var(--color-textPrimary)]">
            {fieldValue(personalInfo.summary)}
          </p>
        </div>
      </PersonalInfoSectionCard>

      {(personalInfo.education ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Education"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          {(personalInfo.education ?? []).map((item) => (
            <div key={item.id} className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
              <p className="font-semibold text-[var(--color-textPrimary)]">{formatEducationLabel(item)}</p>
              <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
                {joinList([item.degree, item.fieldOfStudy])}
              </p>
              <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
                {joinList([item.startDate, item.endDate])}
              </p>
              {hasText(item.grade) && (
                <p className="mt-1 text-sm text-[var(--color-textSecondary)]">Grade: {item.grade}</p>
              )}
              {hasText(item.description) && (
                <p className="mt-2 text-sm leading-6 text-[var(--color-textPrimary)]">{item.description}</p>
              )}
            </div>
          ))}
        </PersonalInfoSectionCard>
      )}

      {(personalInfo.skills ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Skills"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          <div className="flex flex-wrap gap-2">
            {(personalInfo.skills ?? []).map((item) => (
              <span
                key={item.id}
                className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                {item.name}
                {hasText(item.level) && <span className="text-[var(--color-textSecondary)]">({item.level})</span>}
              </span>
            ))}
          </div>
        </PersonalInfoSectionCard>
      )}

      {(personalInfo.languages ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Languages"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          <div className="flex flex-wrap gap-2">
            {(personalInfo.languages ?? []).map((item) => (
              <span
                key={item.id}
                className="inline-flex items-center gap-2 rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-3 py-1 text-sm text-[var(--color-textPrimary)]"
              >
                {item.name}
                {hasText(item.proficiency) && (
                  <span className="text-[var(--color-textSecondary)]">({item.proficiency})</span>
                )}
              </span>
            ))}
          </div>
        </PersonalInfoSectionCard>
      )}

      {(personalInfo.experience ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Experience"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          <div className="space-y-3">
            {(personalInfo.experience ?? []).map((item) => (
              <div key={item.id} className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
                <p className="font-semibold text-[var(--color-textPrimary)]">
                  {item.position}
                  {item.company ? ` · ${item.company}` : ''}
                </p>
                <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
                  {joinList([item.location, item.startDate, item.endDate])}
                  {item.currentlyWorking ? ' · Currently working' : ''}
                </p>
                {hasText(item.description) && (
                  <p className="mt-2 text-sm leading-6 text-[var(--color-textPrimary)]">{item.description}</p>
                )}
              </div>
            ))}
          </div>
        </PersonalInfoSectionCard>
      )}

      {(personalInfo.projects ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Projects"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          <div className="space-y-3">
            {(personalInfo.projects ?? []).map((item) => (
              <div key={item.id} className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
                <p className="font-semibold text-[var(--color-textPrimary)]">{item.name}</p>
                {hasText(item.description) && (
                  <p className="mt-2 text-sm leading-6 text-[var(--color-textPrimary)]">{item.description}</p>
                )}
                {item.technologies && item.technologies.length > 0 && (
                  <p className="mt-2 text-sm text-[var(--color-textSecondary)]">
                    {item.technologies.join(' · ')}
                  </p>
                )}
                <div className="mt-2 flex flex-wrap gap-3 text-sm">
                  {getSafeExternalUrl(item.repositoryUrl).href ? (
                    <a
                      href={getSafeExternalUrl(item.repositoryUrl).href ?? '#'}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-[var(--color-primary)] hover:underline"
                    >
                      Repository
                    </a>
                  ) : hasText(item.repositoryUrl) ? (
                    <span className="break-all text-[var(--color-textPrimary)]">{item.repositoryUrl}</span>
                  ) : null}
                  {getSafeExternalUrl(item.liveUrl).href ? (
                    <a
                      href={getSafeExternalUrl(item.liveUrl).href ?? '#'}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-[var(--color-primary)] hover:underline"
                    >
                      Live
                    </a>
                  ) : hasText(item.liveUrl) ? (
                    <span className="break-all text-[var(--color-textPrimary)]">{item.liveUrl}</span>
                  ) : null}
                </div>
              </div>
            ))}
          </div>
        </PersonalInfoSectionCard>
      )}

      {(personalInfo.certificates ?? []).length > 0 && (
        <PersonalInfoSectionCard
          title="Certificates"
          className="space-y-3 rounded-3xl border border-[var(--color-border)] bg-[var(--color-background)] p-5 shadow-sm sm:p-6"
        >
          <div className="space-y-3">
            {(personalInfo.certificates ?? []).map((item) => (
              <div key={item.id} className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-3">
                <p className="font-semibold text-[var(--color-textPrimary)]">{item.name}</p>
                <p className="mt-1 text-sm text-[var(--color-textSecondary)]">
                  {joinList([item.issuer, item.date])}
                </p>
                {getSafeExternalUrl(item.credentialUrl).href ? (
                  <a
                    href={getSafeExternalUrl(item.credentialUrl).href ?? '#'}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-2 inline-block text-sm text-[var(--color-primary)] hover:underline"
                  >
                    Credential
                  </a>
                ) : hasText(item.credentialUrl) ? (
                  <span className="mt-2 block break-all text-sm text-[var(--color-textPrimary)]">
                    {item.credentialUrl}
                  </span>
                ) : null}
              </div>
            ))}
          </div>
        </PersonalInfoSectionCard>
      )}
    </div>
  );
};

export default SelectedUserPersonalInfoTab;
