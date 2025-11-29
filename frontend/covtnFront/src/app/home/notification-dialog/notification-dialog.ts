import { Component } from '@angular/core';
import { NotificationService } from '../../notification-service';

@Component({
  selector: 'app-notification-dialog',
  templateUrl: './notification-dialog.html',
  styleUrls: ['./notification-dialog.css'],
  standalone: false
})
export class NotificationDialog {
  constructor(public notificationService: NotificationService) {
    // Mark notifications as read when dialog opens
    this.notificationService.markAsRead();
  }
}
