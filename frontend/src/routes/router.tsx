import { createBrowserRouter } from 'react-router';
import {
  AdminsPage,
  DonationsPage,
  HomePage,
  LoginPage,
  NotFoundPage,
  RegisterPage,
} from '@/pages';
import { paths } from './paths';
import { AuthLayout, RootLayout } from '@/components';

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      {
        path: paths.home,
        element: <HomePage />,
      },
      {
        path: paths.admins,
        element: <AdminsPage />,
      },
      {
        path: paths.donate,
        element: <DonationsPage />,
      },
    ],
  },
  {
    element: <AuthLayout />,
    children: [
      {
        path: paths.login,
        element: <LoginPage />,
      },
      {
        path: paths.register,
        element: <RegisterPage />,
      },
    ],
  },
  {
    path: paths.notFound,
    element: <NotFoundPage />,
  },
]);
