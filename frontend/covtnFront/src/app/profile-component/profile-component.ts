import { Component } from '@angular/core';
import { ApiService } from '../api-service';
import { Router } from '@angular/router';
import { Location } from '@angular/common';



@Component({
  selector: 'app-profile-component',
  standalone: false,
  templateUrl: './profile-component.html',
  styleUrls: [
    './profile-component.css'
  ]
  
})
export class ProfileComponent {
  user = ApiService.user;

  constructor(
    protected apiService: ApiService,
    private router: Router,
    private location: Location
  ) { }


  logout() {
    this.apiService.logout();
    window.location.reload()
  }

  previousPage() {
    this.location.back();
  }



}
