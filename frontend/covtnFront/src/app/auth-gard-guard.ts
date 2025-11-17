import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivate, CanActivateChild, Router, RouterStateSnapshot } from "@angular/router";
import { ApiService } from "./api-service";

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate , CanActivateChild {
  constructor(private authService: ApiService, private router: Router) {}
  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    const authenticated = await this.authService.isAuthenticated();
    if (!authenticated) {
      this.router.navigate(['/auth']);
      this.authService.logout();
      return false;
    }
    return true;
  }
  async canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    return this.canActivate(route, state);
  }
}