import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../api-service';
import { MatDialog } from '@angular/material/dialog';
import { NotificationService } from '../../notification-service';
import { NotificationDialog } from '../notification-dialog/notification-dialog';

@Component({
  selector: 'app-nav-bar',
  standalone: false,
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {

  constructor(
    protected apiService: ApiService, 
    private router: Router,
    private dialog: MatDialog,
    public notificationService: NotificationService
  ) {}

  logout() {
    this.apiService.logout();
    window.location.reload()
  }

  openNotifications() {
    this.dialog.open(NotificationDialog, {
      width: '400px',
      position: { top: '70px', right: '20px' },
      panelClass: 'notification-dialog-container'
    });
  }

}
