import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api-service';

export interface Ride {
  id?: number;
  departure: any;
  destination: any;
  departureTime: string;
  arrivalTime: string;
  approved: boolean;
  driver: any;
  rideParticipations?: any[];
}

export interface RideStatistics {
  totalRides: number;
  approvedRides: number;
  pendingRides: number;
  totalUsers: number;
  totalDrivers: number;
  totalPassengers: number;
}

@Injectable({
  providedIn: 'root'
})
export class RideService {

  private apiUrl = 'http://localhost:9092/api/admin';

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) { }

  getAllRides(): Observable<Ride[]> {
    return this.http.get<Ride[]>(`${this.apiUrl}/rides`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  getApprovedRides(): Observable<Ride[]> {
    return this.http.get<Ride[]>(`${this.apiUrl}/rides/approved`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  getPendingRides(): Observable<Ride[]> {
    return this.http.get<Ride[]>(`${this.apiUrl}/rides/pending`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  getRideById(id: number): Observable<Ride> {
    return this.http.get<Ride>(`${this.apiUrl}/rides/${id}`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  approveRide(id: number): Observable<Ride> {
    return this.http.put<Ride>(`${this.apiUrl}/rides/${id}/approve`, {}, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  rejectRide(id: number): Observable<Ride> {
    return this.http.put<Ride>(`${this.apiUrl}/rides/${id}/reject`, {}, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }

  deleteRide(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/rides/${id}`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` },
      responseType: 'text'
    });
  }

  getStatistics(): Observable<RideStatistics> {
    return this.http.get<RideStatistics>(`${this.apiUrl}/statistics`, {
      headers: { 'Authorization': `Bearer ${this.apiService.loadToken()}` }
    });
  }
}
