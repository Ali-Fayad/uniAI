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
          className="block text-sm font-medium leading-6 text-[#151514]"
          htmlFor={id}
        >
          {label}
        </label>
        <div className="mt-2">
          <div
            className={`flex rounded-md shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-custom-primary ${
              disabled ? "bg-gray-100" : "bg-white/80"
            }`}
          >
            <span className="flex select-none items-center pl-3 text-gray-500 sm:text-sm">
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
              className="block flex-1 border-0 bg-transparent py-2.5 pl-1 text-[#151514] placeholder:text-gray-400 focus:ring-0 sm:text-sm sm:leading-6 disabled:cursor-not-allowed disabled:text-gray-500"
            />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <label
        className="block text-sm font-medium leading-6 text-[#151514]"
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
          className={`block w-full rounded-md border-0 py-2.5 text-[#151514] shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-custom-primary sm:text-sm sm:leading-6 ${
            disabled
              ? "bg-gray-100 cursor-not-allowed text-gray-500"
              : "bg-white/80"
          }`}
        />
      </div>
    </div>
  );
};

export default FormInput;
