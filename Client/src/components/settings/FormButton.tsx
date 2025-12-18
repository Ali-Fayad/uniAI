import React from "react";

type Props = {
  children: React.ReactNode;
  variant?: "primary" | "secondary" | "danger" | "ghost";
  type?: "button" | "submit" | "reset";
  onClick?: () => void;
  className?: string;
};

const variantClass: Record<string, string> = {
  primary:
    "rounded-full bg-[var(--color-primary)] px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-[var(--color-primaryVariant)] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[var(--color-focusRing)]",
  secondary:
    "rounded-full bg-[var(--color-surface)] px-5 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] hover:bg-[var(--color-elevatedSurface)]",
  danger:
    "rounded-lg bg-[var(--color-error)] px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-red-500",
  ghost:
    "rounded-full bg-transparent px-5 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] shadow-sm",
};

const FormButton: React.FC<Props> = ({
  children,
  variant = "primary",
  type = "button",
  onClick,
  className = "",
}) => {
  return (
    <button
      type={type}
      onClick={onClick}
      className={`${variantClass[variant]} ${className}`}
    >
      {children}
    </button>
  );
};

export default FormButton;
