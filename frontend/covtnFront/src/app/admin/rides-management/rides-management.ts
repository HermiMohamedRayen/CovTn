import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RideService, Ride } from '../../ride-service';

@Component({
  selector: 'app-rides-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rides-management.html',
  styleUrl: './rides-management.css'
})
export class RidesManagement implements OnInit {

  rides: Ride[] = [];
  filteredRides: Ride[] = [];
  filter: 'all' | 'approved' | 'pending' = 'all';
  loading = false;
  error: string | null = null;

  constructor(private rideService: RideService) { }

  ngOnInit(): void {
    this.loadRides();
  }

  loadRides(): void {
    this.loading = true;
    this.error = null;
    this.rideService.getAllRides().subscribe({
      next: (rides) => {
        this.rides = rides;
        this.applyFilter();
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur lors du chargement des rides:', err);
        this.error = 'Erreur lors du chargement des rides';
        this.loading = false;
      }
    });
  }

  applyFilter(): void {
    switch (this.filter) {
      case 'approved':
        this.filteredRides = this.rides.filter(r => r.approved);
        break;
      case 'pending':
        this.filteredRides = this.rides.filter(r => !r.approved);
        break;
      default:
        this.filteredRides = this.rides;
    }
  }

  setFilter(filter: 'all' | 'approved' | 'pending'): void {
    this.filter = filter;
    this.applyFilter();
  }

  approveRide(id: number | undefined): void {
    if (!id) return;
    this.rideService.approveRide(id).subscribe({
      next: () => {
        this.loadRides();
      },
      error: (err) => {
        console.error('Erreur lors de l\'approbation du ride:', err);
        this.error = 'Erreur lors de l\'approbation du ride';
      }
    });
  }

  rejectRide(id: number | undefined): void {
    if (!id) return;
    this.rideService.rejectRide(id).subscribe({
      next: () => {
        this.loadRides();
      },
      error: (err) => {
        console.error('Erreur lors du rejet du ride:', err);
        this.error = 'Erreur lors du rejet du ride';
      }
    });
  }

  deleteRide(id: number | undefined): void {
    if (!id) return;
    if (confirm('Êtes-vous sûr de vouloir supprimer ce ride ?')) {
      this.rideService.deleteRide(id).subscribe({
        next: () => {
          this.loadRides();
        },
        error: (err) => {
          console.error('Erreur lors de la suppression du ride:', err);
          this.error = 'Erreur lors de la suppression du ride';
        }
      });
    }
  }

  getDriverInfo(ride: Ride): string {
    if (ride.driver) {
      return `${ride.driver.firstName || ''} ${ride.driver.lastName || ''} (${ride.driver.email || ''})`;
    }
    return 'Unknown';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
