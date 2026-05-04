import React, { useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import authApi from '../api/authService';

const EmailVerification = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const verificationAttempted = useRef(false);

  const token = searchParams.get('token');

  useEffect(() => {
    if (!token || verificationAttempted.current) {
      return;
    }

    verificationAttempted.current = true;

    const verify = async () => {
      try {
        await authApi.verifyEmail(token);
        // Redirect to profile with success state
        navigate('/profile', { 
          replace: true, 
          state: { verificationSuccess: true } 
        });
      } catch (err) {
        // Check if email is already verified (409 CONFLICT)
        if (err.response?.status === 409) {
          navigate('/profile', { 
            replace: true, 
            state: { verificationAlreadyDone: true } 
          });
        } else {
          // For other errors, redirect to profile with error
          navigate('/profile', { 
            replace: true, 
            state: { 
              verificationError: true,
              errorMessage: err.response?.data?.message || 'Ошибка подтверждения email'
            } 
          });
        }
      }
    };

    verify();
  }, [token, navigate]);

  return null;
};

export default EmailVerification;