import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RideService, RideStatistics } from '../../ride-service';

@Component({
  selector: 'app-statistics-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistics-dashboard.html',
  styleUrl: './statistics-dashboard.css'
})
export class StatisticsDashboard implements OnInit {

  statistics: RideStatistics | null = null;
  loading = false;
  error: string | null = null;
  refreshInterval: any;

  constructor(private rideService: RideService) { }

  ngOnInit(): void {
    this.loadStatistics();
    this.refreshInterval = setInterval(() => {
      this.loadStatistics();
    }, 30000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  loadStatistics(): void {
    this.loading = true;
    this.error = null;
    this.rideService.getStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des statistiques:', err);
        this.error = 'Erreur lors du chargement des statistiques';
        this.loading = false;
      }
    });
  }

  getApprovalPercentage(): number {
    if (!this.statistics || this.statistics.totalRides === 0) {
      return 0;
    }
    return Math.round((this.statistics.approvedRides / this.statistics.totalRides) * 100);
  }

  getPassengersCount(): number {
    if (!this.statistics) {
      return 0;
    }
    return this.statistics.totalUsers - this.statistics.totalDrivers;
  }
}
