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

export type AdminSeasonStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED';

export interface AdminSeason {
  id: number;
  code: string;
  title: string;
  description: string;
  startsAt: string;
  organizer: string;
  status: AdminSeasonStatus;
}

export interface CreateAdminSeasonPayload {
  code: string;
  title: string;
  description: string;
  startDate?: string;
  endDate?: string;
  image?: string;
  displayOrder?: number;
  startsAt: string;
  organizer: string;
  status: AdminSeasonStatus;
}

export interface UpdateAdminSeasonPayload {
  title: string;
  description: string;
  startsAt: string;
  organizer: string;
  status: AdminSeasonStatus;
}

export type AdminEventStatus = 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface AdminEvent {
  id: number;
  code: string;
  title: string;
  description?: string;
  shortDescription?: string;
  image?: string;
  seasonNumber: number;
  seasonTitle?: string;
  organizer: string;
  status: AdminEventStatus;
  startDate?: string | null;
  endDate?: string | null;
  dateRangeFormatted?: string;
  participantsCount?: number;
  displayOrder?: number;
  active?: boolean;
}

export interface CreateAdminEventPayload {
  code: string;
  title: string;
  description: string;
  shortDescription?: string;
  image?: string;
  seasonNumber: number;
  organizer: string;
  status: AdminEventStatus;
  startDate?: string | null;
  endDate?: string | null;
  displayOrder?: number;
  active?: boolean;
}

export interface UpdateAdminEventPayload {
  title?: string;
  description?: string;
  shortDescription?: string;
  image?: string;
  seasonNumber?: number;
  organizer?: string;
  status?: AdminEventStatus;
  startDate?: string | null;
  endDate?: string | null;
  displayOrder?: number;
  active?: boolean;
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
