import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../api-service';

@Component({
  selector: 'app-nav-bar',
  standalone: false,
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {

  constructor(protected apiService: ApiService, private router: Router) {}

  logout() {
    this.apiService.logout();
    window.location.reload()
  }

}
