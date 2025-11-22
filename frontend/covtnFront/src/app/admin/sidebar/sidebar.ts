import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {
  navLinks = [
    { path: '/admin/entities', label: 'Gérer les Utilisateurs' },
    { path: '/admin/rides', label: 'Gérer les Rides' },
    { path: '/admin/statistics', label: 'Statistiques' },
  ];

}
