import React from "react";

type Props = {
  title: string;
  icon?: string;
  children: React.ReactNode;
  className?: string;
};

const SettingsSection: React.FC<Props> = ({
  title,
  icon,
  children,
  className = "",
}) => {
  return (
    <section
      className={`bg-white/60 backdrop-blur-sm rounded-lg border border-white/50 shadow-sm overflow-hidden ${className}`}
    >
      <div className="px-6 py-6 sm:px-10 sm:py-8">
        <h3 className="text-xl font-bold text-[#151514] mb-6 flex items-center gap-2">
          {icon && <span className="material-symbols-outlined">{icon}</span>}
          {title}
        </h3>

        {children}
      </div>
    </section>
  );
};

export default SettingsSection;
