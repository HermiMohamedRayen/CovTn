import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../api-service';

// Interface pour l'entité générique
export interface Entity {
  id?: number;
  name: string;
  description: string;
  // Ajoutez d'autres propriétés de votre entité ici
}

@Injectable({
  providedIn: 'root'
})
export class EntityService {
  // Remplacez par l'URL de votre API backend
  private apiUrl = 'http://localhost:9092/api/admin';

  constructor(
    private http: HttpClient,
    private apiService: ApiService
  ) { }

  // READ - Récupérer toutes les entités
  getAll(): Observable<Entity[]> {
    return this.http.get<Entity[]>(`${this.apiUrl}/users`,
      {headers:{'Authorization': `Bearer ${this.apiService.loadToken()}`}}
    );
  }

  // READ - Récupérer une entité par ID
  getById(email: string): Observable<Entity> {
    return this.http.get<Entity>(`${this.apiUrl}/users/${email}`, {headers:{'Authorization': `Bearer ${localStorage.getItem('token')}`}});
  }

  // CREATE - Créer une nouvelle entité
  create(entity: Entity): Observable<Entity> {
    return this.http.post<Entity>(`${this.apiUrl}/addUsers`, entity);
  }

  // UPDATE - Mettre à jour une entité existante
  update( entity: Entity): Observable<Entity> {
    return this.http.put<Entity>(`${this.apiUrl}/Updateusers`, entity, {headers:{'Authorization': `Bearer ${localStorage.getItem('token')}`}});
  }

  // DELETE - Supprimer une entité
  delete(email: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${email}`);
  }
}
