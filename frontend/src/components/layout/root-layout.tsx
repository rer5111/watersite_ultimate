import { Outlet, ScrollRestoration } from 'react-router';
import { Header } from './header/header';
import { Footer } from './footer';

export function RootLayout() {
  return (
    <>
      <Header />
      <main className="min-h-dvh">
        <Outlet />
        <ScrollRestoration />
      </main>
      <Footer />
    </>
  );
}
