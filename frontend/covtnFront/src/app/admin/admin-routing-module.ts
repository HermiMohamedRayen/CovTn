import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EntityList } from './entity-list/entity-list' ;
import {EntityForm } from './entity-form/entity-form'; 


import { Admin } from './admin';

const routes: Routes = [
  {
    path: '',
    component: Admin,
    children: [
      { path: 'entities', component: EntityList },
      { path: 'entities/new', component: EntityForm },
      { path: 'entities/delete/:email', component: EntityList },
      { path: 'entities/edit', component: EntityForm },
      { path: '', redirectTo: 'entities', pathMatch: 'full' },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
