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
    "rounded-full bg-custom-primary px-5 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-[#a69d8f] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-custom-primary",
  secondary:
    "rounded-full bg-white px-5 py-2.5 text-sm font-semibold text-[#151514] shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50",
  danger:
    "rounded-lg bg-red-600 px-5 py-3 text-sm font-semibold text-white shadow-sm hover:bg-red-500",
  ghost:
    "rounded-full bg-transparent px-5 py-2.5 text-sm font-semibold text-[#151514] shadow-sm",
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
