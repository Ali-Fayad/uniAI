interface UserStatCardProps {
  label: string;
  value: string | number;
}

const UserStatCard = ({ label, value }: UserStatCardProps) => {
  return (
    <div className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-background)] px-4 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[var(--color-textSecondary)]">
        {label}
      </p>
      <p className="mt-2 text-2xl font-black tracking-tight text-[var(--color-textPrimary)]">
        {value}
      </p>
    </div>
  );
};

export default UserStatCard;
