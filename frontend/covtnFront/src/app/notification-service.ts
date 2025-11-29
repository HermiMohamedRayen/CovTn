import { Injectable, signal } from '@angular/core';
import { ApiService } from './api-service';

export interface Notification {
  id?: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private eventSource!: EventSource;
  public notifications = signal<Notification[]>([]);
  public unreadCount = signal<number>(0);

  constructor(private apiService: ApiService) {}

  connect() {
    const token = this.apiService.loadToken();
    if (!token) return;

    this.eventSource = new EventSource(
      `${ApiService.baseapiUrl}/notifications/stream?token=${token}`
    );

    this.eventSource.addEventListener("notification", (event: any) => {
      const newNotification: Notification = {
        message: event.data,
        timestamp: new Date(),
        read: false
      };
      
      this.notifications.update(current => [newNotification, ...current]);
      this.unreadCount.update(count => count + 1);
      
      // Optional: keep only last 50 notifications
      if (this.notifications().length > 50) {
        this.notifications.update(current => current.slice(0, 50));
      }
    });
    
    this.eventSource.onerror = (error) => {
      console.error('EventSource failed:', error);
      this.eventSource.close();
      // Retry connection after some time if needed
    };
  }

  markAsRead() {
    this.unreadCount.set(0);
    this.notifications.update(current => 
      current.map(n => ({ ...n, read: true }))
    );
  }
}
