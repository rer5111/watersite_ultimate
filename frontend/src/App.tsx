import '@/styles/index.css';
import { RouterProvider } from 'react-router';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@/lib';
import { router } from '@/routes';
import { AuthProvider } from '@/context';
import { ToastProvider } from '@/components/ui';
import { donationTypesQueryOptions, seasonsQueryOptions } from '@/api';

void queryClient.query(donationTypesQueryOptions).catch(() => {});
void queryClient.query(seasonsQueryOptions).catch(() => {});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <RouterProvider router={router} />
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
