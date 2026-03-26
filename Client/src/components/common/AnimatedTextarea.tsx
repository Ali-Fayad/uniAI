import React, { TextareaHTMLAttributes } from 'react';

interface AnimatedTextareaProps extends Omit<TextareaHTMLAttributes<HTMLTextAreaElement>, 'placeholder'> {
  label: string;
  error?: string;
  containerClassName?: string;
}

const AnimatedTextarea: React.FC<AnimatedTextareaProps> = ({
  label,
  error,
  containerClassName = '',
  id,
  className = '',
  rows = 3,
  ...props
}) => {
  // Generate a unique ID if none provided, for the label htmlFor
  const inputId = id || `animated-textarea-${label.replace(/\s+/g, '-').toLowerCase()}-${Math.random().toString(36).substr(2, 9)}`;

  return (
    <div className={`relative w-full ${containerClassName}`}>
      <textarea
        id={inputId}
        rows={rows}
        className={`peer w-full rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-[15px] py-3 pt-6 text-[var(--color-textPrimary)] 
        focus:ring-2 focus:ring-[var(--color-primary)] focus:border-transparent outline-none 
        placeholder-transparent trantision-all transition-colors duration-200 resize-none
        disabled:opacity-50 disabled:cursor-not-allowed
        ${className}`}
        placeholder=" "
        {...props}
      />
      <label
        htmlFor={inputId}
        className="absolute left-3 top-4 text-gray-500 dark:text-gray-400 
        transition-all duration-200 pointer-events-none px-1 bg-transparent
        peer-focus:top-2 peer-focus:text-xs peer-focus:text-gray-400
        peer-[:not(:placeholder-shown)]:top-2 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:text-gray-400"
      >
        {label}
      </label>
      {error && (
        <span className="text-sm text-red-500 mt-1 block px-1">{error}</span>
      )}
    </div>
  );
};

export default AnimatedTextarea;
