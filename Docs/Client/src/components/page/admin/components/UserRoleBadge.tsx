import type { UserRole } from '../../../../types/dto';

interface UserRoleBadgeProps {
  role: UserRole;
}

const UserRoleBadge = ({ role }: UserRoleBadgeProps) => {
  const isAdmin = role === 'ADMIN';

  return (
    <span
      className={[
        'inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold tracking-wide',
        isAdmin
          ? 'border-[var(--color-primary)]/30 bg-[var(--color-primary)]/15 text-[var(--color-primary)]'
          : 'border-[var(--color-border)] bg-[var(--color-elevatedSurface)] text-[var(--color-textSecondary)]',
      ].join(' ')}
    >
      {role}
    </span>
  );
};

export default UserRoleBadge;
