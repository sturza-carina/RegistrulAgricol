import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpClient } from '@angular/common/http';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SidebarComponent],
  templateUrl: './user-management.component.html',
})
export class UserManagementComponent implements OnInit {

  user: any = null;
  users: any[] = [];
  filteredUsers: any[] = [];
  roleFilter = 'Toate Rolurile';
  statusFilter = 'Toate Statusurile';
  searchTerm = '';

  creatingUser = false;
  viewingUser: any = null;
  editingUser: any = null;

  userForm: FormGroup;
  successMessage = '';
  errorMessage = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private http: HttpClient,
    private fb: FormBuilder
  ) {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      nume: [''],
      email: ['', [Validators.required, Validators.email]],
      password: [''],
      role: ['ROLE_USER', Validators.required],
      activ: [true]
    });
  }

  ngOnInit(): void {
    this.authService.currentUser.subscribe(u => {
      if (!u) {
        this.router.navigate(['/login']);
      } else {
        this.user = u;
        this.loadUsers();
      }
    });

    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'create') {
        this.openAddForm();
      }
    });
  }

  loadUsers(): void {
    this.http.get<any[]>('/api/users').subscribe({
      next: (data) => {
        this.users = data.map(u => ({
          id: u.id,
          name: u.nume || u.username,
          initials: (u.nume ? u.nume.charAt(0) : u.username.charAt(0)).toUpperCase(),
          handle: '@' + u.username,
          avatarBg: '#0369a1',
          img: null,
          email: u.email || 'No email provided',
          role: u.role,
          roleDisplay: u.role ? u.role.replace('ROLE_', '') : 'USER',
          status: u.activ ? 'Activ' : 'Inactiv',
          lastLogin: 'Unknown',
          raw: u
        }));
        this.applyFilter();
      },
      error: () => {
        this.users = [];
        this.filteredUsers = [];
        console.error('Failed to load users');
      }
    });
  }

  onSearch(event: any): void {
    this.searchTerm = event.target.value.toLowerCase();
    this.applyFilter();
  }

  applyFilter(): void {
    this.filteredUsers = this.users.filter(u => {
      const matchesSearch = u.name.toLowerCase().includes(this.searchTerm) || u.handle.toLowerCase().includes(this.searchTerm) || u.email.toLowerCase().includes(this.searchTerm);
      const matchesRole = this.roleFilter === 'Toate Rolurile' || 
                          (this.roleFilter === 'Administrator' && u.role === 'ROLE_ADMIN') ||
                          (this.roleFilter === 'Super Administrator' && u.role === 'ROLE_SUPER_ADMIN') ||
                          (this.roleFilter === 'Utilizator / Registrator' && u.role === 'ROLE_USER');
      const matchesStatus = this.statusFilter === 'Toate Statusurile' || u.status === this.statusFilter;
      return matchesSearch && matchesRole && matchesStatus;
    });
  }

  openAddForm(): void {
    this.creatingUser = true;
    this.viewingUser = null;
    this.editingUser = null;
    this.userForm.reset({ role: 'ROLE_USER', activ: true });
    this.userForm.get('password')?.setValidators([Validators.required]);
    this.userForm.get('password')?.updateValueAndValidity();
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeAddForm(): void {
    this.creatingUser = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  viewUser(u: any): void {
    this.viewingUser = u;
    this.creatingUser = false;
    this.editingUser = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeViewUser(): void {
    this.viewingUser = null;
  }

  editUser(u: any): void {
    this.editingUser = { ...u.raw };
    this.viewingUser = null;
    this.creatingUser = false;
    this.userForm.patchValue({
      username: u.raw.username,
      nume: u.raw.nume,
      email: u.raw.email,
      role: u.raw.role,
      activ: u.raw.activ
    });
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.errorMessage = '';
    this.successMessage = '';
  }

  closeEditUser(): void {
    this.editingUser = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  saveUser(): void {
    if (this.userForm.invalid) {
      this.errorMessage = 'Vă rugăm să completați corect toate câmpurile obligatorii.';
      return;
    }
    const userData = this.userForm.getRawValue();
    
    if (this.editingUser) {
      if (!userData.password) {
        delete userData.password;
      }
      this.http.put(`/api/users/${this.editingUser.id}`, userData).subscribe({
        next: () => {
          this.successMessage = 'Utilizator actualizat cu succes!';
          this.loadUsers(); 
          this.closeEditUser(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la actualizare.';
        }
      });
    } else {
      this.http.post('/api/users', userData).subscribe({
        next: () => { 
          this.successMessage = 'Utilizator creat cu succes!';
          this.loadUsers(); 
          this.closeAddForm(); 
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la creare.';
        }
      });
    }
  }

  deleteUser(u: any): void {
    if (confirm(`Ești sigur că vrei să ștergi utilizatorul ${u.name}? Această acțiune este ireversibilă.`)) {
      this.http.delete(`/api/users/${u.id}`).subscribe({
        next: () => {
          this.successMessage = 'Utilizator șters cu succes!';
          this.loadUsers();
          if (this.viewingUser?.id === u.id) this.closeViewUser();
          if (this.editingUser?.id === u.id) this.closeEditUser();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || 'Eroare la ștergere.';
        }
      });
    }
  }

  clearFilters(): void {
    this.roleFilter = 'All Roles';
    this.statusFilter = 'All Statuses';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
