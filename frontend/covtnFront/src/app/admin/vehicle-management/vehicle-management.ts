import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../api-service';

interface Vehicle {
  id?: string;
  brand: string;
  model: string;
  year: number;
  licensePlate: string;
  color: string;
  seats: number;
  isActive: boolean;
}

@Component({
  selector: 'app-vehicle-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicle-management.html',
  styleUrls: ['./vehicle-management.css']
})
export class VehicleManagementComponent implements OnInit {
  
  vehicles: Vehicle[] = [];
  currentUser: any = null;
  isLoading = true;
  error: string | null = null;
  
  showForm = false;
  isEditing = false;
  currentVehicleId: string | null = null;
  
  vehicleForm: Vehicle = {
    brand: '',
    model: '',
    year: new Date().getFullYear(),
    licensePlate: '',
    color: '',
    seats: 4,
    isActive: true
  };

  constructor(private apiService: ApiService) {
    this.currentUser = ApiService.user;
  }

  ngOnInit(): void {
    this.loadVehicles();
  }

  loadVehicles(): void {
    this.isLoading = true;
    this.isLoading = false;
  }

  showAddForm(): void {
    this.isEditing = false;
    this.currentVehicleId = null;
    this.resetForm();
    this.showForm = true;
  }

  showEditForm(vehicle: Vehicle): void {
    this.isEditing = true;
    this.currentVehicleId = vehicle.id || null;
    this.vehicleForm = { ...vehicle };
    this.showForm = true;
  }

  resetForm(): void {
    this.vehicleForm = {
      brand: '',
      model: '',
      year: new Date().getFullYear(),
      licensePlate: '',
      color: '',
      seats: 4,
      isActive: true
    };
  }

  onSubmit(): void {
    if (!this.currentUser()) return;
    if (this.isEditing && this.currentVehicleId) {
      this.loadVehicles();
      this.showForm = false;
    } else {
      this.loadVehicles();
      this.showForm = false;
    }
  }

  deleteVehicle(vehicleId: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce véhicule ?')) {
      this.loadVehicles();
    }
  }

  toggleVehicleStatus(vehicle: Vehicle): void {
    this.loadVehicles();
  }
}
