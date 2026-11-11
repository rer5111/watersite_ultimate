import { useEffect, useRef, useState } from 'react';

type DelayedLoadingOptions = {
  delay?: number;
  minDuration?: number;
};

function useDelayedLoading(
  loading: boolean,
  { delay = 300, minDuration = 400 }: DelayedLoadingOptions = {}
) {
  const [visible, setVisible] = useState(false);
  const shownAt = useRef(0);

  useEffect(() => {
    if (loading === visible) {
      return;
    }
    if (loading) {
      const timer = setTimeout(
        () => {
          shownAt.current = Date.now();
          setVisible(true);
        },
        Math.max(0, delay)
      );
      return () => clearTimeout(timer);
    }
    const remaining = Math.max(0, minDuration - (Date.now() - shownAt.current));
    const timer = setTimeout(() => setVisible(false), remaining);
    return () => clearTimeout(timer);
  }, [loading, visible, delay, minDuration]);

  return visible;
}

export { useDelayedLoading };
export type { DelayedLoadingOptions };
