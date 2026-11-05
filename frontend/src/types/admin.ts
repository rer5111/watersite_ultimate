export type AdminUserRole = 'USER' | 'MODERATOR' | 'ADMIN';
export type AdminUserStatus = 'ACTIVE' | 'BANNED' | 'DEACTIVATED';

export interface AdminUser {
  id: number;
  username: string;
  displayName: string;
  email: string;
  role: AdminUserRole;
  status: AdminUserStatus;
  registeredAt: string;
}

export interface UpdateAdminUserPayload {
  role: AdminUserRole;
  status: AdminUserStatus;
}

export type AdminEventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED';

export interface AdminEvent {
  id: number;
  code: string;
  title: string;
  description: string;
  startsAt: string;
  organizer: string;
  status: AdminEventStatus;
}

export interface UpdateAdminEventPayload {
  title: string;
  description: string;
  startsAt: string;
  organizer: string;
  status: AdminEventStatus;
}

export type AdminRoleRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type AdminRequestedRole = 'MODERATOR' | 'ADMIN';

export interface AdminRoleRequest {
  id: number;
  applicantId: number;
  applicantName: string;
  applicantEmail: string;
  requestedRole: AdminRequestedRole;
  submittedAt: string;
  note: string;
  status: AdminRoleRequestStatus;
  reviewedAt: string | null;
  rejectionReason: string | null;
}

export type BroadcastAudience = 'ALL' | 'ORGANIZERS' | 'MODERATORS' | 'ADMINS';
export type BroadcastCategory = 'INFO' | 'WARNING' | 'SUCCESS';
export type BroadcastStatus = 'SENT' | 'SCHEDULED' | 'FAILED';

export interface BroadcastPayload {
  subject: string;
  category: BroadcastCategory;
  audience: BroadcastAudience;
  message: string;
}

export interface BroadcastDispatch {
  id: number;
  subject: string;
  category: BroadcastCategory;
  audience: BroadcastAudience;
  message: string;
  sentAt: string;
  recipients: number;
  status: BroadcastStatus;
}
