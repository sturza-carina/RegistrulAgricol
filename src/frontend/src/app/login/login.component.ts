import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  error: string = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          const user = this.authService.currentUserSubject.value;
          if (user?.role === 'ROLE_SUPER_ADMIN') {
            this.router.navigate(['/super-admin']);
          } else if (user?.role === 'ROLE_ADMIN') {
            this.router.navigate(['/tenant-admin']);
          } else {
            this.router.navigate(['/user-management']);
          }
        },
        error: err => {
          this.error = 'Invalid credentials';
        }
      });
    }
  }
}
