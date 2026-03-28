/**
 * BasicInformationSection
 *
 * Responsibility:
 * - Render the basic contact/job fields of the Personal Info form.
 *
 * Does NOT:
 * - Fetch or save personal info
 * - Own navigation logic
 */

import React, { useState, useEffect } from 'react';
import { FaUser, FaPhone, FaMapMarkerAlt, FaBriefcase, FaBuilding } from 'react-icons/fa';
import type { BasicFormState } from '../personalInfoTypes';
import PersonalInfoSectionCard from '../PersonalInfoSectionCard';
import AnimatedInput from '../../../common/AnimatedInput';
import { usePositions } from '../../../../hooks/usePositions';
import { isValidPhoneNumber } from '../../../../lib/utils';

export interface BasicInformationSectionProps {
  form: BasicFormState;
  setField: (field: keyof BasicFormState, value: string) => void;
}

const BasicInformationSection: React.FC<BasicInformationSectionProps> = ({ form, setField }) => {
  const [isWorking, setIsWorking] = useState(!!form.jobTitle || !!form.company);
  const { items: positions } = usePositions(form.jobTitle);
  const [phoneError, setPhoneError] = useState<string | null>(null);

  // Local state for split phone inputs to ensure smooth typing
  // We initialize from the prop, but manage updates locally before syncing up
  const [phoneCode, setPhoneCode] = useState(() => {
    const parts = form.phone ? form.phone.split(' ') : [];
    return parts.length > 0 ? parts[0] : '';
  });
  
  const [phoneNumber, setPhoneNumber] = useState(() => {
    const parts = form.phone ? form.phone.split(' ') : [];
    return parts.length > 1 ? parts.slice(1).join(' ') : '';
  });

  // Sync from prop to local state (e.g. on initial load or reset)
  useEffect(() => {
    const parts = form.phone ? form.phone.split(' ') : [];
    const derivedCode = parts.length > 0 ? parts[0] : '';
    const derivedNumber = parts.length > 1 ? parts.slice(1).join(' ') : '';
    
    // Only update if completely different to avoid cursor jumps
    if (`${phoneCode} ${phoneNumber}` !== form.phone) {
       // Only if the prop is truthy and valid-ish, otherwise we keep local
       if (form.phone && form.phone.trim() !== '') {
          setPhoneCode(derivedCode);
          setPhoneNumber(derivedNumber);
       }
    }
  }, [form.phone]);

  // Sync isWorking with form data (handles async data loading)
  useEffect(() => {
    if (form.jobTitle || form.company) {
      setIsWorking(true);
    }
  }, [form.jobTitle, form.company]);

  // Clear job fields when isWorking is unchecked
  useEffect(() => {
    if (!isWorking) {
      if (form.jobTitle) setField('jobTitle', '');
      if (form.company) setField('company', '');
    }
  }, [isWorking]);

  const updatePhone = (code: string, num: string) => {
    const combined = `${code} ${num}`.trim();
    setField('phone', combined);
    
    // Validate combined format
    if (combined && !isValidPhoneNumber(combined)) {
      setPhoneError("Format: +{Code} {Number}");
    } else {
      setPhoneError(null);
    }
  };

  const handleCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setPhoneCode(val);
    updatePhone(val, phoneNumber);
  };

  const handleNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setPhoneNumber(val);
    updatePhone(phoneCode, val);
  };

  // UI Refinement: Removed external text headers ("Contact", "Location", etc.) 
  // and replaced them with icons to the left of inputs for a cleaner, unified layout.
  return (
    <PersonalInfoSectionCard
      title="Basic Information"
      icon={<FaUser className="h-5 w-5" aria-hidden="true" />}
      className="bg-[var(--color-surface)] rounded-3xl border border-[var(--color-border)] shadow-sm overflow-hidden p-5 sm:p-6 space-y-6"
    >
      <div className="grid grid-cols-1 gap-6">

        {/* Phone Split: Area Code + Number */}
        <div className="flex items-start gap-3">
          <FaPhone className="mt-4 text-[var(--color-textPrimary)] text-lg shrink-0" aria-hidden="true" />
          <div className="flex-1 w-full">
            <div className="flex gap-2">
              <div className="min-w-[80px] w-1/4 max-w-[120px]">
                 <AnimatedInput
                    value={phoneCode}
                    onChange={handleCodeChange}
                    label="Code"
                    
                  />
              </div>
              <div className="flex-1">
                  <AnimatedInput
                    value={phoneNumber}
                    onChange={handleNumberChange}
                    label="Number"
                    
                  />
              </div>
            </div>
            {phoneError && (
              <p className="mt-1 text-xs text-red-500 px-1">{phoneError}</p>
            )}
          </div>
        </div>

        {/* Address Row with Icon */}
        <div className="flex items-start gap-3">
           <FaMapMarkerAlt className="mt-4 text-[var(--color-textPrimary)] text-lg shrink-0" aria-hidden="true" />
           <div className="flex-1">
              <AnimatedInput
                value={form.address}
                onChange={(e) => setField('address', e.target.value)}
                label="Full Address"
                containerClassName="w-full"
              />
           </div>
        </div>

        {/* Working Checkbox */}
        <div className="flex items-center gap-3 py-2 pl-8">
          <input
            id="is-working-checkbox"
            type="checkbox"
            checked={isWorking}
            onChange={(e) => setIsWorking(e.target.checked)}
            className="w-4 h-4 text-[var(--color-primary)] rounded border-gray-300 focus:ring-[var(--color-primary)]"
          />
          <label htmlFor="is-working-checkbox" className="text-sm text-[var(--color-textPrimary)] font-medium cursor-pointer">
            Are you currently working?
          </label>
        </div>

        {/* Job Fields */}
        {isWorking && (
          <>
            <div className="flex items-start gap-3">
               <FaBriefcase className="mt-4 text-[var(--color-textPrimary)] text-lg shrink-0" aria-hidden="true" />
               <div className="flex-1">
                  <AnimatedInput
                    value={form.jobTitle}
                    onChange={(e) => setField('jobTitle', e.target.value)}
                    label="Job Title"
                    list="positions-list"
                    autoComplete="off"
                  />
                  <datalist id="positions-list">
                    {positions.map((pos) => (
                      <option key={pos.id} value={pos.name} />
                    ))}
                  </datalist>
               </div>
            </div>

            <div className="flex items-start gap-3">
               <FaBuilding className="mt-4 text-[var(--color-textPrimary)] text-lg shrink-0" aria-hidden="true" />
               <div className="flex-1">
                  <AnimatedInput
                    value={form.company}
                    onChange={(e) => setField('company', e.target.value)}
                    label="Company Name"
                  />
               </div>
            </div>
          </>
        )}
      </div>
    </PersonalInfoSectionCard>
  );
};

export default BasicInformationSection;
