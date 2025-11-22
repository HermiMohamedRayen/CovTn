import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../api-service';

@Component({
  selector: 'app-admin-navbar',
  standalone: false,
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class AdminNavbar {
  user = ApiService.user;
  mobileMenuOpen = false;
  userMenuOpen = false;

  constructor(private router: Router, private apiService: ApiService) {}

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
    if (this.mobileMenuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  }

  toggleUserMenu(event: Event): void {
    event.stopPropagation();
    this.userMenuOpen = !this.userMenuOpen;
  }

  closeMenus(): void {
    this.mobileMenuOpen = false;
    this.userMenuOpen = false;
    document.body.style.overflow = '';
  }

  isActive(route: string): boolean {
    return this.router.isActive(route, {
      paths: 'subset',
      queryParams: 'subset',
      fragment: 'ignored',
      matrixParams: 'ignored'
    });
  }

  logout(): void {
    if (confirm('Are you sure you want to logout?')) {
      this.apiService.logout();
      this.router.navigate(['/login']);
    }
  }

  navigateTo(path: string): void {
    this.closeMenus();
    this.router.navigate([path]);
  }

  handleClickOutside(event: Event): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown') && !target.closest('.user-info')) {
      this.userMenuOpen = false;
    }
  }
}
