import { useEffect, useRef } from 'react';
import { Outlet, useLocation } from 'react-router';
import { Header } from './header/header';
import { Footer } from './footer';

function ScrollToTopOnNavigate() {
  const { pathname } = useLocation();
  const prevPathRef = useRef(pathname);

  useEffect(() => {
    if (prevPathRef.current !== pathname) {
      window.scrollTo(0, 0);
      prevPathRef.current = pathname;
    }
  }, [pathname]);

  return null;
}

export function RootLayout() {
  return (
    <>
      <Header />
      <main className="min-h-dvh">
        <ScrollToTopOnNavigate />
        <Outlet />
      </main>
      <Footer />
    </>
  );
}
