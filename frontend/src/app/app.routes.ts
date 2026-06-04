import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AddressComponent } from './components/address/address.component';
import { BalanceComponent } from './components/balance/balance.component';
import { TransactionComponent } from './components/transaction/transaction.component';
import { MessageComponent } from './components/message/message.component';
import { LoginComponent } from './components/auth/login.component';
import { RegisterComponent } from './components/auth/register.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: '', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'addresses', component: AddressComponent, canActivate: [authGuard] },
  { path: 'balance', component: BalanceComponent, canActivate: [authGuard] },
  { path: 'transactions', component: TransactionComponent, canActivate: [authGuard] },
  { path: 'messages', component: MessageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
