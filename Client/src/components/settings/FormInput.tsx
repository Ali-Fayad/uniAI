import React from "react";

type Props = {
  id: string;
  label: string;
  name?: string;
  type?: string;
  value?: string;
  placeholder?: string;
  prefix?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  disabled?: boolean;
};

const FormInput: React.FC<Props> = ({
  id,
  label,
  name,
  type = "text",
  value,
  placeholder,
  prefix,
  onChange,
  disabled,
}) => {
  if (prefix) {
    return (
      <div>
        <label
          className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]"
          htmlFor={id}
        >
          {label}
        </label>
        <div className="mt-2">
          <div
            className={`flex rounded-md shadow-sm ring-1 ring-inset ring-[var(--color-border)] focus-within:ring-2 focus-within:ring-inset focus-within:ring-[var(--color-primary)] ${
              disabled ? "bg-[var(--color-background)]" : "bg-[var(--color-background)]"
            }`}
          >
            <span className="flex select-none items-center pl-3 text-[var(--color-textSecondary)] sm:text-sm">
              {prefix}
            </span>
            <input
              id={id}
              name={name || id}
              placeholder={placeholder}
              type={type}
              value={value}
              onChange={onChange}
              disabled={disabled}
              className="block flex-1 border-0 bg-transparent py-2.5 pl-1 text-[var(--color-textPrimary)] placeholder:text-[var(--color-textSecondary)] focus:ring-0 sm:text-sm sm:leading-6 disabled:cursor-not-allowed disabled:text-[var(--color-textSecondary)]"
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <label
        className="block text-sm font-medium leading-6 text-[var(--color-textPrimary)]"
        htmlFor={id}
      >
        {label}
      </label>
      <div className="mt-2">
        <input
          id={id}
          name={name || id}
          type={type}
          value={value}
          onChange={onChange}
          disabled={disabled}
          placeholder={placeholder}
          className={`block w-full rounded-md border-0 py-2.5 text-[var(--color-textPrimary)] shadow-sm ring-1 ring-inset ring-[var(--color-border)] placeholder:text-[var(--color-textSecondary)] focus:ring-2 focus:ring-inset focus:ring-[var(--color-primary)] sm:text-sm sm:leading-6 ${
            disabled
              ? "bg-[var(--color-background)] cursor-not-allowed text-[var(--color-textSecondary)]"
              : "bg-[var(--color-background)]"
          }`}
        />
      </div>
    </div>
  );
};

export default FormInput;
