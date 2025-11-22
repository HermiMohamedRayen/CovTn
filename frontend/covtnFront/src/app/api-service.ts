import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private apiUrl = 'http://localhost:9092/api';
  constructor(private http: HttpClient) { }

  public static user = signal<any>(null);


  login(user: { username: string, password: string }): Observable<String> {
    return this.http.post<String>(`${this.apiUrl}/auth/login`, user, { responseType: 'text' as 'json' });
  }
  async isAuthenticated(): Promise<boolean> {
    const token = this.loadToken();
    if (token == null) {
      return Promise.resolve(false);
    }
    return new Promise<boolean>((resolve) => {
      this.http.get(`${this.apiUrl}/auth/me`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }).subscribe({
        next: (user) => {
          ApiService.user.set(user);
          resolve(true);
        },
        error: (e) => {
          this.logout();
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
      this.http.post<String>(`${this.apiUrl}/auth/validateMail`,  verificationCode , {responseType: 'text' as 'json'} ).subscribe({
        next: (reponse) => {
          const token = reponse;
          this.setToken(token.toString());
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
  const token = this.loadToken();
  if (!token) return false;
  const payload = ApiService.user().roles;
  return payload.includes(role);
}
  refreshToken() {
    const token = this.loadToken();
    const retoken = this.http.get<String>(`${this.apiUrl}/auth/refreshToken`, {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'text' as 'json'
    });
    retoken.subscribe({
      next: (newToken) => {
        console.log('Token refreshed successfully');
        this.setToken(newToken.toString());
      },
      error: (err) => {
        console.error('Error refreshing token:', err);
      }
    });
  }

  refreshTokenAsync(): Observable<string> {
    const token = this.loadToken();
    return this.http.get<string>(`${this.apiUrl}/auth/refreshToken`, {
      headers: { Authorization: `Bearer ${token}` },
      responseType: 'text' as 'json'
    }).pipe(
      switchMap((newToken) => {
        console.log('Token refreshed successfully');
        this.setToken(newToken.toString());
        return [newToken.toString()];
      })
    );
  }
  logout() {
    localStorage.clear();
  }

  loadToken(): string | null {
    return localStorage.getItem('token');
  }
  setToken(token: string): void {
    localStorage.setItem('token', token);
  }
  getProfileImage() : Promise<String> {

    return new Promise<String>((resolve, reject) => {
      this.http.get(`${this.apiUrl}/user/profile/picture`, {
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'blob'
      }).subscribe({
        next: (image) => {
          const file = new File([image], 'profile.jpg');
          resolve(URL.createObjectURL(file));
        },
        error: (e) => {
          console.log('No profile image found.', e);
          reject(e);
        }
      });
    });
  }
  getImage(email: string) : Promise<String> {

    return new Promise<String>((resolve, reject) => {
      this.http.get(`${this.apiUrl}/user/userProfile/picture?email=${email}`, {
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'blob'
      }).subscribe({
        next: (image) => {
          const file = new File([image], 'profile.jpg');
          resolve(URL.createObjectURL(file));
        },
        error: (e) => {
          console.log('No profile image found.', e);
          reject(e);
        }
      });
    });
  }

  updateProfileImage(imageFile: File): Promise<void> {
    const formData = new FormData();
    formData.append('file', imageFile);

    return new Promise<void>((resolve, reject) => {
      this.http.post(`${this.apiUrl}/user/profile/updatePicture`, formData, {
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'text'
      }).subscribe({
        next: () => {
          resolve();
        },
        error: (e) => {
          reject(e);
        }
      });
    });
  }
  becomeDriver(){
    return new Promise<void>((resolve, reject) => {
      this.http.get(`${this.apiUrl}/user/becomeDriver`, {
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'text'
      }).subscribe({
        next: () => {
          resolve();
        },
        error: (e) => {
          reject(e);
        }
      });
    });
  }

  addCar(car: {matriculationNumber: string, model: string, seats: number, airConditioner: boolean, smoker: boolean, photos: File[]}): Observable<any> {
    const files = car.photos;
    const c = { ...car };
    c.photos = [];
    const formData = new FormData();
    formData.append('car',new Blob([JSON.stringify(c)], { type: 'application/json' }));
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }
    return this.http.post(`${this.apiUrl}/driver/car`, formData, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      },
      responseType: 'text'
    });
  }

  proposeRide(ride: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/driver/proposeRide`, ride, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      },
      responseType: 'text'
    });
  }
  getRideById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/user/ride/${id}`, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      }
    });
  }

  searchRides(deplat: number, delong: number, destlat: number, destlong: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/user/searchRides?deplat=${deplat}&deplon=${delong}&destlat=${destlat}&destlon=${destlong}`, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      }
    });
  }

  getCarPhotoByName(name: string, email: string): Promise<String> {
    return new Promise<String>((resolve, reject) => {
      this.http.get(`${this.apiUrl}/user/car/photo?name=${name}&email=${email}`, {
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'blob'
      }).subscribe({
        next: (photos) => {
          const img = new File([photos],name);
          resolve(URL.createObjectURL(img));
        },
        error: (e) => {
          reject(e);
        }
      });
    });
  }
  getLatestRides() { 
    return this.http.get(`${this.apiUrl}/user/latestRide`, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      }
    });
  }
  updatePhoneNumber(phoneNumber: string) {
    return this.http.put(`${this.apiUrl}/user/phoneNumber/${phoneNumber}`, { phoneNumber: phoneNumber },{
        headers: {
          'Authorization': `Bearer ${this.loadToken()}`
        },
        responseType: 'text'
      })
  }

  updateCar(carId: number, car: any): Observable<any> {
    const files = car.photos?.filter((p: any) => p instanceof File) || [];
    const c = { ...car };
    c.photos = car.photos?.filter((p: any) => !(p instanceof File)) || [];
    
    const formData = new FormData();
    formData.append('car', new Blob([JSON.stringify(c)], { type: 'application/json' }));
    for (let i = 0; i < files.length; i++) {
      formData.append('files', files[i]);
    }
    return this.http.put(`${this.apiUrl}/driver/car/${carId}`, formData, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      },
      responseType: 'text'
    });
  }

  getComments(rideId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/ride/${rideId}/comments`, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      }
    });
  }

  addComment(rideId: number, comment: { text: string, rating: number }): Observable<any> {
    return this.http.post(`${this.apiUrl}/ride/${rideId}/comment`, comment, {
      headers: {
        'Authorization': `Bearer ${this.loadToken()}`
      },
      responseType: 'text'
    });
  }

}