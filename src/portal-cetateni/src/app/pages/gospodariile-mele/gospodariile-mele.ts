import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-gospodariile-mele',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule],
  templateUrl: './gospodariile-mele.html',
  styleUrls: ['./gospodariile-mele.css']
})
export class GospodariileMele implements OnInit {
  gospodarii: any[] = [];
  profile: any = null;
  isLoading = false;
  error: string | null = null;

  constructor(
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadProfileAndGospodarii();
  }

  loadProfileAndGospodarii() {
    this.isLoading = true;
    this.error = null;
    this.cdr.detectChanges();
    
    // First we get the citizen profile to extract the details
    this.http.get<any>('/api/public/cetatean/me').subscribe({
      next: (profile) => {
        this.profile = profile;
        this.loadGospodarii();
      },
      error: (err) => {
        this.isLoading = false;
        this.error = 'A apărut o eroare la încărcarea profilului tău.';
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  loadGospodarii() {
    this.http.get<any[]>('/api/public/cetateni/me/gospodarii').subscribe({
      next: (res) => {
        this.gospodarii = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        console.error(err);
        this.error = 'Nu s-au putut încărca gospodăriile. Verifică conexiunea sau autentificarea.';
        this.cdr.detectChanges();
      }
    });
  }
}
