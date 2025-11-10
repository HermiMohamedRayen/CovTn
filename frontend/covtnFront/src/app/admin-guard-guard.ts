import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, GuardResult, MaybeAsync, Router, RouterStateSnapshot,  } from '@angular/router';
import { ApiService } from './api-service';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate, CanActivateChild {
  constructor(private apiService: ApiService, private router: Router) {}
  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return this.canActivate();
  }

  async canActivate(): Promise<boolean> {
    const isAdmin = this.apiService.hasRole('ROLE_ADMIN');
    if (!isAdmin) {
      this.router.navigate(['/']);
    }
    return isAdmin;
  }
}
