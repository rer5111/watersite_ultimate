import { useCallback, useEffect, useState } from 'react';

function getStoredCooldownUntil(storageKey: string): number | null {
  if (typeof window === 'undefined') return null;
  try {
    const stored = localStorage.getItem(storageKey);
    if (!stored) return null;
    const until = parseInt(stored, 10);
    if (Number.isNaN(until) || until <= Date.now()) {
      localStorage.removeItem(storageKey);
      return null;
    }
    return until;
  } catch {
    return null;
  }
}

export function useCooldown(storageKey: string) {
  const [cooldownUntil, setCooldownUntil] = useState<number | null>(() =>
    getStoredCooldownUntil(storageKey),
  );

  const [remaining, setRemaining] = useState<number>(() => {
    const until = getStoredCooldownUntil(storageKey);
    return until ? Math.max(0, Math.ceil((until - Date.now()) / 1000)) : 0;
  });

  useEffect(() => {
    if (!cooldownUntil) return;

    const timer = setInterval(() => {
      const left = Math.max(0, Math.ceil((cooldownUntil - Date.now()) / 1000));
      setRemaining(left);
      if (left <= 0) {
        setCooldownUntil(null);
        try {
          localStorage.removeItem(storageKey);
        } catch {
          // ignore
        }
      }
    }, 1000);

    return () => clearInterval(timer);
  }, [cooldownUntil, storageKey]);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === storageKey) {
        const updated = getStoredCooldownUntil(storageKey);
        setCooldownUntil(updated);
        setRemaining(updated ? Math.max(0, Math.ceil((updated - Date.now()) / 1000)) : 0);
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [storageKey]);

  const startCooldown = useCallback(
    (seconds: number) => {
      const until = Date.now() + seconds * 1000;
      try {
        localStorage.setItem(storageKey, String(until));
      } catch {
        // ignore
      }
      setCooldownUntil(until);
      setRemaining(seconds);
    },
    [storageKey],
  );

  const resetCooldown = useCallback(() => {
    try {
      localStorage.removeItem(storageKey);
    } catch {
      // ignore
    }
    setCooldownUntil(null);
    setRemaining(0);
  }, [storageKey]);

  const countdown = cooldownUntil ? remaining : 0;

  return {
    countdown,
    startCooldown,
    resetCooldown,
    isCooldownActive: countdown > 0,
  };
}
