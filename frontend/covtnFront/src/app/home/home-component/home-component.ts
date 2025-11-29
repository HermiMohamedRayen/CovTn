import { Component } from '@angular/core';
import { NotificationService } from '../../notification-service';

@Component({
  selector: 'app-home-component',
  standalone: false,
  templateUrl: './home-component.html',
  styleUrl: './home-component.css'
})
export class HomeComponent {
  constructor(private notificationService: NotificationService) {
    this.notificationService.connect();
  }

}
