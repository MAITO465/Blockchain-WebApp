import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="auth-box">
        <h2>Inscription</h2>
        <div class="error-msg" *ngIf="error">{{ error }}</div>
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Nom d'utilisateur</label>
            <input type="text" [(ngModel)]="username" name="username" required minlength="3">
          </div>
          <div class="form-group">
            <label>Mot de passe</label>
            <input type="password" [(ngModel)]="password" name="password" required minlength="6">
          </div>
          <button type="submit" [disabled]="!username || !password">Créer mon compte</button>
        </form>
        <p class="auth-link">Déjà un compte ? <a routerLink="/login">Se connecter</a></p>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; height: 80vh; }
    .auth-box { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
    h2 { text-align: center; color: #333; margin-bottom: 1.5rem; }
    .form-group { margin-bottom: 1rem; }
    label { display: block; margin-bottom: 0.5rem; color: #666; }
    input { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
    button { width: 100%; padding: 0.75rem; background: #2ecc71; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }
    button:disabled { background: #95a5a6; cursor: not-allowed; }
    .error-msg { color: #e74c3c; margin-bottom: 1rem; text-align: center; }
    .auth-link { text-align: center; margin-top: 1rem; color: #666; }
    .auth-link a { color: #2ecc71; text-decoration: none; }
  `]
})
export class RegisterComponent {
  username = '';
  password = '';
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    this.authService.register({ username: this.username, password: this.password }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => this.error = 'Erreur lors de l\'inscription (nom peut-être déjà pris)'
    });
  }
}
