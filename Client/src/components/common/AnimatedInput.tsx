import React from 'react';

interface AnimatedInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'placeholder'> {
  label: string;
  error?: string;
  containerClassName?: string;
  children?: React.ReactNode;
  /** Optional node rendered inside the input at the start (e.g. country code +) */
  startAdornment?: React.ReactNode;
}

const AnimatedInput: React.FC<AnimatedInputProps> = ({
  label,
  error,
  containerClassName = '',
  id,
  className = '',
  children,
  startAdornment,
  ...props
}) => {
  // Generate a unique ID if none provided, for the label htmlFor
  const inputId = id || `animated-input-${label.replace(/\s+/g, '-').toLowerCase()}-${Math.random().toString(36).substr(2, 9)}`;

  return (
    <div className={`w-full ${containerClassName}`}>
      <div className={`flex items-center ${startAdornment ? 'gap-3' : ''}`}>
        {startAdornment && (
          <span className="select-none text-[var(--color-textPrimary)] mr-1">
            {startAdornment}
          </span>
        )}

        <div className="relative flex-1">
          <input
            id={inputId}
            className={`peer w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] h-14 px-[15px] text-[var(--color-textPrimary)] 
            focus:ring-2 focus:ring-[var(--color-primary)] focus:border-transparent outline-none 
            placeholder-transparent trantision-all transition-colors duration-200
            disabled:opacity-50 disabled:cursor-not-allowed
            ${className}`}
            placeholder=" "
            {...props}
          />

          <label
            htmlFor={inputId}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500 dark:text-gray-400 
            transition-all duration-200 pointer-events-none px-1 bg-[var(--color-surface)]
            peer-focus:-top-2.5 peer-focus:left-3 peer-focus:text-xs peer-focus:text-gray-400
            peer-[:not(:placeholder-shown)]:-top-2.5 peer-[:not(:placeholder-shown)]:left-3 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:text-gray-400"
          >
            {label}
          </label>
        </div>
      </div>

      {error && (
        <span className="text-sm text-red-500 mt-1 block px-1">{error}</span>
      )}
      {children}
    </div>
  );
};

export default AnimatedInput;
