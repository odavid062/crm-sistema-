export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface Contact {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  whatsapp?: string;
  status: "LEAD" | "PROSPECT" | "CUSTOMER" | "CHURNED" | "INACTIVE";
  source?: string;
  companyId?: string;
  companyName?: string;
  ownerId?: string;
  ownerName?: string;
  tags: Tag[];
  avatarUrl?: string;
  createdAt: string;
}

export interface Company {
  id: string;
  name: string;
  cnpj?: string;
  email?: string;
  phone?: string;
  website?: string;
  industry?: string;
  ownerId?: string;
  ownerName?: string;
  tags: Tag[];
  createdAt: string;
}

export interface Pipeline {
  id: string;
  name: string;
  description?: string;
  active: boolean;
  stages: PipelineStage[];
  createdAt: string;
}

export interface PipelineStage {
  id: string;
  name: string;
  color: string;
  position: number;
  winProbability: number;
}

export interface Deal {
  id: string;
  title: string;
  value: number;
  currency: string;
  stageId: string;
  stageName: string;
  stageColor: string;
  pipelineId: string;
  pipelineName: string;
  contactId?: string;
  contactName?: string;
  companyId?: string;
  companyName?: string;
  ownerId?: string;
  ownerName?: string;
  status: "OPEN" | "WON" | "LOST";
  priority: string;
  expectedCloseDate?: string;
  closedAt?: string;
  lostReason?: string;
  createdAt: string;
}

export interface Tag {
  id: string;
  name: string;
  color: string;
}

export interface Activity {
  id: string;
  title: string;
  type: "TASK" | "CALL" | "MEETING" | "EMAIL" | "WHATSAPP";
  status: "PENDING" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
  priority: string;
  dueDate?: string;
  completedAt?: string;
  contactId?: string;
  dealId?: string;
  assignedToId?: string;
  createdAt: string;
}

export interface WhatsAppConversation {
  id: string;
  remoteJid: string;
  contactName?: string;
  contactId?: string;
  assignedToId?: string;
  status: "OPEN" | "PENDING" | "RESOLVED" | "SPAM";
  lastMessageAt?: string;
  unreadCount: number;
  instanceName?: string;
}

export interface WhatsAppMessage {
  id: string;
  conversationId: string;
  content?: string;
  mediaUrl?: string;
  mediaType?: string;
  direction: "IN" | "OUT";
  status: string;
  timestamp: string;
}

export interface Payment {
  id: string;
  asaasId?: string;
  contactId?: string;
  description: string;
  value: number;
  billingType: string;
  status: string;
  dueDate: string;
  paymentDate?: string;
  invoiceUrl?: string;
  bankSlipUrl?: string;
  pixCode?: string;
  createdAt: string;
}

export interface DashboardData {
  contacts: { total: number; leads: number; customers: number };
  deals: { open: number; won: number; lost: number; wonRevenue: number; openRevenue: number };
  payments: { received: number; pending: number };
}

// ===== Agendamentos =====
export type AppointmentStatus = "SCHEDULED" | "CONFIRMED" | "IN_PROGRESS" | "DONE" | "CANCELLED" | "NO_SHOW";
export type AppointmentType = "MEETING" | "CALL" | "VISIT" | "DEMO" | "FOLLOWUP" | "OTHER";

export interface Appointment {
  id: string;
  title: string;
  description?: string;
  type: AppointmentType;
  status: AppointmentStatus;
  startAt: string;
  endAt?: string;
  allDay: boolean;
  location?: string;
  meetingUrl?: string;
  color: string;
  contactId?: string;
  contactName?: string;
  dealId?: string;
  dealTitle?: string;
  assignedTo?: string;
  assignedToName?: string;
  reminderMinutes?: number;
  createdAt: string;
}

// ===== SaaS / Multi-tenant =====
export interface Tenant {
  id: string;
  name: string;
  slug: string;
  document?: string;
  email?: string;
  phone?: string;
  planId?: string;
  planName?: string;
  status: "TRIAL" | "ACTIVE" | "SUSPENDED" | "CANCELLED";
  trialEndsAt?: string;
  dueDate?: string;
  userCount: number;
  createdAt: string;
}

export interface Plan {
  id: string;
  name: string;
  description?: string;
  price: number;
  maxUsers: number;
  maxChannels: number;
  maxQueues: number;
  maxContacts: number;
  active: boolean;
}

export interface ApiToken {
  id: string;
  name: string;
  tokenPrefix: string;
  scopes: string[];
  lastUsedAt?: string;
  expiresAt?: string;
  active: boolean;
  createdAt: string;
  plainToken?: string;
}

export interface Channel {
  id: string;
  name: string;
  type: "WHATSAPP" | "INSTAGRAM" | "FACEBOOK" | "TELEGRAM" | "EMAIL" | "SMS" | "WEBCHAT";
  status: "DISCONNECTED" | "CONNECTING" | "CONNECTED" | "ERROR";
  isDefault: boolean;
  createdAt: string;
}
