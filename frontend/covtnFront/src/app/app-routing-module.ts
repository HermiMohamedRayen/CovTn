import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainComponent } from './main-component/main-component';
import { AuthGuard } from './auth-gard-guard';
import { AuthentificationComponent } from './authentification-component/authentification-component';
import { MailVerifyComponent } from './mail-verify-component/mail-verify-component';

const routes: Routes = [
  { path: '', component: MainComponent, canActivate: [AuthGuard] },
  { path: 'auth', component: AuthentificationComponent },
  { path: 'admin', loadChildren: () => import('./admin/admin-module').then(m => m.AdminModule) },
  { path: 'mail-verify', component: MailVerifyComponent }


];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
