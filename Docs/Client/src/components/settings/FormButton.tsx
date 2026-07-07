/**
 * FormButton
 *
 * Responsible for rendering a styled button with a small set of variants.
 *
 * Does NOT contain navigation logic and does NOT perform API calls.
 */

import React from "react";
import type { ButtonHTMLAttributes } from "react";

type FormButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "danger" | "ghost";
};

const variantClass: Record<NonNullable<FormButtonProps["variant"]>, string> = {
  primary:
    "inline-flex items-center justify-center rounded-full bg-[var(--color-primary)] px-5 py-2.5 text-sm font-semibold text-[var(--color-background)] shadow-sm hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed",
  secondary:
    "inline-flex items-center justify-center rounded-full bg-[var(--color-surface)] px-5 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] hover:bg-[var(--color-elevatedSurface)] disabled:opacity-50 disabled:cursor-not-allowed",
  danger:
    "inline-flex items-center justify-center rounded-full bg-[var(--color-error)] px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-red-500 disabled:opacity-50 disabled:cursor-not-allowed",
  ghost:
    "inline-flex items-center justify-center rounded-full bg-transparent px-5 py-2.5 text-sm font-semibold text-[var(--color-textPrimary)] shadow-sm disabled:opacity-50 disabled:cursor-not-allowed",
};

const FormButton: React.FC<FormButtonProps> = ({
  children,
  variant = "primary",
  className = "",
  ...props
}) => {
  return (
    <button
      className={`${variantClass[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default FormButton;
