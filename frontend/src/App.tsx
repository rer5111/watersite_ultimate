import '@/styles/index.css';
import { RouterProvider } from 'react-router';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@/lib';
import { router } from '@/routes';
import { AuthProvider } from '@/context';
import { donationTypesQueryOptions, eventsQueryOptions } from '@/api';

void queryClient.query(donationTypesQueryOptions).catch(() => {});
void queryClient.query(eventsQueryOptions).catch(() => {});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
