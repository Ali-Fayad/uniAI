import { useState, useEffect } from 'react';
import { cvService } from '../services/cv';

export const useProfileCompleteCheck = () => {
  const [isChecking, setIsChecking] = useState(true);
  const [isFilled, setIsFilled] = useState(false);

  useEffect(() => {
    cvService.getPersonalInfoStatus()
      .then(res => {
        setIsFilled(res.isFilled);
        setIsChecking(false);
      })
      .catch(() => {
        setIsFilled(false);
        setIsChecking(false);
      });
  }, []);

  return { isChecking, isFilled };
};
