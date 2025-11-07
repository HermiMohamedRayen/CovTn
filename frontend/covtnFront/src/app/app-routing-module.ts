import { Input, NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './main-component/main-component';
import { Login } from './login/login';
import { Sign } from './sign/sign';
import { Admin } from './admin/admin';

const routes: Routes = [
  {path : "" , component : MainComponent},
  { path: 'admin', loadChildren: () => import('./admin/admin-module').then(m => m.AdminModule) },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
 }
