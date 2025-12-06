import { Component, OnInit, signal } from '@angular/core';
import { NavigationExtras, Router } from '@angular/router';
import { Entity, EntityService } from '../entity';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-entity-list',
  templateUrl: './entity-list.html',
  styleUrls: ['./entity-list.css'],
  imports: [
    CommonModule,
    RouterModule,
    TitleCasePipe
  ]
})
export class EntityList implements OnInit {
  entities = signal<any[]>([]);
  // Définir les colonnes à afficher dans le tableau
  displayedColumns: string[] = ['id', 'prenom', 'nom', 'actions'];

  constructor(private entityService: EntityService, private router: Router) { }

  ngOnInit(): void {
    this.loadEntities();
  }

  loadEntities(): void {
    this.entityService.getAll().subscribe({
      next: (data) => {
        this.entities.update(() => data);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des entités', err);
        // Gérer l'erreur (ex: afficher un message à l'utilisateur)
      }
    });
  }

  editEntity(email: string): void {
    const extras: NavigationExtras = { state: { email: email } };
    this.router.navigate(['admin/entities/edit'], extras);
  }

  deleteEntity(email: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette entité ?')) {
      this.entityService.delete(email).subscribe({
        next: () => {
          this.loadEntities(); // Recharger la liste après suppression
        },
        error: (err) => {
          console.error('Erreur lors de la suppression de l\'entité', err);
        }
      });
    }
  }
}
