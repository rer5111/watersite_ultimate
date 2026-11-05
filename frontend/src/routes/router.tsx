import { createBrowserRouter } from 'react-router';
import {
  AdminsPage,
  DonationsPage,
  HomePage,
  LoginPage,
  MicrosoftCallbackPage,
  NotFoundPage,
  ProfilePage,
  RegisterPage,
  VerifyEmailPage,
} from '@/pages';
import { paths } from './paths';
import { AuthLayout, RootLayout } from '@/components';
import { queryClient } from '@/lib';
import { donationTypesQueryOptions, eventsQueryOptions } from '@/api';

export const router = createBrowserRouter([
  {
    element: <RootLayout />,
    children: [
      {
        path: paths.home,
        element: <HomePage />,
        loader: () => queryClient.query(eventsQueryOptions),
      },
      {
        path: paths.admins,
        element: <AdminsPage />,
      },
      {
        path: paths.donate,
        element: <DonationsPage />,
        loader: () => queryClient.query(donationTypesQueryOptions),
      },
      {
        path: paths.profile,
        element: <ProfilePage />,
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
      {
        path: paths.verifyEmail,
        element: <VerifyEmailPage />,
      },
      {
        path: paths.microsoftCallback,
        element: <MicrosoftCallbackPage />,
      },
    ],
  },
  {
    path: paths.notFound,
    element: <NotFoundPage />,
  },
]);
