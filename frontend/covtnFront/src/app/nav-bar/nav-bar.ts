import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nav-bar',
  standalone: false,
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {

  constructor(private router: Router) {}

  logout() {
    // Clear any stored authentication tokens
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
    
    // Navigate to login page
    window.location.reload()
  }

}
