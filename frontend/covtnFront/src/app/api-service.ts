import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrl = 'http://localhost:9092/api';
  constructor(private http: HttpClient) { }


  login(user: { username: string, password: string }): Observable<String> {
    return this.http.post<String>(`${this.apiUrl}/auth/login`, user, { responseType: 'text' as 'json' });
  }
  isAuthenticated(): Promise<boolean> {
    const token = localStorage.getItem('token');
    if (token == null) {
      return Promise.resolve(false);
    }
    return new Promise<boolean>((resolve) => {
      this.http.get(`${this.apiUrl}/auth/me`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }).subscribe({
        next: () => {
          resolve(true);
        },
        error: () => {
          localStorage.removeItem('token');
          resolve(false);
        }
      });
    });

  }
  signUp(user: { firstName: string, lastName: string, email: string, password: string }): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, user, { responseType: 'text' as 'json' });
  }

  verifyEmail(verificationCode: any): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.http.post<String>(`${this.apiUrl}/auth/validateMail`,  verificationCode , { responseType: 'text' as 'json' }).subscribe({
        next: (reponse) => {
          const token = reponse;
          localStorage.setItem('token', token.toString());
          resolve(true);
        },
        error: (err : string) => {
          console.error("Email verification failed:", err);
          resolve(false);
        }
      });
    });
  }

  hasRole(role: string): boolean {
  const token = localStorage.getItem('token');
  if (!token) return false;
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.roles.includes(role);
}
  refreshToken() {
    const token = localStorage.getItem('token');
    const retoken = this.http.get<String>(`${this.apiUrl}/auth/refreshToken`, {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'text' as 'json'
    });
    retoken.subscribe({
      next: (newToken) => {
        console.log('Token refreshed successfully');
        localStorage.setItem('token', newToken.toString());
      },
      error: (err) => {
        console.error('Error refreshing token:', err);
      }
    });
  }
  logout() {
    localStorage.clear();
  }

}
