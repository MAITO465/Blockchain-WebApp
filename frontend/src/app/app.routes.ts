import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AddressComponent } from './components/address/address.component';
import { BalanceComponent } from './components/balance/balance.component';
import { TransactionComponent } from './components/transaction/transaction.component';
import { MessageComponent } from './components/message/message.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'addresses', component: AddressComponent },
  { path: 'balance', component: BalanceComponent },
  { path: 'transactions', component: TransactionComponent },
  { path: 'messages', component: MessageComponent },
  { path: '**', redirectTo: '' }
];
