import { useEffect, useRef, useState } from 'react';

const usePaymentNotificationPolling = (onPaymentStatusUpdate) => {
  const [isPolling, setIsPolling] = useState(false);
  const [error, setError] = useState(null);
  const intervalRef = useRef(null);

  useEffect(() => {
    const pollForNotifications = async () => {
      try {
        const token = localStorage.getItem('authToken');
        if (!token) return;

        const response = await fetch('/api/patients/me/payment-notifications', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const notification = await response.json();
          console.log('Payment notification received:', notification);
          onPaymentStatusUpdate(notification);
        } else if (response.status !== 204) {
          console.error('Error fetching payment notifications:', response.status);
        }
        setError(null);
      } catch (err) {
        console.error('Error polling for payment notifications:', err);
        setError('Connection error');
      }
    };

    const startPolling = () => {
      setIsPolling(true);
      // Poll every 3 seconds for notifications
      intervalRef.current = setInterval(pollForNotifications, 3000);
    };

    const stopPolling = () => {
      setIsPolling(false);
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
        intervalRef.current = null;
      }
    };

    startPolling();

    return () => {
      stopPolling();
    };
  }, [onPaymentStatusUpdate]);

  return { isPolling, error };
};

export default usePaymentNotificationPolling; 