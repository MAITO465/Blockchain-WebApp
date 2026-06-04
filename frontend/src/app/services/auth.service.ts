import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface AuthResponse {
  token: string;
  username: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = '/api/auth';
  
  // Using BehaviorSubject to easily track and broadcast login state
  private currentUserSubject = new BehaviorSubject<string | null>(this.getUsername());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(res => {
          localStorage.setItem('jwt_token', res.token);
          localStorage.setItem('username', res.username);
          this.currentUserSubject.next(res.username);
        })
      );
  }

  register(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, credentials)
      .pipe(
        tap(res => {
          localStorage.setItem('jwt_token', res.token);
          localStorage.setItem('username', res.username);
          this.currentUserSubject.next(res.username);
        })
      );
  }

  logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  getUsername(): string | null {
    return localStorage.getItem('username');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
