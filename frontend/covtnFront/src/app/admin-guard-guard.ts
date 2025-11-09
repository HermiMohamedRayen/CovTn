import { CanActivate, Router,  } from '@angular/router';
import { ApiService } from './api-service';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {
  constructor(private apiService: ApiService, private router: Router) {}

  async canActivate(): Promise<boolean> {
    const isAdmin = this.apiService.hasRole('ROLE_ADMIN');
    if (!isAdmin) {
      this.router.navigate(['/']);
    }
    return isAdmin;
  }
}
