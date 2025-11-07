import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  standalone: false,
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {
  navLinks = [
    { path: '/admin/entities', label: 'Gérer les Entités' },
    // Ajoutez d'autres liens ici pour d'autres types d'entités si nécessaire
    // { path: '/admin/users', label: 'Gérer les Utilisateurs' },
  ];

}
