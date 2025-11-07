import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  
  private apiUrl = 'http://localhost:9092/api'; 
  constructor(private http: HttpClient) { }


  login(user: {username: string, password: string}): Observable<String> {
    return this.http.post<String>(`${this.apiUrl}/auth/login`, user,{ responseType: 'text' as 'json' });
  }
  isAuthenticated(): Promise<boolean> {
    const token = localStorage.getItem('token');
    if(token==null){
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
  signUp(user: {firstName: string, lastName: string, email: string, password: string}): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, user, { responseType: 'text' as 'json' });
  }

}
