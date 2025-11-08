import { Component, OnInit } from '@angular/core';
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
export class EntityForm implements OnInit {
  entityForm: FormGroup;
  isEditMode = false;
  entityId: String | null = null;

  constructor(
    private fb: FormBuilder,
    private entityService: EntityService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.entityForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      password: ['', Validators.required],
      roles: ['', Validators.required] 
      // Ajoutez d'autres contrôles de formulaire ici
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('email');
      if (id) {
        this.isEditMode = true;
        this.entityId = id;
        this.loadEntity(id);
      }
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
        this.entityService.update(String(this.entityId), entity).subscribe({
          next: () => {
            this.router.navigate(['/admin/entities/edit', this.entityId]);
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
