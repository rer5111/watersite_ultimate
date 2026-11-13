import { AdminPageHeader } from './components';
import { BroadcastComposerSection, BroadcastHistorySection } from './sections';

export function AdminBroadcastPage() {
  return (
    <div className="flex flex-col gap-6">
      <AdminPageHeader
        title="Рассылки"
        description="Отправка уведомлений всем игрокам или отдельным ролям. Проверьте текст перед отправкой."
      />
      <BroadcastComposerSection />
      <BroadcastHistorySection />
    </div>
  );
}
