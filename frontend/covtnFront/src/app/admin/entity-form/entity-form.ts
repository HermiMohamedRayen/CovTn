import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Entity, EntityService } from '../entity';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-entity-form',
  templateUrl: './entity-form.html',
  styleUrls: ['./entity-form.css'],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule
  ]
})
export class EntityForm  {
  entityForm: FormGroup;
  isEditMode = true;
  entityId: String | null = null;
  userData = signal<any>(null);

  constructor(
    private fb: FormBuilder,
    private entityService: EntityService,
    private route: ActivatedRoute,
    private router: Router,
  ) {
    const nav = this.router.getCurrentNavigation();
      this.entityId = nav?.extras.state?.['email'];
      if(!this.entityId){
        this.router.navigate(['/admin']);
      }
      this.entityService.getById(String(this.entityId)).subscribe({
        next: (entity) => {
          this.entityForm.patchValue(entity);
        },
        error: (err) => {
          console.error('Erreur lors du chargement de l\'entité', err);
        }
      });
    this.entityForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      password: [''],
      roles: ['', Validators.required] 
      // Ajoutez d'autres contrôles de formulaire ici
    });
  }

  loadEntity(id: String | string): void {
    this.entityService.getById(String(id)).subscribe({
      next: (entity) => {
        this.entityForm.patchValue(entity);
      },
      error: (err) => {
        console.error('Erreur lors du chargement de l\'entité', err);
      }
    });
  }

  onSubmit(): void {
    if (this.entityForm.valid) {
      const entity: Entity = this.entityForm.value;

      if (this.isEditMode && this.entityId !== null) {
        // Mode Mise à jour (Update)
        this.entityService.update( entity).subscribe({
          next: () => {
            alert('Entity updated successfully');
          },
          error: (err) => {
            console.error('Erreur lors de la mise à jour de l\'entité', err);
          }
        });
      } else {
        // Mode Création (Create)
        this.entityService.create(entity).subscribe({
          next: () => {
            this.router.navigate(['/admin/entities/new']);
          },
          error: (err) => {
            console.error('Erreur lors de la création de l\'entité', err);
          }
        });
      }
    }
  }
}
