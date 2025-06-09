import React, { createContext, useState, useContext, useEffect, useCallback, useMemo } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [currentPatient, setCurrentPatient] = useState(null);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoadingAuth, setIsLoadingAuth] = useState(true);

    useEffect(() => {
        console.log('[AuthContext] useEffect running - checking localStorage');
        const storedPatient = localStorage.getItem('currentPatient');
        console.log('[AuthContext] Attempting to load from localStorage. Found:', storedPatient);
        if (storedPatient) {
            try {
                const patientData = JSON.parse(storedPatient);
                console.log('[AuthContext] Parsed patientData from localStorage:', patientData);
                setCurrentPatient(patientData);
                setIsLoggedIn(true);
                console.log('[AuthContext] Set isLoggedIn to true from localStorage');
            } catch (error) {
                console.error("[AuthContext] Failed to parse patient data from localStorage", error);
                localStorage.removeItem('currentPatient');
                setCurrentPatient(null);
                setIsLoggedIn(false);
                console.log('[AuthContext] Set isLoggedIn to false due to parse error');
            }
        } else {
            console.log('[AuthContext] No stored patient found, keeping isLoggedIn false');
        }
        setIsLoadingAuth(false);
        console.log('[AuthContext] useEffect completed, set isLoadingAuth to false');
    }, []);

    const login = useCallback((patientData) => {
        console.log('[AuthContext] login function called with patientData:', patientData);
        console.log('[AuthContext] Token in patientData:', patientData.token);
        
        // Atomic state updates
        setCurrentPatient(patientData);
        setIsLoggedIn(true);
        localStorage.setItem('currentPatient', JSON.stringify(patientData));
        
        console.log('[AuthContext] login completed, isLoggedIn set to true, token should be:', patientData.token);
    }, []);

    const logout = useCallback(() => {
        console.log('[AuthContext] logout function called');
        
        // Atomic state updates
        setCurrentPatient(null);
        setIsLoggedIn(false);
        localStorage.removeItem('currentPatient');
        
        console.log('[AuthContext] logout completed, isLoggedIn set to false');
    }, []);

    const contextValue = useMemo(() => ({
        currentPatient,
        isLoggedIn,
        login,
        logout,
        isLoadingAuth,
        token: currentPatient?.token
    }), [currentPatient, isLoggedIn, login, logout, isLoadingAuth]);

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext); 