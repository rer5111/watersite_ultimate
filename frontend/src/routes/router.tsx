import { createBrowserRouter, Navigate } from 'react-router';
import {
  AdminBroadcastPage,
  AdminEventsPage,
  AdminRequestsPage,
  AdminUsersPage,
  AdminsPage,
  ChatPage,
  DonationsPage,
  HomePage,
  LoginPage,
  MicrosoftCallbackPage,
  NotificationsPage,
  NotFoundPage,
  ProfilePage,
  RegisterPage,
  VerifyEmailPage,
} from '@/pages';
import { paths } from './paths';
import { AdminRoute } from './admin-route';
import { AdminLayout, AuthLayout, RootLayout } from '@/components';
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
        path: paths.chat,
        element: <ChatPage />,
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
      {
        path: paths.notifications,
        element: <NotificationsPage />,
      },
    ],
  },
  {
    element: <AdminRoute />,
    children: [
      {
        path: paths.admin,
        element: <AdminLayout />,
        children: [
          {
            index: true,
            element: <Navigate to={paths.adminUsers} replace />,
          },
          {
            path: 'users',
            element: <AdminUsersPage />,
          },
          {
            path: 'events',
            element: <AdminEventsPage />,
          },
          {
            path: 'broadcast',
            element: <AdminBroadcastPage />,
          },
          {
            path: 'requests',
            element: <AdminRequestsPage />,
          },
        ],
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
