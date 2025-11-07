import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing-module';
import { Admin } from './admin';
import { AdminDashboard } from './admin-dashboard';
import { Sidebar } from './sidebar/sidebar';
import { EntityList } from './entity-list/entity-list';
import { EntityForm } from './entity-form/entity-form';
import { ReactiveFormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    Admin,
    AdminDashboard,
    Sidebar,
    
  ],
  imports: [
    CommonModule,
    AdminRoutingModule,
    ReactiveFormsModule,
    EntityForm,
    EntityList
  ]
})
export class AdminModule { }
