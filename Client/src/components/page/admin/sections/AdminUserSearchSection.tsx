interface AdminUserSearchSectionProps {
  id: string;
  labelledBy: string;
}

const AdminUserSearchSection = ({ id, labelledBy }: AdminUserSearchSectionProps) => {
  return (
    <section
      id={id}
      role="tabpanel"
      aria-labelledby={labelledBy}
      className="rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-8 sm:px-6"
    >
      <p className="text-base font-semibold text-[var(--color-textPrimary)]">User Search</p>
      <p className="mt-2 text-sm leading-6 text-[var(--color-textSecondary)]">
        Search users by email will appear here.
      </p>
    </section>
  );
};

export default AdminUserSearchSection;
