import { useEffect, useRef, useState } from 'react';

const usePaymentStatusWebSocket = (onPaymentStatusUpdate) => {
  const ws = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // WebSocket connection URL - adjust based on your API Gateway setup
    const wsUrl = 'ws://localhost:4004/ws/payment-notifications';
    
    const connect = () => {
      try {
        console.log('Attempting to connect to WebSocket:', wsUrl);
        ws.current = new WebSocket(wsUrl);
        
        ws.current.onopen = () => {
          console.log('WebSocket connected for payment notifications');
          setIsConnected(true);
          setError(null);
        };
        
        ws.current.onmessage = (event) => {
          try {
            const notification = JSON.parse(event.data);
            console.log('Received payment notification:', notification);
            
            if (onPaymentStatusUpdate && typeof onPaymentStatusUpdate === 'function') {
              onPaymentStatusUpdate(notification);
            }
          } catch (parseError) {
            console.error('Error parsing payment notification:', parseError);
          }
        };
        
        ws.current.onclose = (event) => {
          console.log('WebSocket disconnected:', event.code, event.reason);
          setIsConnected(false);
          
          // Attempt to reconnect after 3 seconds if not manually closed
          if (event.code !== 1000) {
            setTimeout(() => {
              if (ws.current?.readyState === WebSocket.CLOSED) {
                connect();
              }
            }, 3000);
          }
        };
        
        ws.current.onerror = (error) => {
          console.error('WebSocket error:', error);
          setError('WebSocket connection failed');
          setIsConnected(false);
        };
        
      } catch (err) {
        console.error('Error creating WebSocket connection:', err);
        setError('Failed to create WebSocket connection');
      }
    };

    connect();

    // Cleanup function
    return () => {
      if (ws.current) {
        console.log('Closing WebSocket connection');
        ws.current.close(1000, 'Component unmounting');
      }
    };
  }, [onPaymentStatusUpdate]);

  const sendMessage = (message) => {
    if (ws.current && ws.current.readyState === WebSocket.OPEN) {
      ws.current.send(JSON.stringify(message));
    } else {
      console.warn('WebSocket is not connected. Cannot send message:', message);
    }
  };

  return {
    isConnected,
    error,
    sendMessage
  };
};

export default usePaymentStatusWebSocket; 